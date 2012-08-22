/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.net;

import com.futurice.tantalum3.Result;
import javax.microedition.io.HttpConnection;

/**
 *
 * @author phou
 */
public class NewHttpPoster extends HttpGetter {

    /**
     * HTTP POST a message to a given URL
     * 
     * @param url
     * @param retriesRemaining
     * @param result
     * @param postMessage 
     */
    public NewHttpPoster(final String url, final int retriesRemaining, final Result result, final byte[] postMessage) {
        super(url, retriesRemaining, result);
        
        if (postMessage == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " was passed null post message- meaningless post operation: " + url);
        }
        this.postMessage = postMessage;
        this.requestMethod = HttpConnection.POST;
    }
}
