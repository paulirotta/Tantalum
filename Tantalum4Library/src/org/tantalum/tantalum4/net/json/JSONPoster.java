/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.tantalum4.net.json;

import org.tantalum.tantalum4.log.L;
import org.tantalum.tantalum4.net.HttpPoster;

/**
 *
 * @author combes
 */
public abstract class JSONPoster extends HttpPoster {

    private final JSONModel jsonModel = new JSONModel();

    public JSONPoster(final String url) {
        super(url);
    }

    public Object doInBackground(final Object in) {
        String value = null;
        
        try {
            value = new String((byte[]) super.doInBackground(in), "UTF8").trim();
            if (value.startsWith("[")) {
                // Parser expects non-array base object- add one
                value = "{\"base:\"" + value + "}";
            }
            jsonModel.setJSON(value);
            setValue(jsonModel);
        } catch (Exception e) {
            //#debug
            L.e("JSONPoster HTTP response problem", url + " : " + value, e);
            cancel(false);
        }
        
        return jsonModel;
    }
}
