/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2;

/**
 * A convenient and common implementation of the cache and http get result as an
 * Object.
 * 
 * For asynchronous cache get. Non-blocking get results are returned in a
 * runnable object, often of the form:
 *
 * staticWebCache.get("myurl", new Result() { public void run() { // do
 * something on EDT with getResult() object }
 *
 * The setResult() method will be called from a Worker thread. If actions taken
 * will take time or resources such as memory, it may be better to queue those
 * separately to the Worker thread because the calling routine can then proceed
 * to clean up and release resources and memory in parallel.
 *
 * The run() method will be automatically completed on the EDT thread after
 * changes in supporting methods in the Tantalum library.
 * 
 * @author tsaa, paul houghton
 */
public class Result {

    private volatile Object o;

    public Object getResult() {
        return o;
    }

    /**
     * This is called by the routine returning a result, from a Worker thread
     * and before run().
     *
     * @param o - the object returned from asynchronous Worker work
     */
    public void setResult(final Object o) {
        this.o = o;
    }

    /**
     * This is called from the Worker thread by the routine returning a result.
     * It indicates that no normal, meaningful result can be given at this time,
     * for example there was a network error, or data is not yet available in
     * the cache, etc.
     *
     */
    public void noResult() {
    }
}
