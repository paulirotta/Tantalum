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
 * A collection which calls lengthExceeded() when the designated maximum size is
 * reached. Your implementation can use this to limit growth by trimming the
 * collection.
 *
 * @author phou
 */
public abstract class LengthLimitedVector extends Vector {

    private int maxLength;

    /**
     * Create a new limited-size collection, specifying the maximum allowed
     * size.
     *
     * @param maxLength
     */
    public LengthLimitedVector(final int maxLength) {
        super(maxLength + 1);

        this.maxLength = maxLength;
    }

    /**
     * You decide what to do with the extra object each time the maximum
     * allowable length is exceeded
     *
     * @param extra
     */
    protected abstract void lengthExceeded(Object extra);

    /**
     * Add one element to the collection
     *
     * @param o
     */
    public synchronized void addElement(final Object o) {
        if (maxLength == 0) {
            throw new IllegalArgumentException("Max length of LRU vector is currently 0, can not add: " + o);
        }

        super.addElement(o);

        if (size() > maxLength) {
            final Object extra = firstElement();
            removeElementAt(0);
            lengthExceeded(extra);
        }
    }

    /**
     * Indicates if the collection is larger than expected
     *
     * @return
     */
    public synchronized final boolean isLengthExceeded() {
        return size() > maxLength;
    }

    /**
     * Remove the object and call lengthExceeded() on it, treating it as if the
     * Vector has outgrown its bounds.
     *
     * @param o
     * @return
     */
    public boolean markAsExtra(final Object o) {
        Object extra = null;

        synchronized (this) {
            final int index = this.indexOf(o);

            if (index >= 0) {
                extra = elementAt(index);
                removeElementAt(index);
            }
        }
        if (extra != null) {
            lengthExceeded(extra);
        }

        return extra != null;
    }

    /**
     * Change the maximum allowed size for the collection
     *
     * @param maxLength
     */
    public synchronized void setMaxLength(final int maxLength) {
        this.maxLength = maxLength;
        while (size() > maxLength) {
            final Object extra = firstElement();
            removeElementAt(0);
            lengthExceeded(extra);
        }
    }
}
