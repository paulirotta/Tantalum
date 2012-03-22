/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2.util;

/**
 *
 * @author phou
 */
public abstract class LengthLimitedLRUVector extends LRUVector {
    private final int maxLength;

    public LengthLimitedLRUVector(final int maxLength) {
        super(maxLength + 1);

        this.maxLength = maxLength;
    }
    
    protected abstract void lengthExceeded();

    public synchronized void addElement(Object o) {
        super.addElement(o);

        if (size() > maxLength) {
            lengthExceeded();
        }
    }
}
