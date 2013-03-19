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
 * A collection which continuously maintains sort order
 *
 * @author phou
 */
public class SortedVector extends Vector {

    private final Comparator comparator;

    /**
     * Create a new sorted collection
     *
     * @param comparator
     */
    public SortedVector(final Comparator comparator) {
        this.comparator = comparator;
    }

    /**
     * Add one element to the continuously-sorted collection
     *
     * @param o
     */
    public synchronized void addElement(final Object o) {
        int insertIndex = size(); /* Future index of o in the SortedVector? */
        /* Finding the index */
        for (int i = size() - 1; i >= 0; i--) {
            if (comparator.before(o, elementAt(i))) {
                insertIndex--;
            } else {
                break;
            }
        }

        /* Inserting the object */
        if (insertIndex == size()) {
            super.addElement(o);
        } else {
            super.insertElementAt(o, insertIndex);
        }
    }

    /**
     * Add an element at a specified location is not allowed.
     *
     * This method will always return IllegalArgumentException
     *
     * @param o
     * @param index
     */
    public final void insertElementAt(Object o, int index) {
        throw new IllegalArgumentException("insertElementAt() not supported");
    }

    /**
     * Replace an element at a specified location is not allowed.
     *
     * This method will always return IllegalArgumentException
     *
     * @param o
     * @param index
     */
    public final void setElementAt(Object o, int index) {
        throw new IllegalArgumentException("setElementAt() not supported");
    }

    /**
     * Test for equivalence including having an equivalent Comparator
     *
     * @param o
     * @return
     */
    public boolean equals(Object o) {
        return super.equals(o) && o instanceof SortedVector && this.comparator.equals(((SortedVector) o).comparator);
    }

    /**
     * Generate a hash based on the underlying Vector hash
     *
     * @return
     */
    public int hashCode() {
        return super.hashCode() ^ comparator.hashCode();
    }

    /**
     * A helper class to indicate the sort order for the Vector with the given
     * current data type
     *
     */
    public abstract static class Comparator {

        /**
         * Return true of o1 should be before o1 in the ordering imposed by this
         * Comparator
         *
         * @param o1
         * @param o2
         * @return
         */
        public abstract boolean before(Object o1, Object o2);
    }
}
