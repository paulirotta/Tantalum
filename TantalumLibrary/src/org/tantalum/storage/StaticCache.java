/*
 Copyright © 2012 Paul Houghton and Futurice on behalf of the Tantalum Project.
 All rights reserved.

 Tantalum software shall be used to make the world a better place for everyone.

 This software is licensed for use under the Apache 2 open source software license,
 http://www.apache.org/licenses/LICENSE-2.0.html

 You are kindly requested to return your improvements to this library to the
 open source community at http://projects.developer.nokia.com/Tantalum

 The above copyright and license notice notice shall be included in all copies
 or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package org.tantalum.storage;

import java.util.Vector;
import org.tantalum.PlatformUtils;
import org.tantalum.Task;
import org.tantalum.Worker;
import org.tantalum.net.StaticWebCache;
import org.tantalum.util.L;
import org.tantalum.util.LRUVector;
import org.tantalum.util.SortedVector;
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
    /*
     * The number of the Worker thread which will perform all cache write
     * operations. Reading is asynchronous from any thread and this works because
     * the WeakReferenceHash cache is locked from garbage-collecting in-memory
     * copies of write-queued objects until after the write Task completes, thus
     * out-of-order read is thread safe. But flash writing must be done in-order
     * to preserve the application designer's intent without thread race
     * conditions arising.
     */

    private static final int RMS_WORKER_INDEX = Worker.nextSerialWorkerIndex();
    /*
     * A list of all caches, sorted by priority order (lowest char first)
     */
    protected static final SortedVector caches = new SortedVector(new SortedVector.Comparator() {
        public boolean before(final Object o1, final Object o2) {
            return ((StaticCache) o1).priority < ((StaticCache) o2).priority;
        }
    });
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
     * character values indicate lower priority caches which will be garage
     * collected first when flash memory is low, so use larger characters for
     * more transient or less-important-to-persist data.
     */
    protected final char priority;
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
     */
    private volatile boolean flashCacheEnabled = true;
    /*
     * All sychronization is not on "this", but on the MUTEX Object to encapsulate
     * synch and dis-allow the bad practice of externally sychronizing on the
     * ramCache object itself.
     */
    protected final Object MUTEX = new Object();

    /**
     * Get the previously-created cache with the same parameters
     *
     * @param priority
     * @param handler
     * @return
     */
    protected static StaticCache getExistingCache(final char priority, final DataTypeHandler handler, final Object taskFactory) {
        synchronized (caches) {
            for (int i = 0; i < caches.size(); i++) {
                final StaticCache c = (StaticCache) caches.elementAt(i);

                if (c.equals(priority, handler, taskFactory)) {
                    throw new IllegalArgumentException("A cache with priority=" + priority + " already exists, but DataTypeHandler and/or HttpTaskFactory are now equal to your factory request");
                }

                return c;
            }

            return null;
        }

    }

    /**
     * Get a named Cache
     *
     * Caches with higher priority are more likely to keep their data when space
     * is limited.
     *
     * You will get IllegalArgumentException if you call this multiple times for
     * the same cache priority but with a different (not .equals())
     * DataTypeHandler.
     *
     * @param priority - a character from '0' to '9', higher numbers get a
     * preference for space. Letters are also allowed.
     * @param handler - a routine to convert from byte[] to Object form when
     * loading into the RAM ramCache.
     * @return
     */
    public static synchronized StaticCache getCache(final char priority, final DataTypeHandler handler) {
        StaticCache c = getExistingCache(priority, handler, null);

        if (c == null) {
            c = new StaticCache(priority, handler);
        }

        return c;
    }

    /**
     * Create a named Cache
     *
     */
    protected StaticCache(final char priority, final DataTypeHandler handler) {
        if (priority < '0') {
            throw new IllegalArgumentException("Priority=" + priority + " is invalid, must be '0' or higher");
        }
        caches.addElement(this);
        this.priority = priority;
        this.handler = handler;
        flashCache = PlatformUtils.getInstance().getFlashCache(priority);
        init();
    }

    /**
     * Load the keys from flash memory
     *
     * We want to use the RAM Hashtable to know what the ramCache contains, even
     * though we do not pre-load from flash all the values
     */
    private void init() {
        try {
            Vector keys = flashCache.getKeys();
            for (int i = 0; i < keys.size(); i++) {
                String key = (String) keys.elementAt(i);
                this.ramCache.put(key, null);
            }
        } catch (FlashDatabaseException ex) {
            //#debug
            L.e("Can not load keys to RAM during init() cache", "cache priority=" + priority, ex);
        }
    }

    /**
     * Turn off persistent local storage use for read and write to see what
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
    protected Object convertAndPutToHeapCache(final String key, final byte[] bytes) {
        //#debug
        L.i("Start to convert", key + " bytes length=" + bytes.length);
        final Object o = handler.convertToUseForm(bytes);

        synchronized (MUTEX) {
            accessOrder.addElement(key);
            ramCache.put(key, o);
        }
        //#debug
        L.i("End convert", key);

        return o;
    }

    /**
     * Synchronously return the hash object
     *
     * @param key
     * @return
     */
    public Object synchronousRAMCacheGet(final String key) {
        synchronized (MUTEX) {
            Object o = ramCache.get(key);

            if (o != null) {
                //#debug            
                L.i("Possible StaticCache hit in RAM (might be expired WeakReference)", key);
                this.accessOrder.addElement(key);
            }

            return o;
        }
    }

    /**
     * Retrieve an object from RAM or RMS storage.
     *
     * @param key
     * @param getPriority - default is Work.NORMAL_PRIORITY set to
     * Work.HIGH_PRIORITY if you want the results quickly to update the UI.
     * @return
     */
    public Task getAsync(final String key, final int getPriority, final Task chainedTask) {
        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException("Trivial StaticCache get");
        }

        final Task task = new GetLocalTask(key);
        task.chain(chainedTask);
        task.fork(getPriority);

        return task;
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
        L.i("StaticCache RAM result", "(" + priority + ") " + key + " : " + o);
        if (o == null) {
            // Load from flash memory
            final byte[] bytes;
            if (flashCacheEnabled) {
                bytes = flashCache.getData(key);
            } else {
                bytes = null;
            }

            //#debug
            L.i("StaticCache flash intermediate result", "(" + priority + ") " + key + " : " + bytes);
            if (bytes != null) {
                //#debug
                L.i("StaticCache hit in flash", "(" + priority + ") " + key);
                o = convertAndPutToHeapCache(key, bytes);
                //#debug
                L.i("StaticCache flash result", "(" + priority + ") " + key + " : " + o);
            }
        }

        return o;
    }

    /**
     * Store a value to heap and flash memory.
     *
     * Note that the storage to RMS is done asynchronously in the background
     * which may lead to large binary objects being queued up on the Worker
     * thread. If you do this many times, you could run short on memory, and
     * should re-factor with use of synchronousPutToRMS() instead.
     *
     * Note that conversion to use form happens immediately and synchronously on
     * the calling thread before before this method returns. If conversion may
     * take a long time (XML parsing, etc) then consider not calling this from
     * the user event dispatch thread.
     *
     * @param key
     * @param bytes
     * @return the byte[] converted to use form by the ramCache's Handler
     */
    public Object putAsync(final String key, final byte[] bytes) {
        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException("Attempt to put trivial key to cache");
        }
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Attempt to put trivial bytes to cache: key=" + key);
        }
        if (flashCacheEnabled) {
            Worker.forkSerial(new Task() {
                public Object exec(final Object in) {
                    try {
                        synchronousFlashPut(key, bytes);
                    } catch (Exception e) {
                        //#debug
                        L.e("Can not synch write to RMS", key, e);
                    }

                    return in;
                }
            }, RMS_WORKER_INDEX);
        }

        return convertAndPutToHeapCache(key, bytes);
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
    private void synchronousFlashPut(final String key, final byte[] bytes) {
        if (key == null) {
            throw new IllegalArgumentException("Null key put to cache");
        }
        try {
            do {
                try {
                    //#debug
                    L.i("RMS cache write start", key + " (" + bytes.length + " bytes)");
                    flashCache.putData(key, bytes);
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
            } while (true);
        } catch (Exception e) {
            //#debug
            L.e("Couldn't store object to RMS", key, e);
        }
    }

    /**
     * Remove unused and then currently used items from the RMS ramCache to make
     * room for new items.
     *
     * @param minSpaceToClear - in bytes
     * @return true if the requested amount of space has been cleared
     */
    private boolean clearSpace(final int minSpaceToClear) throws FlashDatabaseException {
        int spaceCleared = 0;
        //FIXME Not appropriate for Android. Why is there no compilation bad reference error?
        final Vector rsv = flashCache.getKeys();

        //#debug
        L.i("Clearing RMS space", minSpaceToClear + " bytes");

        // First: clear cached objects not currently appearing in any open ramCache
        for (int i = rsv.size() - 1; i >= 0; i--) {
            final String key = (String) rsv.elementAt(i);
            final StaticCache sc = getCacheContainingKey(key);

            if (sc != null) {
                spaceCleared += getByteSizeByKey(key);
                sc.remove(key);
            }
        }
        //#debug
        L.i("End phase 1: clearing RMS space", spaceCleared + " bytes recovered");

        // Second: remove currently cached items, first from low priority caches
        while (spaceCleared < minSpaceToClear && rsv.size() > 0) {
            for (int i = 0; i < caches.size(); i++) {
                final StaticCache sc = (StaticCache) caches.elementAt(i);

                while (!sc.accessOrder.isEmpty() && spaceCleared < minSpaceToClear) {
                    final String key = (String) sc.accessOrder.removeLeastRecentlyUsed();
                    spaceCleared += getByteSizeByKey(key);
                    sc.remove(key);
                }
            }
        }
        //#debug
        L.i("End phase 2: clearing RMS space", spaceCleared + " bytes recovered (total)");

        return spaceCleared >= minSpaceToClear;
    }

    private int getByteSizeByKey(final String key) throws FlashDatabaseException {
        int size = 0;

        final byte[] bytes = flashCache.getData(key);
        if (bytes != null) {
            size = bytes.length;
        } else {
            //#debug
            L.i("Can not check size of record store to clear space", key);
        }

        return size;
    }

    private static StaticCache getCacheContainingKey(String key) {
        StaticCache cache = null;

        synchronized (caches) {
            for (int i = 0; i < caches.size(); i++) {
                final StaticCache currentCache = (StaticCache) caches.elementAt(i);
                if (currentCache.containsKey(key)) {
                    cache = currentCache;
                    break;
                }
            }
        }

        return cache;
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
                synchronized (MUTEX) {
                    accessOrder.removeElement(key);
                    ramCache.remove(key);
                }
                flashCache.removeData(key);
                //#debug
                L.i("Cache remove (from RAM and RMS)", key);
            }
        } catch (Exception e) {
            //#debug
            L.e("Couldn't remove object from cache", key, e);
        }
    }

    /**
     * Remove all elements from this ramCache
     *
     */
    public void clear() {
        //#debug
        L.i("Start Cache Clear", "ID=" + priority);
        final String[] keys;

        synchronized (MUTEX) {
            keys = new String[accessOrder.size()];
            accessOrder.copyInto(keys);
        }
        for (int i = 0; i < keys.length; i++) {
            remove(keys[i]);
        }
        //#debug
        L.i("Cache cleared", "ID=" + priority);
    }

    /**
     * Note that if the conversion in the DataTypeHandler changes due to
     * application logic requirements, you can at any time use this to force
     * as-needed re-conversion. This affects queued conversions but does not
     * affect conversions already in progress. Therefore you may want to chain()
     * to the Task returned and continue your code after this clear completes
     * after other queued tasks.
     */
    public Task clearHeapCacheAsync(final Task chainedTask) {
        final Task task = new Task() {
            protected Object exec(final Object in) {
                synchronized (MUTEX) {
                    //#debug
                    L.i("Heap cached clear start", "" + priority);
                    ramCache.clear();
                    init();
                    //#debug
                    L.i("Heap cached cleared", "" + priority);
                }

                return in;
            }
        };
        task.chain(chainedTask);
        task.fork();

        return task;
    }

    /**
     * Does this ramCache contain an object matching the key?
     *
     * @param key
     * @return
     */
    public boolean containsKey(final String key) {
        if (key == null) {
            throw new IllegalArgumentException("containsKey was passed null");
        }

        boolean contained;
        synchronized (MUTEX) {
            contained = ramCache.containsKey(key);
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
     * The relative priority used for allocating RMS space between multiple
     * caches. Higher priority caches synchronousRAMCacheGet more space.
     *
     * @return
     */
    public int getPriority() {
        synchronized (MUTEX) {
            return priority;
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

    /**
     * Get a Vector of all keys for key-value pairs currently in the local
     * ramCache at the flash memory level. The value may or may not be in heap
     * memory at the current time.
     *
     * @return
     */
    public Task getKeysAsync(final Task chainedTask) {
        final Task task = new Task() {
            protected Object exec(Object in) {
                try {
                    return flashCache.getKeys();
                } catch (Exception e) {
                    //#debug
                    L.e("Can not complete", "getKeysTask", e);
                    this.setStatus(Task.CANCELED);
                    flashCache.clear();

                    return new Vector();
                }
            }
        };
        task.chain(chainedTask);
        task.fork();

        return task;
    }

    /**
     * For debugging use
     *
     * @return
     */
    public String toString() {
        synchronized (MUTEX) {
            StringBuffer str = new StringBuffer();

            str.append("StaticCache --- priority: ");
            str.append(priority);
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

    /**
     * A helper class to get data asynchronously from the local ramCache without
     * attempting to get data from the web.
     *
     * Usually you do not invoke this directly, but rather call
     * StaticCache.getAsync() or StaticWebCache.get(StaticWebCache.GET_LOCAL) to
     * invoke this with chain() support. You can use this to build your own
     * custom asynchronous background processing Task chain.
     */
    protected final class GetLocalTask extends Task {

        /**
         * Create a new get operation. The url will be supplied as an input to
         * this chained Task (the output of the previous Task in the chain).
         */
        public GetLocalTask() {
            super();
        }

        /**
         * Create a new get operation, specifying the url in advance.
         *
         * @param key
         */
        public GetLocalTask(final String key) {
            super(key);
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
            L.i("Async StaticCache get", (String) in);
            if (in == null || !(in instanceof String)) {
                L.i("ERROR", "StaticCache.GetLocalTask must receive a String url, but got " + in == null ? "null" : in.toString());
                cancel(false);

                return in;
            }
            try {
                return synchronousGet((String) in);
            } catch (Exception e) {
                //#debug
                L.e("Can not async StaticCache get", in.toString(), e);
                this.cancel(false);
            }

            return in;
        }
    }

    protected boolean equals(final char priority, final DataTypeHandler handler, final Object taskFactory) {
        return this.priority == priority && this.handler.equals(handler);
    }
}
