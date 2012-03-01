/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2;

import com.futurice.tantalum2.Result;

/**
 * A convenient and common implementation of the cache and http get result as an Object.
 * 
 * @author tsaa
 */
public class DefaultResult implements Result {
    
    private volatile Object result; // volatile is important, this result may be accessed by multiple threads

    /**
     * Nothing is done on the EDT by the default implementation. You may want
     * to override this to also implement Runnable since all Tantalum routines
     * which return a Result will honour this and also queue the object to the
     * EDT.
     * 
     */
    public void run() {
    }
    
    public Object getResult() {
        return result;
    }
    
    /**
     * This is called by the routine returning a result, from a Worker thread and
     * before run().
     * 
     * @param result 
     */
    public void setResult(Object result) {
        this.result = result;
    }
}
