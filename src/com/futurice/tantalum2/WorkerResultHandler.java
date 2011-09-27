package com.futurice.tantalum2;

import com.futurice.tantalum2.rms.StaticCache;

/**
 * This is a basic asynchronous response mechanism for processing data on a
 * Worker thread. An operation which returns a result can return it at a later
 * time, and after this execute custom code in run().
 * 
 * Most implementations extend the convenient DefaultResultHandler implementation
 * of this interface.
 * 
 * @author pahought
 */
public interface WorkerResultHandler extends Workable {

    public void setResult(Object result);
    
    public Object getResult();
    
    public void setCache(StaticCache cache);
    
    /**
     * Override this for custom exception handling when there is a network error
     *
     * @param e
     */
    public void exception(Exception e);
}
