/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3;

/**
 *
 * @author phou
 */
public abstract class AsyncCallbackResult extends AsyncResult implements Runnable {

    public synchronized void set(final Object o) {
        super.set(o);

        PlatformUtils.runOnUiThread((Runnable) this);
    }
}
