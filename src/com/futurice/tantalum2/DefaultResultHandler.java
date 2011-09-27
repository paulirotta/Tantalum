/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2;

import com.futurice.tantalum2.rms.StaticCache;

/**
 * Convenient implementation of the ResultHandler interface
 * 
 * @author tsaa
 */
public abstract class DefaultResultHandler implements ResultHandler {
    protected Object result;
    protected StaticCache cache;
    
    public void exception(Exception e) {
        Log.logThrowable(e, "DefaultResultHandler");
    }

    public Object getResult() {
        return result;
    }
    
    public StaticCache getCache() {
        return cache;
    }
    
    public void setCache(StaticCache cache) {
        this.cache = cache;
    }

    public abstract boolean work();

    public void setResult(Object result) {
        this.result = result;
    }
}
