/**
 * A weak reference cache for keeping images in a RAM hashtable with automatic
 * garbage collection as needed by the virtual machine.
 * 
 */
package com.futurice.tantalum2;

import java.lang.ref.WeakReference;
import java.util.Hashtable;

/**
 *
 * @author phou
 */
public final class WeakHashCache {

    private final Hashtable hash = new Hashtable();

    public Object get(String key) {
        synchronized (hash) {
            Object o = null;
            final WeakReference reference = (WeakReference) hash.get(key);

            if (reference != null) {
                o = reference.get();
            }

            return o;
        }
    }

    public void put(String key, Object value) {
        hash.put(key, new WeakReference(value));
    }

    public void remove(String key) {
        hash.remove(key);
    }
    
    public boolean containsKey(String key) {
        return hash.containsKey(key);
    }
    
    public int size() {
        return hash.size();
    }
}
