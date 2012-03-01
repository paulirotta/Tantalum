/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2;

/**
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
 * Be sure to use volatile or synchronized() to stay thread safe
 *
 * @author tsaa
 */
public interface Result extends Runnable {

    /**
     * Retrieve the result of asynchronous Worker thread work
     *
     * @return
     */
    public Object getResult();

    /**
     * Each Result implementation is responsible for maintaining a linked list
     * of Result objects and calling setResult() on these _before_ handling its
     * own result. See DefaultResult implementation and prepend().
     *
     * @param result
     * @param queueToEDTnow - set "true" unless you are calling setResult() on a
     * pervious Result in the prepend chain.
     */
    public void setResult(Object result, boolean queueToEDTnow);

    /**
     * This is called from the Worker thread if a trivial result such as null
     * is received
     * 
     * Note that the Result is _not_ automatically queued to the EDT under these
     * circumstances, however you may choose to queue it anyway and handle
     * trivial results there as you notify the user.
     * 
     */
    public void noResult();
    
    /**
     * Add a Result to the beginning of the linked list chain
     *
     * NOTE: the run() implementation of each Result _must_ similarly invoke
     * run() on the prepend Result chain before doing any action on the EDT.
     *
     * @param result - The Result which will be handled before the current
     * result
     *
     * @return - a reference to "this" so that prepend() can be chained
     */
    public Result prepend(Result result);
}
