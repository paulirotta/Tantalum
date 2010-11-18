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

import javax.microedition.lcdui.Display;

/**
 *
 * @author Paul Houghton
 */
public class JSONGetter extends Result implements Runnable {

    final Display display;
    final String url;
    final JSONVO jsonvo;
    final Runnable eventDispatchThreadRunnable;
    final int retriesRemaining;

    public JSONGetter(Display display, String url, JSONVO jsonvo, Runnable eventDisplatchThreadRunnable, int retriesRemaining) {
        this.display = display;
        this.url = url;
        this.jsonvo = jsonvo;
        this.eventDispatchThreadRunnable = eventDisplatchThreadRunnable;
        this.retriesRemaining = retriesRemaining;
    }

    public void run() {
        final HttpGetter httpGetter = new HttpGetter(url, this, this.retriesRemaining);
        httpGetter.run();
    }

    public void response(final String value) {
        try {
            String v2 = value.trim();
            if (v2.startsWith("[")) {
                // Parser expects non-array base object- add one
                v2 = "{base:" + value + "}";
            }
            jsonvo.setJSON(v2);
            if (eventDispatchThreadRunnable != null) {
                display.callSerially(eventDispatchThreadRunnable);
            }
        } catch (Exception e) {
            Log.log("JSONGetter HTTP response problem at " + url + " : " + e);
            e.printStackTrace();
            this.exception(e);
        }
    }
}
