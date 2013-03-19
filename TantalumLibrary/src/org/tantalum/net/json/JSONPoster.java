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
 package org.tantalum.net.json;

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
     * HTTP POST a JSONModel to a server
     * 
     * @param key 
     */
    public JSONPoster(final String key) {
        super(key);
    }

    /**
     * Receives a "key", which is a URL plus optional additional lines
     * of text to create a unique cacheable hash code.
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
            cancel(false, "JSONPoster exception - " + key + " : " + e);
        }
        
        return jsonModel;
    }
}
