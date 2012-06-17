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
public class SortedVector extends Vector {
    private final Comparator comparator;
    
    public SortedVector(final Comparator comparator) {
        this.comparator = comparator;
    }

    public synchronized void addElement(final Object o) {
        int i;

        for (i = size() - 1; i >= 0; i--) {
            if (comparator.before(o, elementAt(i))) {
                break;
            }
        }

        if (i < 0) {
            super.addElement(o);
        } else {
            super.insertElementAt(o, i);
        }
    }

    public final void insertElementAt(Object o, int index) {
        throw new IllegalArgumentException("insertElementAt() not supported");
    }

    public final void setElementAt(Object o, int index) {
        throw new IllegalArgumentException("setElementAt() not supported");
    }

    /**
     * Indicate the sort order for the Vector with the given current data type
     * 
     */
    public abstract static class Comparator {

        public abstract boolean before(Object o1, Object o2);
    }
}
