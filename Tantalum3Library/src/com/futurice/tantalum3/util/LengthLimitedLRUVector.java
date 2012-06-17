/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.util;

/**
 *
 * @author phou
 */
public abstract class LengthLimitedLRUVector extends LRUVector {
    private int maxLength;

    public LengthLimitedLRUVector(final int maxLength) {
        super(maxLength + 1);

        this.maxLength = maxLength;
    }
    
    /**
     * You _must_ remove the removeLeastRecentlyUsed() object when length is exceeded
     */
    protected abstract void lengthExceeded();

    public void addElement(final Object o) {
        if (maxLength == 0) {
            throw new IllegalArgumentException("Max length of LRU vector is currently 0, can not add: " + o);
        }
        
        super.addElement(o);

        if (size() > maxLength) {
            lengthExceeded();
        }
    }
    
    public final boolean isLengthExceeded() {
        return size() > maxLength;
    }
    
    public void setMaxLength(final int maxLength) {
        this.maxLength = maxLength;
        if (size() > maxLength) {
            lengthExceeded();
        }
    }
}
