/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.futurice.tantalum3;

/**
 *
 * @author phou
 */
public class DelegateTask extends Task {

    /**
     * You can call this at the end of your overriding method once you have set
     * the result.
     *
     * If you implement set(), do not implement or use exec()
     *
     * @param o
     */
    public synchronized void set(final Object o) {
        this.result = o;
        setStatus(EXEC_FINISHED);

        if (this instanceof Runnable) {
            // Continue to closure
            PlatformUtils.runOnUiThread((Runnable) this);
        }
    }
    
    public synchronized void exec() {
        throw new IllegalStateException("DelegateTask must be set() by another executable, it can not exec()");
    }    
}
