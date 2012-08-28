package com.futurice.tantalum3.rms;

import com.futurice.tantalum3.Result;
import com.futurice.tantalum3.Workable;
import com.futurice.tantalum3.Worker;
import com.futurice.tantalum3.log.Log;
import com.futurice.tantalum3.util.LRUVector;
import com.futurice.tantalum3.util.SortedVector;
import com.futurice.tantalum3.util.WeakHashCache;
import java.util.LinkedList;

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

    protected final AndroidDatabase database;
    protected static final int DATA_TYPE_IMAGE = 1;
    protected static final int DATA_TYPE_XML = 2;
    private static final SortedVector caches = new SortedVector(new SortedVector.Comparator() {
        @Override
        public boolean before(final Object o1, final Object o2) {
            return ((StaticCache) o1).priority < ((StaticCache) o2).priority;
        }
    });
    private static final LinkedList rmsWriteWorkables = new LinkedList();
    private static final Workable writeAllPending = new Workable() {
        @Override
        public Object compute() {
            try {
                Workable work;
                while (!rmsWriteWorkables.isEmpty()) {
                    synchronized (rmsWriteWorkables) {
                        work = (Workable) rmsWriteWorkables.poll();
                    }
                    work.compute();
                    if (!rmsWriteWorkables.isEmpty()) {
                        try {
                            // DEBUG TEST- be kind to slow phones, avoid crashes
                            Thread.sleep(50);
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            } catch (Exception e) {
                Log.l.log("Can not write all pending", "", e);
            }

            return null;
        }
    };
    protected final WeakHashCache cache = new WeakHashCache();
    protected final LRUVector accessOrder = new LRUVector();
    protected final char priority; // Must be unique, preferrably and integer,
    // larger characters get more space when
    // space is limited
    protected final DataTypeHandler handler;
    protected int sizeAsBytes = 0;

    /**
     * Create a named cache
     *
     * Caches with higher priority are more likely to keep their data when space
     * is limited.
     *
     * @param name
     * @param priority , a character from '0' to '9', higher numbers get a
     * preference for space
     * @param handler
     */
    public StaticCache(final char priority, final DataTypeHandler handler) {
        database = new AndroidDatabase();

        this.priority = priority;
        this.handler = handler;

        if (priority < '0') {
            throw new IllegalArgumentException("Priority=" + priority + " is invalid, must be '0' or higher");
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
    protected synchronized Object convertAndPutToHeapCache(final String key, final byte[] bytes) {
        //#debug
        Log.l.log("Start to convert", key);
        final Object o = handler.convertToUseForm(bytes);
        accessOrder.addElement(key);
        cache.put(key, o);
        //#debug
        Log.l.log("End convert", key);

        return o;
    }

    public synchronized Object synchronousRAMCacheGet(final String key) {
        Object o = null;

        if (containsKey(key)) {
            // Log.l.log("StaticCache hit in RAM", key);
            this.accessOrder.addElement(key);
            o = cache.get(key);
        }

        return o;
    }

    public void get(final String key, final Result result, final int priority) {
        if (key == null || key.length() == 0) {
            Log.l.log("Trivial get", "");
            result.onCancel();
            return;
        }
        final Object ho = synchronousRAMCacheGet(key);

        if (ho != null) {
            Log.l.log("RAM cache hit", "(" + priority + ") " + key);
            result.setResult(ho);
        } else {
            final Workable getWorkable = new Workable() {
                @Override
                public Object compute() {
                    try {
                        final Object o = synchronousGet(key);

                        if (o != null) {
                            //#debug
                            Log.l.log("RMS cache hit", key);
                            result.setResult(o);
                        } else {
                            //#debug
                            Log.l.log("RMS cache miss", key);
                            result.onCancel();
                        }
                    } catch (Exception e) {
                        Log.l.log("Can not get", key, e);
                    }

                    return null;
                }
            };

            Worker.fork(getWorkable, priority);
        }
    }

    protected Object synchronousGet(final String key) {
        Object o = synchronousRAMCacheGet(key);

        if (o == null) {
            // Load from flash memory
            final byte[] bytes = this.database.getData(key);

            if (bytes != null) {
                //#debug
                Log.l.log("StaticCache hit in RMS", "(" + priority + ") " + key);

                o = convertAndPutToHeapCache(key, bytes);
            }
        }

        return o;
    }

    public synchronized Object put(final String key, final byte[] bytes) {
        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException("Attempt to put trivial key to cache");
        }
        if (bytes == null) {
            throw new IllegalArgumentException("Attempt to put trivial bytes to cache: key=" + key);
        }
        rmsWriteWorkables.addLast(new Workable() {
            @Override
            public Object compute() {
                try {
                    synchronousPutToRMS(key, bytes);
                } catch (Exception e) {
                    Log.l.log("Can not synch write to RMS", key, e);
                }

                return null;
            }
        });
        Worker.fork(writeAllPending, Worker.HIGH_PRIORITY);

        return convertAndPutToHeapCache(key, bytes);
    }

    /**
     * Store the object to RMS, blocking the calling thread until the write is
     * complete.
     *
     * Generally you should use this method if you are on a Worker thread to
     * avoid adding large objects in the Worker forkSerial waiting to be stored to
     * the RMS which could lead to a memory shortage. If you are on the EDT, use
     * the asynchronous put() method instead to avoid blocking the calling
     * thread.
     *
     * @param key
     * @param bytes
     * @param putToHeapCache - Set "true" unless an overriding method has
     * already done this
     * @return
     */
    protected void synchronousPutToRMS(final String key, final byte[] bytes) {
        if (key == null) {
            throw new IllegalArgumentException("Null key put to cache");
        }
        try {
            do {
                Log.l.log("RMS cache write start", key + " (" + bytes.length + " bytes)");
                this.database.putData(key, bytes);
                Log.l.log("RMS cache write end", key + " (" + bytes.length + " bytes)");
                break;

            } while (true);
        } catch (Exception e) {
            Log.l.log("Couldn't store object to RMS", key, e);
        }
    }

    /**
     * Remove unused and then currently used items from the RMS cache to make
     * room for new items.
     *
     * @param minSpaceToClear - in bytes
     * @return true if the requested amount of space has been cleared
     */
    protected static boolean clearSpace(final int minSpaceToClear) {

        //TODO: Implement on Android

        /*
         * int spaceCleared = 0; final Vector rsv =
         * RMSUtils.getCachedRecordStoreNames();
         * 
         * // #debug Log.l.log("Clearing RMS space", minSpaceToClear + "
         * bytes");
         * 
         * // First: clear cached objects not currently appearing in any open
         * cache for (int i = rsv.size() - 1; i >= 0; i--) { final String key =
         * (String) rsv.elementAt(i); final StaticCache cache =
         * getCacheContainingKey(key);
         * 
         * if (cache != null) { spaceCleared += getByteSizeByKey(key);
         * cache.remove(key); } } // #debug Log.l.log("End phase 1: clearing RMS
         * space", spaceCleared + " bytes recovered");
         * 
         * // Second: remove currently cached items, first from low priority
         * caches while (spaceCleared < minSpaceToClear && rsv.size() > 0) { for
         * (int i = 0; i < caches.size(); i++) { final StaticCache cache =
         * (StaticCache) caches.elementAt(i);
         * 
         * while (!cache.accessOrder.isEmpty() && spaceCleared <
         * minSpaceToClear) { final String key = (String) cache.accessOrder
         * .removeLeastRecentlyUsed(); spaceCleared += getByteSizeByKey(key);
         * cache.remove(key); } } } // #debug Log.l.log("End phase 2: clearing
         * RMS space", spaceCleared + " bytes recovered (total)");
         * 
         * return spaceCleared >= minSpaceToClear;
         */
        return true;
    }

    protected static int getByteSizeByKey(final String key) {
        int size = 0;

        // TODO: Write for android
        Log.l.log("Can not check size of record store to clear space", key);

        return size;
    }

    /**
     * Note that delete is synchronous, so while this operation does not take
     * long, other operations using the RMS may cause a slight stagger or pause
     * before this operation can complete.
     *
     * @param key
     */
    protected void remove(final String key) {
        try {
            if (containsKey(key)) {
                synchronized (StaticCache.this) {
                    accessOrder.removeElement(key);
                    cache.remove(key);
                }
                this.database.removeData(key);
                Log.l.log("Cache remove (from RAM and RMS)", key);
            }
        } catch (Exception e) {
            Log.l.log("Couldn't remove object from cache", key, e);
        }
    }

    public void clear() {
        Log.l.log("Start Cache Clear", "ID=" + priority);
        final String[] keys;

        synchronized (this) {
            keys = new String[accessOrder.size()];
            accessOrder.copyInto(keys);
        }
        for (int i = 0; i < keys.length; i++) {
            remove(keys[i]);
        }
        Log.l.log("Cache cleared", "ID=" + priority);
    }

    public synchronized boolean containsKey(final String key) {
        if (key != null) {
            return this.cache.containsKey(key);
        }

        return false;
    }

    public synchronized int getSize() {
        return this.cache.size();
    }

    public synchronized int getPriority() {
        return priority;
    }

    public DataTypeHandler getHandler() {
        return handler;
    }

    @Override
    public synchronized String toString() {
        String str = "StaticCache --- priority: " + priority + " size: " + getSize() + " size (bytes): " + sizeAsBytes + "\n";

        for (int i = 0; i < accessOrder.size(); i++) {
            str += accessOrder.elementAt(i) + "\n";
        }

        return str;
    }
}
