/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.util;

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
    public LRUVector() {
        super();
    }
    
    public LRUVector(final int length) {
        super(length);
    }
    
    public synchronized void addElement(Object o) {
        removeElement(o);
        super.addElement(o);
    }
    
    public void setElementAt(Object o, int index) {
        throw new IllegalArgumentException("setElementAt() not allowed on LRUVector");
    }
    
    public void insertElementAt(Object o, int index) {
        throw new IllegalArgumentException("insertElementAt() not allowed on LRUVector");
    }
    
    public synchronized Object removeLeastRecentlyUsed() {
        Object o = null;
        
        if (size() > 0) {
            o = firstElement();
            removeElementAt(0);
        }
        
        return o;
    }
    
    public synchronized boolean contains(Object o) {
        final boolean contained = super.contains(o);
        
        if (contained) {
            // Shift to be LRU
            addElement(o);
        }
        
        return contained;
    }
}
