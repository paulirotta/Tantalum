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
package org.tantalum.util;

import java.util.Vector;

/**
 * Least recently used list of object
 *
 * Each object can be in the Vector a maximum of one time. Elements are always
 * added to the end of the Vector so the oldest object is at position 0.
 *
 * @author phou
 */
public class LRUVector extends Vector {

    /**
     * Create a new least-recently-used collection
     */
    public LRUVector() {
        super();
    }

    /**
     * Create a new least-recently-used collection, specifying the maximum size
     *
     * @param length
     */
    public LRUVector(final int length) {
        super(length);
    }

    /**
     * Add one element. If the element is already in the collection, it will be
     * moved to the beginning (position 0).
     *
     * @param o
     */
    public synchronized void addElement(Object o) {
        removeElement(o);
        super.addElement(o);
    }

    /**
     * Explicitly setting an element at a position is not allowed in an
     * automatically-sorted collection like this. This will always through
     * IllegalArgumentException.
     *
     * @param o
     * @param index
     */
    public void setElementAt(Object o, int index) {
        throw new IllegalArgumentException("setElementAt() not allowed on LRUVector");
    }

    /**
     * Explicitly adding an element at a position is not allowed in an
     * automatically-sorted collection like this. This will always through
     * IllegalArgumentException.
     *
     * @param o
     * @param index
     */
    public void insertElementAt(Object o, int index) {
        throw new IllegalArgumentException("insertElementAt() not allowed on LRUVector");
    }

    /**
     * Remove the element least recently accessed in the collection, thereby
     * shrinking the size.
     *
     * @return
     */
    public synchronized Object removeLeastRecentlyUsed() {
        Object o = null;

        if (size() > 0) {
            o = firstElement();
            removeElementAt(0);
        }

        return o;
    }

    /**
     * Check if the object is in the collection.
     *
     * Note that this operation will also shift the object to position 0 as it
     * counts as "use" and thus possible relevancy for access in
     * least-recently-used algorithms.
     *
     * @param o
     * @return
     */
    public synchronized boolean contains(Object o) {
        final boolean contained = super.contains(o);

        if (contained) {
            // Shift to be LRU
            addElement(o);
        }

        return contained;
    }
}
