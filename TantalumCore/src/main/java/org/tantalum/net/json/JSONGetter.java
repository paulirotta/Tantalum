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

import org.tantalum.Task;
import org.tantalum.net.HttpGetter;
import org.tantalum.util.L;

/**
 *
 * @author Paul Houghton
 */
public class JSONGetter extends HttpGetter {

    protected final JSONModel jsonModel;

    /**
     * Request with HTTP GET a JSONModel object update
     * 
     * @param url
     * @param jsonModel
     * @param priority 
     */
    public JSONGetter(final int priority, final String url, final JSONModel jsonModel) {
        super(priority, url);
        
        this.jsonModel = jsonModel;
    }

    /**
     * Request with HTTP GET a JSONModel object update
     * 
     * @param jsonModel
     * @param priority 
     */
    public JSONGetter(final int priority, final JSONModel jsonModel) {
        super(priority);
        
        this.jsonModel = jsonModel;
    }

    /**
     * Create a Task.NORMAL_PRIORITY JSON getter
     * 
     * @param jsonModel 
     */
    public JSONGetter(final JSONModel jsonModel) {
        this(Task.NORMAL_PRIORITY, jsonModel);
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
            
            return jsonModel;
        } catch (Exception e) {
            //#debug
            L.e("JSONGetter HTTP response problem", "value=" + value, e);
            cancel(false, "JSONGetter exception - " + value, e);
        }

        return null;
    }
}
