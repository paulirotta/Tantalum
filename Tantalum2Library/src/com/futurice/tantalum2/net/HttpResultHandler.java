/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.futurice.tantalum2.net;

/**
 *
 * @author pahought
 */
public abstract class HttpResultHandler {

    /**
     * Implement this to receive data asynchronously from the network
     * 
     * @param responseData
     */
    public abstract void response(byte[] responseData);

    /**
     * Override this for custom exception handling when there is a network error
     *
     * @param e
     */
    public void exception(Exception e) {
        //Log.logThrowable(e, "Http result exception");
    }
}
