/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.net.json;

import com.futurice.tantalum3.log.L;
import com.futurice.tantalum3.net.HttpPoster;

/**
 *
 * @author combes
 */
public abstract class JSONPoster extends HttpPoster {

    private final JSONModel jsonModel;

    public JSONPoster(final String url, final String postMessage, final JSONModel jsonModel, final int retriesRemaining) {
        super(url, retriesRemaining, postMessage.getBytes());
        
        this.jsonModel = jsonModel;
    }

    public Object doInBackground(final Object in) {
        String value = ((byte[]) super.doInBackground(in)).toString().trim();

        try {
            if (value.startsWith("[")) {
                // Parser expects non-array base object- add one
                value = "{\"base:\"" + value + "}";
            }
            jsonModel.setJSON(value);
            setResult(jsonModel);
        } catch (Exception e) {
            //#debug
            L.e("JSONPoster HTTP response problem", this.getUrl() + " : " + value, e);
            cancel(false);
        }
        
        return jsonModel;
    }
}
