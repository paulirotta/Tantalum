/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2;

import java.lang.ref.WeakReference;
import java.util.Hashtable;

/**
 *
 * @author phou
 */
public class WeakHashCache {

    private final Hashtable hash = new Hashtable();

    public final Object get(String key) {
        synchronized (hash) {
            Object o = null;
            final WeakReference reference = (WeakReference) hash.get(key);

            if (reference != null) {
                o = reference.get();
                if (o == null) {
                    remove(key);
                }
            }

            return o;
        }
    }

    public final void put(String key, Object value) {
        hash.put(key, new WeakReference(value));
    }

    public void remove(String key) {
        hash.remove(key);
    }
}
