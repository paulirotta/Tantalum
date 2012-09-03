package com.futurice.tantalum3;

import com.futurice.tantalum3.log.Log;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * A convenience class for creating anonymous inner classes which first perform
 * an action on a Worker thread, then notify the user on the UI thread.
 *
 * A given Closure will either implement set() or compute(), but not both. It
 * simply depends if this Closure is executed on the Worker thread, or if it is
 * used by another object as a delegate for returning a result to the UI thread.
 *
 * @author phou
 */
public abstract class Closure extends Task implements Runnable {
}
