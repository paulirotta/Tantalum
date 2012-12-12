/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.tantalum4.net;

/**
 * PUT data to a web REST service
 *
 * @author phou
 */
public class HttpPutter extends HttpPoster {

    /**
     * PUT data to a web REST service
     *
     * HTTP PUT is structurally very similar to HTTP POST, but can be used for
     * different purposes by some servers.
     *
     * Make sure you call setMessage(byte[]) to specify what you want to PUT or
     * you will get an IllegalArgumentException
     *
     * @param url
     */
    public HttpPutter(final String url) {
        super(url);
    }
}
