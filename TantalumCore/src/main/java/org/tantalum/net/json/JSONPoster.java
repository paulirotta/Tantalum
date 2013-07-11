/*
 Copyright (c) 2013 Nokia Corporation. All rights reserved.
 Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation.
 Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 affiliates. Other product and company names mentioned herein may be trademarks
 or trade names of their respective owners.

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
package org.tantalum.net.json;

import java.io.UnsupportedEncodingException;
import org.tantalum.Task;
import org.tantalum.net.HttpPoster;
import org.tantalum.util.L;

/**
 * A convenience class for sending HTTP POST to a web server
 *
 * @author combes
 */
public abstract class JSONPoster extends HttpPoster {

    private final JSONModel jsonModel = new JSONModel();

    /**
     * HTTP POST a JSON to a server
     *
     * The actual JSON will be provided as input from a previously chained Task.
     *
     * @param url
     * @param priority
     */
    public JSONPoster(final int priority, final String url) {
        super(priority, url);
    }

    /**
     * Create a Task.NORMAL_PRIORITY JSON poster
     *
     * @param url
     */
    public JSONPoster(final String url) {
        this(Task.NORMAL_PRIORITY, url);
    }

    /**
     * Create a Task.NORMAL_PRIORITY JSON poster
     * 
     */
    public JSONPoster() {
        super();
    }

    /**
     * HTTP POST JSON to a server
     *
     * @param url
     * @param json
     * @param priority
     * @throws UnsupportedEncodingException if the String can not be converted
     * to UTF-8 on this device
     */
    /**
     * 
     * @param priority
     * @param url
     * @param json
     * @param encoding - use "UTF-8" or other as required by the server
     * @throws UnsupportedEncodingException 
     */
    public JSONPoster(final int priority, final String url, final String json, final String encoding) throws UnsupportedEncodingException {
        super(priority, url, json.getBytes(encoding));
    }

    /**
     * Receives a "key", which is a URL plus optional additional lines of text
     * to create a unique cacheable hash code.
     *
     * This returns a JSONModel of the data provided by the HTTP server.
     *
     * @param key
     * @return
     */
    public Object exec(final Object key) {
        String value = null;

        try {
            value = new String((byte[]) super.exec(key), "UTF8").trim();
            if (value.startsWith("[")) {
                // Parser expects non-array base object- add one
                value = "{\"base:\"" + value + "}";
            }
            jsonModel.setJSON(value);
        } catch (Exception e) {
            //#debug
            L.e("JSONPoster HTTP response problem", key + " : " + value, e);
            cancel(false, "JSONPoster exception - " + key, e);
        }

        return jsonModel;
    }
}
