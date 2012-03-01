/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2;

/**
 * For asynchronous cache get. Non-blocking get results are returned in a runnable
 * object, often of the form:
 * 
 * staticWebCache.get("myurl", new Result() {
 *     public void run() {
 *         // do something on EDT with getResult() object
 *     }
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
    public Object getResult();
    
    public void setResult(Object result);
}
