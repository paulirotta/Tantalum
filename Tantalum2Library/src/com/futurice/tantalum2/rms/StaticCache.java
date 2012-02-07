package com.futurice.tantalum2.rms;

import com.futurice.tantalum2.WeakHashCache;
import com.futurice.tantalum2.log.Log;

import java.util.Hashtable;
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
    protected Hashtable objectSizes = new Hashtable();

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
     * If you also want the object stored in RMS, call
     * storeToRMS()
     * 
     * @param key
     * @param o 
     */
    public void put(final String key, final Object o) {
        if (o == null) {
            Log.logNonfatalThrowable(new NullPointerException(), "Null put for key " + key);
            return;
        }
        remove(key);
        accessOrder.addElement(key);
        cache.put(key, o);
    }

    /**
     * Synchronously return the hash object
     * 
     * @param key
     * @return 
     */
    public synchronized Object get(final String key) {
        if (containsKey(key)) {
            Log.log("Hit in RAM");
            int index = this.accessOrder.indexOf(key);
            this.accessOrder.removeElementAt(index);
            this.accessOrder.addElement(key);
            return cache.get(key);
        }

        final byte[] bytes = getFromRMS(key);
        if (bytes != null) {
            Log.log("Hit in RMS");
            Object converted = handler.convertToUseForm(bytes);
            put(key, converted);
            return converted;
        }

        return null;
    }

    /**
     * Removes items until there is space to store the needed item
     * returns null if cannot make space
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
     * Removes an item from the StaticCache which most exceeds its reserved space
     * according to available space and assigned priority number
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

    protected synchronized byte[] getFromRMS(String key) {
        return (new RMSGetter(SESSION_ID, RMSResourceType.BYTE_ARRAY, key)).get();
    }

    /**
     * Store a value to nonvolatile memory
     * 
     * @param key
     * @param bytes 
     */
    public synchronized void storeToRMS(String key, byte[] bytes) {
        final ByteArrayStorableResource res = new ByteArrayStorableResource(0, key, bytes);
        final int recordLength = res.serialize().length;
        
        if (makeSpace(recordLength)) {
            try {
                RMSResourceDB.getInstance().storeResource(res, null);
                Log.log("Stored: " + key);
                this.objectSizes.put(key, new Integer(recordLength));
                this.sizeAsBytes += recordLength;
            } catch (Exception e) {
                Log.log("Couldn't store object to RMS: " + key);
            }
        } else {
            Log.log("Couldn't store object to RMS (too big item?): " + key);
        }
        Log.log("*** All caches size total: " + countTotalSizeAsBytes() + "/" + TOTAL_SIZE_BYTES_MAX + " Cache " + this.name + " size: " + sizeAsBytes + " Size of record stored: " + recordLength);
    }

    private synchronized void removeFromRMS(String key) {
        Object o = objectSizes.get(key);
        if (o != null) {
            RMSResourceDB.getInstance().deleteResource(RMSResourceType.BYTE_ARRAY, key);
            int recordLength = ((Integer) o).intValue();
            this.objectSizes.remove(key);
            this.sizeAsBytes -= recordLength;
        } else {
            Log.log("Couldn't remove object from RMS: " + key);
        }
    }

    protected synchronized void remove(final String key) {
        if (containsKey(key)) {
            this.accessOrder.removeElement(key);
            this.cache.remove(key);
            removeFromRMS(key);
            Log.log("Removed (from RAM and RMS): " + key);
        }
    }

    private synchronized void removeOldest() {
        if (this.accessOrder.size() > 0) {
            String key = (String) this.accessOrder.elementAt(0);
            this.cache.remove(key);
            this.accessOrder.removeElementAt(0);
            removeFromRMS(key);
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
        if (key instanceof String) {
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
     * The relative priority used for allocating RMS space between multiple caches.
     * Higher priority caches get more space.
     * 
     * @return 
     */
    public synchronized int getPriority() {
        return priority;
    }

    /**
     * Provide the handler which this caches uses for converting between in-memory
     * and binary formats
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
