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
package com.futurice.tantalum;

import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.lcdui.Display;
import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 *
 * @author Paul Houghton
 */
public class JSONVO {
    private static Display display;

    private JSONObject jsonObject = new JSONObject();
    private static final Hashtable bindings = new Hashtable();

    public static void setDisplay(Display display) {
        JSONVO.display = display;
    }

    /**
     * Null constructor is an empty placeholder
     */
    public JSONVO() {
    }

    public void bind(String key, Runnable binding) {
        bindings.put(key, binding);
    }

    public void unbind(String key) {
        bindings.remove(key);
    }

    public void setJSON(String json) throws JSONException {
        synchronized (bindings) {
            jsonObject = new JSONObject(json);
            notifyBoundObjects();
        }
    }

    public void notifyBoundObjects() {
        synchronized (bindings) {
            final Enumeration enu = bindings.keys();

            while (enu.hasMoreElements()) {
                notifyBoundObject((String) enu.nextElement());
            }
        }
    }

    public void notifyBoundObject(String key) {
        synchronized (bindings) {
            if (bindings.contains(key)) {
                display.callSerially((Runnable) bindings.get(key));
            }
        }
    }

    public boolean getBoolean(String key) throws JSONException {
        synchronized (bindings) {
            return jsonObject.getBoolean(key);
        }
    }

    public String getString(String key) throws JSONException {
        synchronized (bindings) {
            return jsonObject.getString(key);
        }
    }

    public double getDouble(String key) throws JSONException {
        synchronized (bindings) {
            return jsonObject.getDouble(key);
        }
    }

    public int getInt(String key) throws JSONException {
        synchronized (bindings) {
            return jsonObject.getInt(key);
        }
    }

    public long getLong(String key) throws JSONException {
        synchronized (bindings) {
            return jsonObject.getLong(key);
        }
    }
}
