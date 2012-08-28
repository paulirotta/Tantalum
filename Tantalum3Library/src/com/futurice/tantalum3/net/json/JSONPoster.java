/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.futurice.tantalum3.net.json;

import com.futurice.tantalum3.RunnableResult;
import com.futurice.tantalum3.Workable;
import com.futurice.tantalum3.log.Log;
import com.futurice.tantalum3.net.HttpPoster;

/**
 * 
 * @author combes
 */
public abstract class JSONPoster extends RunnableResult implements Workable {
    private HttpPoster httpPoster;    
    private final JSONModel jsonModel;

    public JSONPoster(final String url, final String postMessage, final int retriesRemaining) {
        jsonModel = new JSONModel();
        this.httpPoster = new HttpPoster(url, retriesRemaining, this, postMessage.getBytes());        
    }
    
    public JSONModel getJSONResult() {
        return jsonModel;
    }

    public void setResult(final Object o) {
        super.setResult(o);
              
        String value = "";

        try {
            byte[] byteResult = (byte[])o;
            //value = this.getResult().toString().trim();
            value = new String(byteResult);
            if (value.startsWith("[")) {
                // Parser expects non-array base object- add one
                value = "{\"base:\"" + value + "}";
            }
            jsonModel.setJSON(value);
        } catch (Exception e) {
            //#debug
            Log.l.log("JSONPoster result parsing problem", this.jsonModel + " : " + value, e);
            onCancel();
        }
    }
    
    public Object compute() {
        return httpPoster.compute();
    }
}
