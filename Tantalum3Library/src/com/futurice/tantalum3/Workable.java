package com.futurice.tantalum3;

/**
 * A task to be completed on the Worker thread
 * 
 * Conceptually this is the same as the Runnable interface, but helps to keep
 * clear which objects objects are intended for Worker threads and which for
 * other threads such as the event dispatch thread.
 * 
 * An object may implement both Workable and Closure to provide automatic passage
 * to the UI thread after completing a task on a Worker thread. This is used to
 * perform any UI cleanup. AsyncTask provides additional support for this
 * using a Java7-like fork-join pattern and an Android-like asynchronous
 * thread hand-off pattern and progress update pattern.
 * 
 * @author phou
 */
public interface Workable {
    
    /**
     * Do a task on a background thread
     * 
     * @param in 
     */
    public Object exec(Object in);
}
