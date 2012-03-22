/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2;

/**
 *
 * @author phou
 */
public abstract class RunnableResult extends Result implements Runnable {
    public synchronized void setResult(final Object o) {
        super.setResult(o);
        Worker.queueEDT(this);
    }    
}
