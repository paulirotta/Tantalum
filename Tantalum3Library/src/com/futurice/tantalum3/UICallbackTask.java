package com.futurice.tantalum3;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * A convenience class for creating anonymous inner classes which first perform
 * an action on a Worker thread, then notify the user on the UI thread.
 *
 * A given UICallbackTask will either implement set() or compute(), but not both. It
 * simply depends if this UICallbackTask is executed on the Worker thread, or if it is
 * used by another object as a delegate for returning a result to the UI thread.
 * 
 * Note that you can make any Task such as HttpGetter behave as a UICallbackTask by
 * extending it and implementing Runnable. The resulting behavior is in all cases
 * the same as for a UICallbackTask. Your code should similarly use instancoef Runnable
 * rather than instanceof UICallbackTask.
 *
 * @author phou
 */
public abstract class UICallbackTask extends Task implements Runnable {
}
