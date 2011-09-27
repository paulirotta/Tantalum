/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2;

import com.futurice.tantalum2.rms.CacheGetResult;

/**
 *
 * @author tsaa
 */
public class DefaultCacheGetResult implements CacheGetResult {
    
    private Object result;
    
    public void run() {
        
    }
    
    public Object getResult() {
        return result;
    }
    
    public void setResult(Object result) {
        this.result = result;
    }
    
}
