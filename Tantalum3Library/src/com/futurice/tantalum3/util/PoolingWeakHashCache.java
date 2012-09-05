/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.util;

import com.futurice.tantalum3.log.L;
import java.lang.ref.WeakReference;
import java.util.Vector;

/**
 * Objects stored in the WeakRefernce hash table may be garbage collected at any
 * time if the phone needs more memory. Explicit memory managment is also supported
 * such that any object manually remove()d from the cache is pooled and can be
 * re-used via getFromPool().
 * 
 * This class can be useful for example with procedural graphics paint acceleration
 * where an re-paintable graphics object is pooled as it goes off one edge of the
 * screen and re-used for an equal sized object appearing at the bottom of the
 * screen. If sufficient memory is available, the WeakReference hashtable also
 * allows still-valid objects that were not explicitly removed (such as those
 * still on-screen) to be re-used without repainting, but if other parts of the
 * program need more memory temporarily they can bump these cache objects out
 * of memory.
 * 
 * @author phou
 */
public class PoolingWeakHashCache extends WeakHashCache {

    private final Vector pool = new Vector();

    public void remove(final Object key) {
        synchronized (hash) {
            if (key == null) {
                //#debug
                L.i("PoolingWeakHashCache", "remove() with null key");
                return;
            }
            final WeakReference wr = (WeakReference) hash.get(key);

            if (wr != null) {
                hash.remove(key);
                if (wr.get() != null) {
                    //#debug
                    L.i("Adding to pool", key.toString());
                    pool.addElement(wr);
                }
            }
        }
    }

    public Object getFromPool() {
        synchronized (hash) {
            Object o = null;
            WeakReference wr;

            while (pool.size() > 0) {
                wr = (WeakReference) pool.firstElement();
                pool.removeElementAt(0);
                o = wr.get();
                if (o != null) {
                    break;
                }
            }

            return o;
        }
    }

    public void clear() {
        synchronized (hash) {
            super.clear();

            pool.removeAllElements();
        }
    }
}
