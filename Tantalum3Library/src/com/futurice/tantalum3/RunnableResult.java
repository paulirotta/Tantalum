/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3;

/**
 * An asynchronous callback object which is first run with setResult() on a
 * Worker thread, then queued to the Event Dispatch Thread (EDT) for updating
 * of user-interface synchronous results.
 * 
 * @author phou
 */
public abstract class RunnableResult extends Result implements Runnable {
    public void setResult(final Object o) {
        super.setResult(o);
        Worker.queueEDT(this);
    }    
}
