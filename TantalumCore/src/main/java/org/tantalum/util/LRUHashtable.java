/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author phou
 */
public class LRUHashtable {

    private final Hashtable hash;
    private final LRUVector v;

    public LRUHashtable() {
        hash = new Hashtable();
        v = new LRUVector();
    }

    public LRUHashtable(final int size) {
        hash = new Hashtable(size);
        v = new LRUVector(size);
    }

    public synchronized Object get(final Object key) {
        final Object value = hash.get(key);

        if (value != null) {
            v.addElement(key);
        }

        return value;
    }

    public synchronized void put(final Object key, final Object value) {
        hash.put(key, value);
        v.addElement(key);
    }

    public synchronized boolean containsKey(final Object key) {
        return hash.containsKey(key);
    }

    public synchronized boolean contains(final Object value) {
        return hash.contains(value);
    }

    public synchronized Object remove(final Object key) {
        final Object value = hash.remove(key);

        if (value != null) {
            v.removeElement(key);
        }

        return value;
    }

    /**
     * An enumeration of a current snapshot of the LRUHashtable contents, in
     * least-recently-used-first order.
     *
     * You can continue to make changes to the LRUHashtable while using the
     * enumeration. These changes will not be reflected in the enumeration
     * contents or order.
     *
     * All objects in the LRUHashtable will continue to be hard referenced in
     * RAM until the Enumeration is garbage collected. If the LRUHashtable
     * contains weak reference objects, the 2nd-degree referenced object will of
     * course not be hard referenced.
     *
     * @return
     */
    public synchronized Enumeration keys() {
        final int n = v.size();
        final Vector v2 = new Vector(n);

        for (int i = 0; i < n; i++) {
            v2.addElement(v.elementAt(i));
        }

        return v2.elements();
    }

    /**
     * A non-thread-safe enumeration of the values in the hashtable, in no particular order.
     * 
     * Any changes to this data structure while enumerating will have unpredicatable consequences.
     * 
     * @return 
     */
    public synchronized Enumeration elements() {
        return hash.elements();
    }

    public synchronized int size() {
        return hash.size();
    }

    public synchronized void clear() {
        hash.clear();
        v.removeAllElements();
    }
}
