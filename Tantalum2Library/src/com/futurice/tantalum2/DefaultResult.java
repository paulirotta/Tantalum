/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2;

/**
 * A convenient and common implementation of the cache and http get result as an Object.
 * 
 * @author tsaa
 */
public class DefaultResult implements Result {    
    private Object o; // volatile is important, this result may be accessed by multiple threads
    private Result prepended;

    /**
     * Nothing is done on the EDT by the default implementation. You may want
     * to override this to also implement Runnable since all Tantalum routines
     * which return a Result will honour this and also queue the object to the
     * EDT.
     * 
     * When you override run(), call super.run() as the first action.
     */
    public synchronized void run() {
        if (prepended != null) {
            prepended.run();
        }
    }
    
    public synchronized Object getResult() {
        return o;
    }
    
    /**
     * This is called by the routine returning a result, from a Worker thread and
     * before run().
     * 
     * @param o - the object returned from asynchronous Worker work
     */
    public synchronized void setResult(final Object o, final boolean queueToEDTnow) {
        this.o = o;
        if (prepended != null) {
            prepended.setResult(o, false);
        }
        if (queueToEDTnow) {
            Worker.queueEDT(this);
        }
    }

    /**
     * This is called from the Worker thread by the routine returning a result.
     * It indicates that no normal, meaningful result can be given at this time,
     * for example there was a network error, or data is not yet available in the
     * cache, etc.
     * 
     */
    public synchronized void noResult() {
        if (prepended != null) {
            prepended.noResult();
        }
    }

    /**
     * Add another Result which will be notified on the Worker thread before the
     * present Result.
     * 
     * @param prepended
     * @return 
     */
    public synchronized Result prepend(final Result prepended) {
        if (this.prepended == null) {
            this.prepended = prepended;
        } else {
            this.prepended.prepend(prepended);
        }
        
        return this;
    }
}
