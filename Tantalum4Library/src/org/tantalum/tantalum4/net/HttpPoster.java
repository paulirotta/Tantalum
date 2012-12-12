/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.tantalum4.net;

/**
 * HTTP POST a message to a given URL
 *
 * @author phou
 */
public class HttpPoster extends HttpGetter {

    /**
     * HTTP POST a message to a given URL
     *
     * Make sure you call setMessage(byte[]) to specify what you want to POST or
     * you will get an IllegalArgumentException
     *
     * @param url
     */
    public HttpPoster(final String url) {
        super(url);
    }

    public HttpPoster setMessage(final byte[] message) {
        if (message == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " was passed null message- meaningless POST or PUT operation: " + url);
        }
        this.postMessage = new byte[message.length];
        System.arraycopy(message, 0, this.postMessage, 0, message.length);

        return this;
    }
}
