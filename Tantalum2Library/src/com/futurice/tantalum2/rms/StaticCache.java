package com.futurice.tantalum2.rms;

import com.futurice.tantalum2.Result;
import com.futurice.tantalum2.Workable;
import com.futurice.tantalum2.Worker;
import com.futurice.tantalum2.log.Log;

import java.util.Vector;

/**
 * A cache which returns Objects based on a String key asynchronously from RAM,
 * RMS, or network and synchronously from RAM and RMS.
 *
 * Objects in RAM are kept with WeakReferences so they may be garbage collected.
 *
 * Objects in RMS are managed in a "least recently accessed" form to make space.
 *
 * Each StaticCache uses a single RMS and may be referred to by name.
 *
 * You may provide alternative MODEs to change the default characteristics of a
 * given StaticCache.
 */
public class StaticCache {

    protected static final int DATA_TYPE_IMAGE = 1;
    protected static final int DATA_TYPE_XML = 2;
    private static final Vector caches = new Vector();
    private static final int TOTAL_SIZE_BYTES_MAX = 150 * 1024;
    private static int sumOfPriorities = 0;
    protected final WeakHashCache cache = new WeakHashCache();
    protected final Vector accessOrder = new Vector();
    protected final String name;
    protected final int priority; // defines the size of reserved space for this cache
    protected final DataTypeHandler handler;
    protected int sizeAsBytes = 0;
    private static long SESSION_ID;

    /**
     * Create a named cache
     *
     * Caches with higher priority are more likely to keep their data when space
     * is limited.
     *
     * @param name
     * @param priority, a positive integer
     * @param handler
     */
    public StaticCache(String name, int priority, DataTypeHandler handler) {
        this.name = name;
        this.priority = priority;
        this.handler = handler;
        SESSION_ID = System.currentTimeMillis();

        sumOfPriorities += priority;
        caches.addElement(this);
    }

    /**
     * Synchronously put the hash object to the RAM cache.
     *
     * If you also want the object stored in RMS, call put()
     *
     * @param key
     * @param o
     */
    private Object convertAndPutToHeapCache(final String key, final byte[] bytes) {
        final Object o = handler.convertToUseForm(bytes);
        remove(key);
        accessOrder.addElement(key);
        cache.put(key, o);

        return o;
    }

    /**
     * Synchronously return the hash object
     *
     * @param key
     * @return
     */
    public synchronized Object synchronousGet(final String key) {
        Object o = null;

        if (containsKey(key)) {
            Log.l.log("StaticCache hit in RAM", key);
            this.accessOrder.removeElement(key);
            this.accessOrder.addElement(key);
            o = cache.get(key);
            if (o != null) {
                return o;
            }
        }

        return o;
    }

    public synchronized Object synchronousGetIncludingRMS(final String key) {
        Object o = synchronousGet(key);

        if (o == null) {
            final byte[] bytes = (new RMSGetter(SESSION_ID, RMSResourceType.BYTE_ARRAY, key)).get();
            if (bytes != null) {
                Log.l.log("StaticCache hit in RMS", key);

                return convertAndPutToHeapCache(key, bytes);
            }
        }

        return o;
    }

    public void get(final String key, final Result result) {
        final Object ho = synchronousGet(key);
        if (ho != null) {
            result.setResult(ho, true);
            Worker.queueEDT(result);
            return;
        }
        
        Worker.queue(new Workable() {

            public boolean work() {
                final Object o = synchronousGetIncludingRMS(key);

                if (o != null) {
                    result.setResult(o, true);
                    Worker.queueEDT(result);
                } else {
                    result.noResult();
                }

                return false;
            }
        });
    }

    /**
     * Removes items until there is space to store the needed item returns null
     * if cannot make space
     *
     * @param spaceNeeded
     * @return
     */
    public synchronized static boolean makeSpace(final int spaceNeeded) {
        if (spaceNeeded > TOTAL_SIZE_BYTES_MAX) {
            return false;
        }
        while (countTotalSizeAsBytes() + spaceNeeded > TOTAL_SIZE_BYTES_MAX) {
            removeMostUseless();
        }
        return true;
    }

    private synchronized static int countTotalSizeAsBytes() {
        int total = 0;
        for (int i = 0; i < caches.size(); i++) {
            total += ((StaticCache) caches.elementAt(i)).sizeAsBytes;
        }

        return total;
    }

