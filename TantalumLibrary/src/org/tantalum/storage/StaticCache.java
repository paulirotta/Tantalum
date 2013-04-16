/*
 Copyright (c) 2013, Paul Houghton and Futurice Oy
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
package org.tantalum.storage;

import java.io.UnsupportedEncodingException;
import java.security.DigestException;
import org.tantalum.PlatformUtils;
import org.tantalum.Task;
import org.tantalum.util.L;
import org.tantalum.util.LRUVector;
import org.tantalum.util.SortedVector;
import org.tantalum.util.StringUtils;
import org.tantalum.util.WeakHashCache;

/**
 * A ramCache which returns Objects based on a String key asynchronously from
 * RAM, RMS, or network and synchronously from RAM and RMS.
 *
 * Objects in RAM are kept with WeakReferences so they may be garbage collected.
 *
 * Objects in flash memory are managed in a "least recently accessed" form to
 * garbage collect and make space when persistent storage runs low.
 *
 * Each StaticCache uses a single RMS and may be referred to by name.
 *
 * You may provide alternative MODEs to change the default characteristics of a
 * given StaticCache.
 */
public class StaticCache {

    /**
     * A list of all caches, sorted by cachePriorityChar order (lowest char
     * first)
     */
    protected static final SortedVector caches = new SortedVector(new SortedVector.Comparator() {
        public boolean before(final Object o1, final Object o2) {
            return ((StaticCache) o1).cachePriorityChar < ((StaticCache) o2).cachePriorityChar;
        }
    });
    /*
     * Always access withing a synchronized block
     */
    private final FlashCache flashCache;
    /**
     * A heap memory ramCache in the form of a Hashtable from which data can be
     * removed automatically by the virtual machine to free up memory (automatic
     * memory management).
     */
    protected final WeakHashCache ramCache = new WeakHashCache();
    /**
     * The order in which object in the ramCache have been accessed since the
     * program started. Each time an object is accessed, it moves to the
     * beginning of the last. Least recently used objects are the ones most
     * likely to be cleared when additional flash memory is needed. The heap
     * memory WeakReferenceCache does not make use of this access order.
     */
    protected final LRUVector accessOrder = new LRUVector();
    /**
     * This character serves as a market tag to distinguish the contents of this
     * ramCache from other caches which may also be stored in flash memory in a
     * flat name space. This must be unique like '0'..'9' or 'a'..'z'. Larger
     * character values indicate lower cachePriorityChar caches which will be
     * garage collected first when flash memory is low, so use larger characters
     * for more transient or less-important-to-persist data.
     */
    protected final char cachePriorityChar;
    /**
     * This interface gives one method you must provide to convert from the raw
     * byte[] format received from a web server or similar source into an Object
     * format such as Image or your own data model. This is usually a parser of
     * some kind. It will be run both when data first arrives over the network,
     * and again any time the data is loaded from flash memory. Since the heap
     * memory ramCache uses WeakReference, values may be loaded from flash
     * memory several times. This conversion method should therefore be
     * stateless and thread safe so it can be run on several threads and
     * possibly multiple cores at the same time.
     */
    protected final DataTypeHandler handler;
    /**
     * Total size of the ramCache as it exists in flash memory. This is often
     * larger than the current heap ramCache memory consumption.
     */
    protected int sizeAsBytes = 0;
    /*
     *  For testing and performance comparison
     * 
     *  Always access within a synchronized block
     */
    private boolean flashCacheEnabled = true;
    /**
     * All synchronization is not on "this", but on the hidden MUTEX Object to
     * encapsulate synch and disallow the bad practice of externally
     * synchronizing on the ramCache object itself.
     */
    protected final Object MUTEX = new Object();

