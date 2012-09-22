/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.util;

import java.util.Vector;

/**
 *
 * @author phou
 */
public abstract class LengthLimitedVector extends Vector {

    private int maxLength;

    public LengthLimitedVector(final int maxLength) {
        super(maxLength + 1);

        this.maxLength = maxLength;
    }

    /**
     * You decide what to do with the extra object each time length is exceeded
     */
    protected abstract void lengthExceeded(Object extra);

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

    public synchronized void setMaxLength(final int maxLength) {
        this.maxLength = maxLength;
        while (size() > maxLength) {
            final Object extra = firstElement();
            removeElementAt(0);
            lengthExceeded(extra);
        }
    }
}
