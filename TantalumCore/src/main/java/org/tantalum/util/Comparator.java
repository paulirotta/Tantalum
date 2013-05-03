/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.util;

/**
 * A helper to indicate the sort order for a SortedVector or other collection
 * with the given current data type
 *
 * @author phou
 */
public interface Comparator {

    /**
     * Return a number less than zero, zero, or more than zero. Less than zero
     * if o1 is before o2 in the ordering imposed.
     * 
     * Much like "o1 minus o2" for simple cases.
     *
     * @param o1
     * @param o2
     * @return
     */
    public abstract int compare(Object o1, Object o2);
}
