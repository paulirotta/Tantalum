/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2;

/**
 * A task to be completed on the Worker thread
 * 
 * Conceptually this is the same as the Runnable interface, but helps to keep
 * clear which objects objects are intended for Worker threads and which for
 * other threads such as the event dispatch thread.
 * 
 * An object may implement both Workable and Runnable to provide automatic passage
 * to the EDT after completing a task on the Worker thread.
 * 
 * @author phou
 */
public interface Workable {
    public boolean work();
}
