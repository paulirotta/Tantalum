/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum4.net;

/**
 *
 * @author phou
 */
public class HttpPoster extends HttpGetter {

    /**
     * HTTP POST a message to a given URL
     * 
     * @param url
     */
    public HttpPoster(final String url) {
        super(url);
    }
    
    public HttpPoster setPostMessage(final byte[] postMessage) {
        if (postMessage == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " was passed null post message- meaningless post operation: " + url);
        }
        this.postMessage = new byte[postMessage.length];
        System.arraycopy(postMessage, 0, this.postMessage, 0, postMessage.length);        
        
        return this;
    }
}
