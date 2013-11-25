/*
 Copyright (c) 2013 Nokia Corporation. All rights reserved.
 Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation.
 Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 affiliates. Other product and company names mentioned herein may be trademarks
 or trade names of their respective owners.

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
package org.tantalum.util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * A Hashtable the tracks access order. Each time an item is retrieved from the
 * Hashtable, it is moved to the end of a Vector of "least recently used"
 * objects.
 *
 * You can enumerate in a thread-safe manner over a snapshot of the hashtable
 * key values. Changes to the keys are not reflected in this enumeration, but
 * the associated values may or many not still be available from the hashtable
 * depending on concurrent changes while enumerating.
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
     * A non-thread-safe enumeration of the values in the hashtable, in no
     * particular order.
     *
     * Any changes to this data structure while enumerating will have
     * unpredicatable consequences.
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