    /**
     * Removes an item from the StaticCache which most exceeds its reserved
     * space according to available space and assigned priority number
     *
     */
    private synchronized static void removeMostUseless() {
        StaticCache mostExceedingCache = (StaticCache) caches.elementAt(0);
        double biggestRatio = (double) mostExceedingCache.sizeAsBytes / (double) mostExceedingCache.getSizeOfReservedSpace();

        if (caches.size() > 1) {
            for (int i = 1; i < caches.size(); i++) {
                final StaticCache candidate = (StaticCache) caches.elementAt(i);
                final double candidateRatio = (double) candidate.sizeAsBytes / (double) candidate.getSizeOfReservedSpace();

                if (candidateRatio > biggestRatio) {
                    mostExceedingCache = candidate;
                    biggestRatio = candidateRatio;
                }
            }
        }
        mostExceedingCache.removeOldest();
    }

    protected synchronized int getSizeOfReservedSpace() {
        return TOTAL_SIZE_BYTES_MAX * priority / sumOfPriorities;
    }

    /**
     * Store a value to heap and flash memory
     *
     * @param key
     * @param bytes
     * @return the byte[] converted to use form by the cache's Handler
     */
    public synchronized Object put(final String key, final byte[] bytes) {
        if (key == null) {
            throw new IllegalArgumentException("Null key put to cache");
        }
        if (bytes == null) {
            throw new IllegalArgumentException("Null bytes put to cache");
        }
        final Object o = convertAndPutToHeapCache(key, bytes);
        Worker.queue(new Workable() {

            public boolean work() {
                final ByteArrayStorableResource res = new ByteArrayStorableResource(0, key, bytes);
                final int recordLength = res.serialize().length;

                if (makeSpace(recordLength)) {
                    try {
                        RMSResourceDB.getInstance().storeResource(res);
                        Log.l.log("Store to RMS", key);
                        accessOrder.removeElement(key);
                        accessOrder.addElement(key);
                    } catch (Exception e) {
                        Log.l.log("Couldn't store object to RMS", key, e);
                    }
                } else {
                    Log.l.log("Couldn't store object to RMS (too big item?)", key);
                }
                Log.l.log("*** All caches size total", countTotalSizeAsBytes() + "/" + TOTAL_SIZE_BYTES_MAX + " Cache " + name + " size: " + sizeAsBytes + " Size of record stored: " + recordLength);

                return false;
            }
        });

        return o;
    }

    public synchronized void remove(final String key) {
        try {
            if (containsKey(key)) {
                this.accessOrder.removeElement(key);
                this.cache.remove(key);
                this.accessOrder.removeElement(key);
                RMSResourceDB.getInstance().deleteResource(RMSResourceType.BYTE_ARRAY, key);
                Log.l.log("Removed (from RAM and RMS)", key);
            }
        } catch (Exception e) {
            Log.l.log("Couldn't remove object from cache", key, e);
        }
    }

    private synchronized void removeOldest() {
        if (this.accessOrder.size() > 0) {
            remove((String) this.accessOrder.elementAt(0));
        }
    }

    /**
     * Remove all elements from this cache
     *
     */
    public synchronized void clear() {
        while (this.accessOrder.size() > 0) {
            removeOldest();
        }
    }

    /**
     * Does this cache contain an object matching the key?
     *
     * @param key
     * @return
     */
    public synchronized boolean containsKey(final String key) {
        if (key != null) {
            return this.cache.containsKey(key);
        }

        return false;
    }

    /**
     * The number of items in the cache
     *
     * @return
     */
    public synchronized int getSize() {
        return this.cache.size();
    }

    /**
     * The relative priority used for allocating RMS space between multiple
     * caches. Higher priority caches synchronousGet more space.
     *
     * @return
     */
    public synchronized int getPriority() {
        return priority;
    }

    /**
     * Provide the handler which this caches uses for converting between
     * in-memory and binary formats
     *
     * @return
     */
    public DataTypeHandler getHandler() {
        return handler;
    }

    /**
     * For debugging use
     *
     * @return
     */
    public synchronized String toString() {
        String str;
        str = "StaticCache " + name + " --- priority: " + priority + " size: " + getSize() + " size (bytes): " + sizeAsBytes + " space: " + this.getSizeOfReservedSpace() + "\n";
        for (int i = 0; i < accessOrder.size(); i++) {
            str += accessOrder.elementAt(i) + "\n";
        }
        return str;
    }
}