    /**
     * Get the previously-created cache with the same parameters
     *
     * @param cachePriorityChar
     * @param handler
     * @param taskFactory
     * @param clas
     * @return
     */
    protected static StaticCache getExistingCache(final char priority, final DataTypeHandler handler, final Object taskFactory, final Class clas) {
        synchronized (caches) {
            for (int i = 0; i < caches.size(); i++) {
                final StaticCache c = (StaticCache) caches.elementAt(i);

                if (c.cachePriorityChar == priority) {
                    if (c.getClass() != clas) {
                        throw new IllegalArgumentException("You can not create a StaticCache and a StaticWebCache with the same priority: " + priority);
                    }
                    if (c.equals(priority, handler, taskFactory)) {
                        throw new IllegalArgumentException("A cache with priority=" + priority + " already exists, but DataTypeHandler and/or HttpTaskFactory are now equal to your factory request");
                    }

                    return c;
                }
            }

            return null;
        }
    }

    /**
     * Get a named Cache
     *
     * Caches with higher cachePriorityChar are more likely to keep their data
     * when space is limited.
     *
     * You will getDigests IllegalArgumentException if you call this multiple
     * times for the same cache cachePriorityChar but with a different (not
     * .equals()) DataTypeHandler.
     *
     * @param cachePriorityChar - a character from '0' to '9', higher numbers
     * getDigests a preference for space. Letters are also allowed.
     * @param cacheType a constant such at PlatformUtils.PHONE_DATABASE_CACHE
     * @param handler - a routine to convert from byte[] to Object form when
     * loading into the RAM ramCache.
     * @return
     * @throws DigestException
     * @throws UnsupportedEncodingException
     */
    public static synchronized StaticCache getCache(final char priority, final int cacheType, final DataTypeHandler handler) throws FlashDatabaseException {
        StaticCache c = getExistingCache(priority, handler, null, StaticCache.class);

        if (c == null) {
            c = new StaticCache(priority, cacheType, handler);
            caches.addElement(c);
        }

        return c;
    }

    /**
     * Create a named Cache
     *
     * @param cachePriorityChar
     * @param cacheType
     * @param handler
     * @throws DigestException
     * @throws UnsupportedEncodingException
     */
    protected StaticCache(final char priority, final int cacheType, final DataTypeHandler handler) throws FlashDatabaseException {
        if (priority < '0') {
            throw new IllegalArgumentException("Priority=" + priority + " is invalid, must be '0' or higher");
        }
        this.cachePriorityChar = priority;
        this.handler = handler;
        flashCache = PlatformUtils.getInstance().getFlashCache(priority, cacheType);
        init();
    }

    /**
     * Load the keys from flash memory
     *
     * We want to use the RAM Hashtable to know what the ramCache contains, even
     * though we do not pre-load from flash all the values
     */
    private void init() throws FlashDatabaseException {
        try {
            final byte[][] digests = flashCache.getDigests();
            for (int i = 0; i < digests.length; i++) {
                ramCache.markContains(digests[i]);
            }
        } catch (Exception ex) {
            //#debug
            L.e(this, "Can not load keys to RAM during init() cache", "cache priority=" + cachePriorityChar, ex);
            throw new FlashDatabaseException("Can not load cache keys during init for cache '" + cachePriorityChar + "' : " + ex);
        }
    }

    /**
     * Turn off persistent local storage' use for read and write to see what
     * happens to the application performance. This is most useful for
     * StaticWebCache but may be useful in other test cases.
     *
     * The default is enabled
     *
     * @param enabled
     */
    public void setFlashCacheEnabled(final boolean enabled) {
        this.flashCacheEnabled = enabled;
    }

    /**
     * Synchronously put the hash object to the RAM ramCache.
     *
     * If you also want the object stored in RMS, call put()
     *
     * @param key
     * @param bytes
     * @return
     */
    protected Object convertAndPutToHeapCache(final String key, final byte[] bytes) throws DigestException, UnsupportedEncodingException {
        //#mdebug
        L.i(this, "Start to convert", key + " bytes length=" + bytes.length);
        final long startTime = System.currentTimeMillis();
        //#enddebug
        final Object o = handler.convertToUseForm(key, bytes);

        synchronized (MUTEX) {
            final byte[] digest = flashCache.toDigest(key);
            accessOrder.addElement(digest);
            ramCache.put(digest, o);
        }
        //#debug
        L.i(this, "End convert, elapsedTime=" + (System.currentTimeMillis() - startTime) + "ms", key);

        return o;
    }

