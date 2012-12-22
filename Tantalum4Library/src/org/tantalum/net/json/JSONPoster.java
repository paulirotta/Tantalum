/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.net.json;

import org.tantalum.util.L;
import org.tantalum.net.HttpPoster;

/**
 *
 * @author combes
 */
public abstract class JSONPoster extends HttpPoster {

    private final JSONModel jsonModel = new JSONModel();

    public JSONPoster(final String key) {
        super(key);
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
            L.e("JSONPoster HTTP response problem", key + " : " + value, e);
            cancel(false);
        }
        
        return jsonModel;
    }
}
