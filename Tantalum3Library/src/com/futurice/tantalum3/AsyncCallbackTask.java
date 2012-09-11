/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3;

/**
 *
 * @author phou
 */
public abstract class AsyncCallbackTask extends Task implements Runnable {

    public final void run() {
        onPostExecute(getResult());
    }

    public Object doInBackground(final Object in) {
        return in;
    }

    protected abstract void onPostExecute(Object result);
}
