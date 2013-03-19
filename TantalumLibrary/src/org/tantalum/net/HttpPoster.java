/*
 Copyright (c) 2013, Paul Houghton and Futurice Oy
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
package org.tantalum.net;

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
     * @param key - The url we will HTTP POST to, plus optional lines of text to
     * create a unique hashcode for caching this value locally.
     */
    public HttpPoster(final String key) {
        super(key);
    }

    /**
     * Create an HTTP POST operation
     * 
     * @param key
     * @param message 
     */
    public HttpPoster(final String key, final byte[] message) {
        super(key, message);
    }

    /**
     * Set the message to be HTTP POSTed to the server
     * 
     * @param message
     * @return 
     */
    public HttpPoster setMessage(final byte[] message) {
        if (message == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " was passed null message- meaningless POST or PUT operation: " + key);
        }
        this.postMessage = new byte[message.length];
        System.arraycopy(message, 0, this.postMessage, 0, message.length);

        return this;
    }
}
