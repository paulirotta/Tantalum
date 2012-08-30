/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.net.json;

import com.futurice.tantalum3.Task;
import com.futurice.tantalum3.log.Log;
import com.futurice.tantalum3.net.HttpPoster;

/**
 *
 * @author combes
 */
public abstract class JSONPoster extends HttpPoster {

    private final JSONModel jsonvo;

    public JSONPoster(final String url, final String postMessage, final JSONModel jsonModel, final Task result, final int retriesRemaining) {
        super(url, retriesRemaining, result, postMessage.getBytes());
        
        this.jsonvo = jsonModel;
    }

    public Object compute() {
        final byte[] bytes = (byte[]) super.compute();
        String value = "";

        try {
            value = bytes.toString().trim();
            if (value.startsWith("[")) {
                // Parser expects non-array base object- add one
                value = "{\"base:\"" + value + "}";
            }
            jsonvo.setJSON(value);
            result.set(value);
        } catch (Exception e) {
            //#debug
            Log.l.log("JSONPoster HTTP response problem", this.getUrl() + " : " + value, e);
            result.cancel(false);
        }
        
        return bytes;
    }
}
