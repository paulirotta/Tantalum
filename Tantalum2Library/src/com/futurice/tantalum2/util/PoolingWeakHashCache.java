/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2.util;

import com.futurice.tantalum2.util.WeakHashCache;
import com.futurice.tantalum2.log.Log;
import java.lang.ref.WeakReference;
import java.util.Vector;

/**
 *
 * @author phou
 */
public final class PoolingWeakHashCache extends WeakHashCache {

    private final Vector pool = new Vector();

    public void remove(final Object key) {
        if (key == null) {
            Log.l.log("PoolingWeakHashCache", "remove() with null key");
            return;
        }
        synchronized (hash) {
            final WeakReference wr = (WeakReference) hash.get(key);

            if (wr != null) {
                hash.remove(key);
                if (wr.get() != null) {
                    Log.l.log("Adding to pool", key.toString());
                    pool.addElement(wr);
                }
            }
        }
    }

    public Object getFromPool() {
        synchronized (pool) {
            Object o = null;
            WeakReference wr;

            while (pool.size() > 0) {
                wr = (WeakReference) pool.elementAt(0);
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
        super.clear();

        pool.removeAllElements();
    }
}
