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

import org.tantalum.net.HttpGetter;
import org.tantalum.util.L;

/**
 *
 * @author Paul Houghton
 */
public class JSONGetter extends HttpGetter {

    private final JSONModel jsonModel;

    /**
     * Request with HTTP GET a JSONModel object update
     * 
     * @param key
     * @param jsonModel 
     */
    public JSONGetter(final String key, final JSONModel jsonModel) {
        super(key);
        this.jsonModel = jsonModel;
    }

    /**
     * Execute the update of the JSONModel value on a background Worker thread
     * 
     * @param in
     * @return 
     */
    public Object exec(final Object in) {
        String value = null;
        try {
            value = new String((byte[]) super.exec(in), "UTF8").trim();
            if (value.startsWith("[")) {
                // Parser expects non-array base object- add one
                value = "{\"base:\"" + value + "}";
            }
            jsonModel.setJSON(value);
        } catch (Exception e) {
            //#debug
            L.e("JSONGetter HTTP response problem", key + " : " + value, e);
            cancel(false, "JSONGetter exception - " + key + " : " + e);
        }

        return jsonModel;
    }
}
