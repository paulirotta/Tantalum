/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.futurice.tantalum3;

/**
 *
 * @author phou
 */
public class AsyncResult {
    protected Object result;
    
    /**
     * You can call this at the end of your overriding method once you have set
     * the result.
     *
     * If you implement set(), do not implement or use exec()
     *
     * @param o
     */
    public synchronized void set(final Object o) {
        this.result = o;
    }
    
    public synchronized void cancel() {
    }
    
    public synchronized Object get() {
        return result;
    }
}
