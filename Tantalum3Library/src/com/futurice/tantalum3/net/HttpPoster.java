/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.net;

import javax.microedition.io.HttpConnection;

/**
 *
 * @author phou
 */
public class HttpPoster extends HttpGetter {

    /**
     * HTTP POST a message to a given URL
     * 
     * @param url
     * @param retriesRemaining
     * @param task
     * @param postMessage 
     */
    public HttpPoster(final String url, final int retriesRemaining, final byte[] postMessage) {
        super(url, retriesRemaining);
        
        if (postMessage == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " was passed null post message- meaningless post operation: " + url);
        }
        this.postMessage = postMessage;
        this.requestMethod = HttpConnection.POST;
    }
}
