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

import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 * A data model (as in MVC) that keeps the data in JSON format
 *
 * This model is thread safe and internally synchronized. You can synchronize
 * off this object to safely access multiple fields at once without interference
 * from model updates which may come in from other background threads.
 *
 * The point of JSONModel is, like XMLModel, to allow you to create a local Object of type
 * JSONModel which may last the entire life of your application. You can then
 * refer to values from JSONModel and know that it always contains the latest,
 * best JSONObject received from the server or loaded from the local cache.
 * 
 * You can update the value in JSONModel by passing it as an object when you
 * call JSONGetter(url, myJSONMOdel). If you would like to execute some code such
 * as updating the user interface after the JSONModel is updated asynchonously,
 * you should chain() that continuation code (a Task or UITask) to the JSONGetter.
 * 
 * @author Paul Houghton
 */
public class JSONModel {

    private JSONObject jsonObject = new JSONObject();

    /**
     * Update the value of the model
     *
     * @param json
     * @throws JSONException
     */
    public synchronized void setJSON(final String json) throws JSONException {
        jsonObject = new JSONObject(json);
    }

    /**
     * Get a boolean by key
     *
     * @param key
     * @return
     * @throws JSONException
     */
    public synchronized boolean getBoolean(final String key) throws JSONException {
        return jsonObject.getBoolean(key);
    }

    /**
     * Get a String by key
     *
     * @param key
     * @return
     * @throws JSONException
     */
    public synchronized String getString(String key) throws JSONException {
        return jsonObject.getString(key);
    }

    /**
     * Get a double by key
     *
     * @param key
     * @return
     * @throws JSONException
     */
    public synchronized double getDouble(String key) throws JSONException {
        return jsonObject.getDouble(key);
    }

    /**
     * Get an int by key
     *
     * @param key
     * @return
     * @throws JSONException
     */
    public synchronized int getInt(String key) throws JSONException {
        return jsonObject.getInt(key);
    }

    /**
     * Get a long by key
     *
     * @param key
     * @return
     * @throws JSONException
     */
    public synchronized long getLong(String key) throws JSONException {
        return jsonObject.getLong(key);
    }
    
    /**
     * Returns JSONObject currently contained in the model 
     * and sets it to null 
     *    
     * @param 
     * @return JSONObject
     * @throws 
    */
    public synchronized JSONObject take() {
    	JSONObject ret = jsonObject;
    	jsonObject = null;
    	return ret;    	
    }
   
}
