/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3;

/**
 *
 * @author phou
 */
public abstract class AsyncCallbackTask extends Task {

    public final void run() {
        onPostExecute(getResult());
    }

    public Object doInBackground(final Object params) {
        return setResult(params);
    }

    protected abstract void onPostExecute(Object result);
}
