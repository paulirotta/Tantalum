package com.futurice.tantalum2.rms;

import com.futurice.tantalum2.DefaultResult;
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
    protected final WeakHashCache cache = new WeakHashCache();
    protected final LRUVector accessOrder = new LRUVector();
    protected final String name;
    protected final char priority; // Must be unique, '0'-'9', larger numbers get more space when space is limited
    protected final DataTypeHandler handler;
    protected int sizeAsBytes = 0;

    /**
     * Create a named cache
     *
     * Caches with higher priority are more likely to keep their data when space
     * is limited.
     *
     * @param name
     * @param priority, a character from '0' to '9', higher numbers get a
     * preference for space
     * @param handler
     */
    public StaticCache(final String name, final char priority, final DataTypeHandler handler) {
        this.name = name;
        this.priority = priority;
        this.handler = handler;

        if (priority < '0' || priority > '9') {
            throw new IllegalArgumentException("Priority=" + priority + " is invalid, must be '0'-'9'");
        }
        synchronized (caches) {
            for (int i = 0; i < caches.size(); i++) {
                if (((StaticCache) caches.elementAt(i)).priority == priority) {
                    throw new IllegalArgumentException("A StaticCache with priority=" + priority + " already exists");
                }
            }
            caches.addElement(this);
        }
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
    public synchronized Object synchronousRAMCacheGet(final String key) {
        Object o = null;

        if (containsKey(key)) {
//            Log.l.log("StaticCache hit in RAM", key);
            this.accessOrder.addElement(key);
            o = cache.get(key);
            if (o != null) {
                return o;
            }
        }

        return o;
    }

    public synchronized Object synchronousRAMCacheAndRMSGet(final String key) {
        Object o = synchronousRAMCacheGet(key);

        if (o == null) {
            final byte[] bytes = RMSUtils.read(key);
            
            if (bytes != null) {
                Log.l.log("StaticCache hit in RMS", key);

                o = convertAndPutToHeapCache(key, bytes);
            }
        }

        return o;
    }

    public void get(final String key, final DefaultResult result) {
        final Object ho = synchronousRAMCacheGet(key);

        if (ho != null) {
            result.setResult(ho);
        } else {
            Worker.queue(new Workable() {

                public boolean work() {
                    final Object o = synchronousRAMCacheAndRMSGet(key);

                    if (o != null) {
                        result.setResult(o);
                    } else {
                        result.noResult();
                    }

                    return false;
                }
            });
        }
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
        Worker.queue(new Workable() {

            public boolean work() {
                try {
                    Log.l.log("Store to RMS", key);
                    RMSUtils.write(key, bytes);
                } catch (Exception e) {
                    Log.l.log("Couldn't store object to RMS", key, e);
                }

                return false;
            }
        });

        return convertAndPutToHeapCache(key, bytes);
    }

    public synchronized void remove(final String key) {
        try {
            if (containsKey(key)) {
                this.accessOrder.removeElement(key);
                this.cache.remove(key);
                RMSUtils.delete(key);
                Log.l.log("Removed (from RAM and RMS)", key);
            }
        } catch (Exception e) {
            Log.l.log("Couldn't remove object from cache", key, e);
        }
    }

    /**
     * Remove all elements from this cache
     *
     */
    public synchronized void clear() {
        while (accessOrder.size() > 0) {
            remove((String) accessOrder.lastElement());
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
     * caches. Higher priority caches synchronousRAMCacheGet more space.
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
        String str = "StaticCache " + name + " --- priority: " + priority + " size: " + getSize() + " size (bytes): " + sizeAsBytes + "\n";

        for (int i = 0; i < accessOrder.size(); i++) {
            str += accessOrder.elementAt(i) + "\n";
        }

        return str;
    }
}
