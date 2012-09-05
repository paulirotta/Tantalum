/**
 * A weak reference cache for keeping images in a RAM hashtable with automatic
 * garbage collection as needed by the virtual machine.
 *
 */
package com.futurice.tantalum3.util;

import com.futurice.tantalum3.log.L;
import java.lang.ref.WeakReference;
import java.util.Hashtable;

/**
 * This is a hashtable which acts as a RAM cache.
 *
 * Objects in the hashtable are not held in memory, they may be garbage
 * collected at any time, in which case the calling routine must do something
 * else to recreate the value. You can freely put as many objects in this cache
 * as you like, but do not store things of no interest as the objects will
 * displace more useful data.
 *
 * If an object is garbage collected, it will not be removed from the hash cache
 * automatically, so your app can count on this is a stable list of objects. You
 * may choose to manually remove references which will no longer be of interest
 * according to application logic.
 *
 * Since you do not have any control over which weak references are destroyed,
 * your application should be prepared to re-create data stored in this cache
 * without too much cost to user responsiveness.
 *
 * Placing a null object into the cache is the same as removing it- null values
 * are not stored as useful.
 *
 * @author phou
 */
public class WeakHashCache {

    protected final Hashtable hash = new Hashtable();

    public Object get(final Object key) {
        final WeakReference reference = (WeakReference) hash.get(key);

        if (reference != null) {
            return reference.get();
        }

        return null;
    }

    public void put(final Object key, final Object value) {
        synchronized (hash) {
            if (key == null) {
                //#debug
                L.i("WeakHash put", "key is null");
                return;
            }
            if (value == null) {
                //#debug
                L.i("WeakHash put", "value is null, key removed");
                hash.remove(key);
                return;
            }
            hash.put(key, new WeakReference(value));
        }
    }

    public void remove(final Object key) {
        if (key != null) {
            hash.remove(key);
        } else {
            //#debug
            L.i("WeakHashCache", "remove() with null key");
        }
    }

    public boolean containsKey(final Object key) {
        if (key != null) {
            return hash.containsKey(key);
        } else {
            //#debug
            L.i("WeakHashCache", "containsKey() with null key");
            return false;
        }
    }

    public int size() {
        return hash.size();
    }

    public void clear() {
        hash.clear();
    }
}
