/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2;

import com.futurice.tantalum2.net.HttpGetter;

/**
 *
 * @author phou
 */
public abstract class RunnableHttpGetter /*extends HttpGetter implements Runnable*/ {

    public RunnableHttpGetter(String url, int retriesRemaining) {
        //super(url, retriesRemaining);
    }

    abstract public void run();
}
