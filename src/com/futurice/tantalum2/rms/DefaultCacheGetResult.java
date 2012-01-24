/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2.rms;

import com.futurice.tantalum2.rms.CacheGetResult;

/**
 * A convenient and common implementation of the cache get result as an Object.
 * 
 * @author tsaa
 */
public class DefaultCacheGetResult implements CacheGetResult {
    
    private Object result;

    /**
     * Nothing is done on the EDT by the default implementation. You may want
     * to override this.
     * 
     */
    public void run() {
    }
    
    public Object getResult() {
        return result;
    }
    
    public void setResult(Object result) {
        this.result = result;
    }
    
}
