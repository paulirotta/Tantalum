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
package com.futurice.tantalum2.net.json;

import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.net.HttpGetter;
import com.futurice.tantalum2.net.HttpGetter;

/**
 *
 * @author Paul Houghton
 */
public class JSONGetter /*implements ResultHandler*/ {

    private final HttpGetter httpGetter;
    private final JSONModel jsonvo;

    public JSONGetter(final String url, final JSONModel jsonModel, final int retriesRemaining) {
        this.httpGetter = null;//new HttpGetter(url, retriesRemaining);
        this.jsonvo = jsonModel;
    }

    public void setHttpGetter(HttpGetter httpGetter) {
        //this.httpGetter = httpGetter;
    }

    public boolean work() {
        if (httpGetter.work()) {
            String value = "";

            try {
                value = this.getResult().toString().trim();
                if (value.startsWith("[")) {
                    // Parser expects non-array base object- add one
                    value = "{\"base:\"" + value + "}";
                }
                jsonvo.setJSON(value);

                return true;
            } catch (Exception e) {
                Log.logThrowable(e, "JSONGetter HTTP response problem at " + this.httpGetter.getUrl() + " : " + value);
                this.exception(e);

                return false;
            }
        }

        return false;
    }

    public void setResult(Object result) {
        //this.result = result;
    }

    public Object getResult() {
        return null;
    }

    public void exception(Exception e) {
    }
}
