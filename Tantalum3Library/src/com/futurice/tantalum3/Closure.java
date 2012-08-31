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

    /**
     * You can call this at the end of your overriding method once you have set
     * the result.
     *
     * If you implement set(), do not implement or use compute()
     *
     * @param o
     */
    public synchronized void set(final Object o) {
        super.set(o);

        PlatformUtils.runOnUiThread(this);
    }

    public final synchronized Object joinUIThread(final long timeout) throws InterruptedException, CancellationException, ExecutionException, TimeoutException {
        final long t = System.currentTimeMillis();
        super.join(timeout);

        if (status < UI_RUN_FINISHED) {
            //#debug
            Log.l.log("Start joinUIThread wait()", this.toString());
            this.wait(timeout - (System.currentTimeMillis() - t));
            if (status < EXEC_STARTED) {
                throw new TimeoutException();
            }
            //#debug
            Log.l.log("End joinUIThread wait()", this.toString());
        }

        return result;
    }
}
