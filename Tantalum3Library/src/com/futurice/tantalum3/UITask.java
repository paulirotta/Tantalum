package com.futurice.tantalum3;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * A convenience class for creating anonymous inner classes which first perform
 * an action on a Worker thread, then notify the user on the UI thread.
 *
 * A given UITask will either implement set() or compute(), but not both. It
 * simply depends if this UITask is executed on the Worker thread, or if it is
 * used by another object as a delegate for returning a result to the UI thread.
 * 
 * Note that you can make any Task such as HttpGetter behave as a UITask by
 * extending it and implementing Runnable. The resulting behavior is in all cases
 * the same as for a UITask. Your code should similarly use instancoef Runnable
 * rather than instanceof UITask.
 *
 * @author phou
 */
public abstract class UITask extends AsyncResult implements Runnable {
}
