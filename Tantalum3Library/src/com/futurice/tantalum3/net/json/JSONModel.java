/*
 * Tantalum Mobile Toolset
 * https://projects.forum.nokia.com/Tantalum
 *
 * Special thanks to http://www.futurice.com for support of this project
 * Project lead: paul.houghton@futurice.com
 *
 * Copyright 2010 Paul Eugene Houghton
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.futurice.tantalum3.net.json;

import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 *
 * @author Paul Houghton
 */
public class JSONModel {
    private static final Object MUTEX = new Object();

    private JSONObject jsonObject = new JSONObject();

    /**
     * Null constructor is an empty placeholder
     */
    public JSONModel() {
    }

    public void setJSON(String json) throws JSONException {
        synchronized (MUTEX) {
            jsonObject = new JSONObject(json);
        }
    }

    public boolean getBoolean(String key) throws JSONException {
        synchronized (MUTEX) {
            return jsonObject.getBoolean(key);
        }
    }

    public String getString(String key) throws JSONException {
        synchronized (MUTEX) {
            return jsonObject.getString(key);
        }
    }

    public double getDouble(String key) throws JSONException {
        synchronized (MUTEX) {
            return jsonObject.getDouble(key);
        }
    }

    public int getInt(String key) throws JSONException {
        synchronized (MUTEX) {
            return jsonObject.getInt(key);
        }
    }

    public long getLong(String key) throws JSONException {
        synchronized (MUTEX) {
            return jsonObject.getLong(key);
        }
    }
}