    /**
     * Synchronously return the hash object
     *
     * @param key
     * @return
     */
    public Object synchronousRAMCacheGet(final String key) throws FlashDatabaseException {
        synchronized (MUTEX) {
            try {
                final byte[] digest = flashCache.toDigest(key);
                Object o = ramCache.get(digest);

                if (o != null) {
                    //#debug            
                    L.i(this, "Possible StaticCache hit in RAM (might be expired WeakReference)", key);
                    this.accessOrder.addElement(key);
                }

                return o;
            } catch (Exception e) {
                //#debug
                L.e(this, "Can not synchronousRAMCacheGet", key, e);
                throw new FlashDatabaseException("Can not get from heap cache: " + key + " - " + e);
            }
        }
    }

    /**
     * Retrieve an object from RAM or RMS storage.
     *
     * @param key
     * @param cachePriorityChar - set to Work.FASTLANE_PRIORITY if you want the
     * results quickly to update the UI.
     * @param nextTask
     * @return
     */
    public Task getAsync(final String key, int priority, final Task nextTask) {
        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException("Trivial StaticCache get");
        }
        if (priority == Task.HIGH_PRIORITY) {
            priority = Task.FASTLANE_PRIORITY;
        }
        
        return (new GetLocalTask(key, priority)).chain(nextTask).fork();
    }

    /**
     * Get the value from local RAM or Flash ramCache memory. null is returned
     * if the value is not available locally.
     *
     * @param key
     * @return
     * @throws FlashDatabaseException
     */
    protected Object synchronousGet(final String key) throws FlashDatabaseException {
        Object o = synchronousRAMCacheGet(key);

        //#debug
        L.i(this, "Heap get result", "(" + cachePriorityChar + ") " + key + " : " + o);
        if (o == null) {
            try {
                // Load from flash memory
                final byte[] bytes;
                synchronized (MUTEX) {
                    if (flashCacheEnabled) {
                        bytes = flashCache.get(key);
                    } else {
                        bytes = null;
                    }
                }

                //#debug
                L.i(this, "Flash get result", "(" + cachePriorityChar + ") key=" + key + " byteLength=" + (bytes != null ? ("" + bytes.length) : "<null>"));
                if (bytes != null) {
                    o = convertAndPutToHeapCache(key, bytes);
                    //#debug
                    L.i(this, "Flash get converted result", "(" + cachePriorityChar + ") " + key + " : " + o);
                }
            } catch (DigestException e) {
                //#debug
                L.e(this, "Can not synchronousGet", key, e);
                throw new FlashDatabaseException("Can not synchronousGet: " + key + " - " + e);
            } catch (UnsupportedEncodingException e) {
                //#debug
                L.e(this, "Can not synchronousGet", key, e);
                throw new FlashDatabaseException("Can not synchronousGet: " + key + " - " + e);
            }
        }

        return o;
    }

    /**
     * Store a value to heap and flash memory.
     *
     * Conversion to from byte[] to use form (POJO, Plain Old Java Object)
     * happens synchronously on the calling thread before before this method
     * returns. If data type conversion may take a long time (XML or JSON
     * parsing, etc) then avoid calling this method from the UI thread.
     *
     * Actual storage to persistent flash storage is done asynchronously on a
     * background worker thread. This is done at high priority to prevent the
     * queue of to-be-written objects from taking up precious heap memory. Items
     * are written in the order in which calls to this method complete. You may
     * prefer to explicitly manage this processes yourself by use of
     * synchronousPutToRMS().
     *
     * @param key
     * @param bytes
     * @return the byte[] converted to the parsed Object "use form" returned by
     * this cache's DataTypeHandler
     * @throws FlashDatabaseException
     */
    public Object put(final String key, final byte[] bytes) throws FlashDatabaseException {
        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException("Attempt to put trivial key to cache");
        }
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Attempt to put trivial bytes to cache: key=" + key);
        }
        final Object useForm;
        //#debug
        L.i(this, "put", "key=" + key + " byteLength=" + bytes.length);
        try {
            useForm = convertAndPutToHeapCache(key, bytes);
        } catch (DigestException ex) {
            //#debug
            L.e("Can not putAync", key, ex);
            throw new FlashDatabaseException("Can not putAsync: " + key + " - " + ex);
        } catch (UnsupportedEncodingException ex) {
            //#debug
            L.e("Can not putAync", key, ex);
            throw new FlashDatabaseException("Can not putAsync: " + key + " - " + ex);
        }
        if (flashCacheEnabled) {
            (new Task(Task.SERIAL_PRIORITY) {
                public Object exec(final Object in) {
                    try {
                        synchronousFlashPut(key, bytes);
                    } catch (FlashDatabaseException e) {
                        //#debug
                        L.e("Can not synch write to flash", key, e);
                        cancel(false, "Can not sync write to flash: " + key, e);
                    }

                    return in;
                }
            }.setClassName("SynchronousPutter")
                    .setShutdownBehaviour(Task.EXECUTE_NORMALLY_ON_SHUTDOWN))
                    .fork();
        }

        return useForm;
    }

    /**
     * Store the object to RMS, blocking the calling thread until the write is
     * complete.
     *
     * Generally you should use this method if you are on a Worker thread to
     * avoid adding large objects in the Worker forkSerial waiting to be stored
     * to the RMS which could lead to a memory shortage. If you are on the EDT,
     * use the asynchronous put() method instead to avoid blocking the calling
     * thread.
     *
     * @param key
     * @param bytes
     * @param putToHeapCache - Set "true" unless an overriding method has
     * already done this
     * @return
     */
    private void synchronousFlashPut(final String key, final byte[] bytes) throws FlashDatabaseException {
        if (key == null) {
            throw new IllegalArgumentException("Null key put to cache");
        }
        try {
            do {
                synchronized (MUTEX) {
                    try {
                        //#debug
                        L.i("RMS cache write start", key + " (" + bytes.length + " bytes)");
                        flashCache.put(key, bytes);
                        //#debug
                        L.i("RMS cache write end", key + " (" + bytes.length + " bytes)");
                        break;
                    } catch (FlashFullException ex) {
                        //#debug
                        L.e("Clearning space for data, ABORTING", key + " (" + bytes.length + " bytes)", ex);
                        if (!clearSpace(bytes.length)) {
                            //#debug
                            L.i("Can not clear enough space for data, ABORTING", key);
                        }
                    }
                }
            } while (true);
        } catch (DigestException e) {
            //#debug
            L.e("Couldn't store object to flash", key, e);
            throw new FlashDatabaseException("Could not store object to flash: " + key + " - " + e);
        } catch (UnsupportedEncodingException e) {
            //#debug
            L.e("Couldn't store object to flash", key, e);
            throw new FlashDatabaseException("Could not store object to flash: " + key + " - " + e);
        }
    }

    /**
     * Remove unused and then currently used items from the RMS ramCache to make
     * room for new items.
     *
     * @param minSpaceToClear - in bytes
     * @return true if the requested amount of space has been cleared
     */
    private boolean clearSpace(final int minSpaceToClear) throws FlashDatabaseException, UnsupportedEncodingException, DigestException {
        int spaceCleared = 0;

        final byte[][] rsv = flashCache.getDigests();

        //#debug
        L.i("Clearing RMS space", minSpaceToClear + " bytes");

        // First: clear cached objects not currently appearing in any open ramCache
        for (int i = rsv.length - 1; i >= 0; i--) {
            final byte[] digest = rsv[i];
            final StaticCache sc = getCacheContainingDigest(digest);

            if (sc != null) {
                spaceCleared += getByteSizeByDigest(digest);
                sc.remove(digest);
            }
        }
        //#debug
        L.i("End phase 1: clearing RMS space", spaceCleared + " bytes recovered");

        // Second: remove currently cached items, first from low cachePriorityChar caches
        while (spaceCleared < minSpaceToClear && rsv.length > 0) {
            for (int i = 0; i < caches.size(); i++) {
                final StaticCache sc = (StaticCache) caches.elementAt(i);

                while (!sc.accessOrder.isEmpty() && spaceCleared < minSpaceToClear) {
                    final byte[] digest = (byte[]) sc.accessOrder.removeLeastRecentlyUsed();
                    spaceCleared += getByteSizeByDigest(digest);
                    sc.remove(digest);
                }
            }
        }
        //#debug
        L.i("End phase 2: clearing RMS space", spaceCleared + " bytes recovered (total)");

        return spaceCleared >= minSpaceToClear;
    }

    private int getByteSizeByDigest(final byte[] digest) throws FlashDatabaseException, DigestException {
        final byte[] bytes = flashCache.get(digest);

        return bytes.length;
    }

    private static StaticCache getCacheContainingDigest(final byte[] digest) {
        StaticCache cache = null;

        synchronized (caches) {
            for (int i = 0; i < caches.size(); i++) {
                final StaticCache currentCache = (StaticCache) caches.elementAt(i);
                if (currentCache.containsDigest(digest)) {
                    cache = currentCache;
                    break;
                }
            }
        }

        return cache;
    }

    /**
     * Note that delete from flash is synchronous, so while this operation does
     * not take long, other operations using the RMS may cause a slight stagger
     * or pause before this operation can complete. On some phones this may be
     * visible as UI thread jitter even though the UI thread is only weakly
     * linked to the flash operations by concurrency.
     *
     * @param digest
     */
    protected void remove(final byte[] digest) {
        try {
            if (containsDigest(digest)) {
                synchronized (MUTEX) {
                    accessOrder.removeElement(digest);
                    ramCache.remove(digest);
                    flashCache.removeData(digest);
                }
                //#debug
                L.i("Cache remove (from RAM and RMS)", StringUtils.toHex(digest));
            }
        } catch (Exception e) {
            //#debug
            L.e("Couldn't remove object from cache", StringUtils.toHex(digest), e);
        }
    }

    /**
     * Remove all elements from this ramCache
     *
     * @param nextTask
     * @return
     */
    public Task clearAsync(final Task nextTask) {
        final Task task = new Task(Task.SERIAL_PRIORITY) {
            protected Object exec(final Object in) {
                //#debug
                L.i("Start Cache Clear", "ID=" + cachePriorityChar);
                synchronized (MUTEX) {
                    flashCache.clear();
                    accessOrder.removeAllElements();
                }
                //#debug
                L.i("Cache cleared", "ID=" + cachePriorityChar);

                return in;
            }
        }.setClassName("ClearAsync");
        
        return task.chain(nextTask).fork();
    }

    /**
     * Note that if the conversion in the DataTypeHandler changes due to
     * application logic requirements, you can at any time use this to force
     * as-needed re-conversion. This affects queued conversions but does not
     * affect conversions already in progress. Therefore you may want to chain()
     * to the Task returned and continue your code after this clear completes
     * after other queued tasks.
     *
     * @param chainedTask
     * @return
     */
    public Task clearHeapAsync(final Task chainedTask) {
        final Task task = new Task(Task.SERIAL_PRIORITY) {
            protected Object exec(final Object in) {
                synchronized (MUTEX) {
                    //#debug
                    L.i("Heap cached clear start", "" + cachePriorityChar);
                    ramCache.clearValues();
                    //#debug
                    L.i("Heap cached cleared", "" + cachePriorityChar);
                }

                return in;
            }
        }.setClassName("ClearHeapAsync");

        return task.chain(chainedTask).fork();
    }

    /**
     * Does this ramCache contain an object matching the key?
     *
     * @param key
     * @return
     */
    public boolean containsDigest(final byte[] digest) {
        if (digest == null) {
            throw new IllegalArgumentException("containsDigest was passed null");
        }

        boolean contained;
        synchronized (MUTEX) {
            contained = ramCache.containsKey(digest);
        }

        return contained;
    }

    /**
     * The number of items in the ramCache
     *
     * @return
     */
    public int getSize() {
        synchronized (MUTEX) {
            return this.ramCache.size();
        }
    }

    /**
     * The relative cachePriorityChar used for allocating RMS space between
     * multiple caches. Higher cachePriorityChar caches synchronousRAMCacheGet
     * more space.
     *
     * @return
     */
    public int getPriority() {
        synchronized (MUTEX) {
            return cachePriorityChar;
        }
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

//#mdebug
    /**
     * For debugging use
     *
     * @return
     */
    public String toString() {
        synchronized (MUTEX) {
            StringBuffer str = new StringBuffer();

            str.append("StaticCache --- priority: ");
            str.append(cachePriorityChar);
            str.append(" size: ");
            str.append(getSize());
            str.append(" size (bytes): ");
            str.append(sizeAsBytes);
            str.append("\n");

            for (int i = 0; i < accessOrder.size(); i++) {
                str.append(accessOrder.elementAt(i));
                str.append("\n");
            }

            return str.toString();
        }
    }
//#enddebug

    /**
     * A helper class to getDigests data asynchronously from the local ramCache
     * without attempting to getDigests data from the web.
     *
     * Usually you do not invoke this directly, but rather call
     * StaticCache.getAsync() or
     * StaticWebCache.getDigests(StaticWebCache.GET_LOCAL) to invoke this with
     * chain() support. You can use this to build your own custom asynchronous
     * background processing Task chain.
     */
    protected final class GetLocalTask extends Task {

        final int priority;

        /**
         * Create a new getDigests operation. The url will be supplied as an
         * input to this chained Task (the output of the previous Task in the
         * chain).
         *
         * @param cachePriorityChar
         */
        public GetLocalTask(final int priority) {
            super();

            this.priority = priority;
        }

        /**
         * Create a new getDigests operation, specifying the url in advance.
         *
         * @param key
         * @param cachePriorityChar
         */
        public GetLocalTask(final String key, final int priority) {
            super(key);

            this.priority = priority;
            // Better not to interrupt RMS read operation, just in case
            setShutdownBehaviour(Task.DEQUEUE_ON_SHUTDOWN);
        }

        /**
         * Complete the persistent flash storage read operation on a background
         * Worker thread. Note that although read operations are about 10x
         * faster than flash write operations, this must still be done on a
         * background thread because a slower write operation on a different
         * thread could block this read operation for a significant amount of
         * time. That would be bad for user experience it it were blocking the
         * UI thread.
         *
         * @param in
         * @return
         */
        protected Object exec(final Object in) {
            //#debug
            L.i(this, "Async StaticCache get", (String) in);
            if (in == null || !(in instanceof String)) {
                //#debug
                L.i(this, "ERROR", "Must receive a String url, but got " + in);
                cancel(false, "StaticCache.GetLocalTask got bad input to exec(): " + in);

                return in;
            }
            try {
                return synchronousGet((String) in);
            } catch (FlashDatabaseException e) {
                //#debug
                L.e(this, "Can not exec async get", in.toString(), e);
            }

            return in;
        }
    }

    /**
     * Analyze if this cache is the same one that would be returned by a call to
     * StaticCache.getCache() with the same parameters
     *
     * @param cachePriorityChar
     * @param handler
     * @param taskFactory - always null. The parameter exists for polymorphic
     * equivalency with the overriding StaticWebCache implementation.
     *
     * @return
     */
    protected boolean equals(final char priority, final DataTypeHandler handler, final Object taskFactory) {
        return this.cachePriorityChar == priority && this.handler.equals(handler);
    }
}
