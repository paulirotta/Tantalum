/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2.net;

import com.futurice.tantalum2.Log;
import javax.microedition.lcdui.Image;

/**
 *
 * @author phou
 */
public class HttpImageGetter /*extends HttpGetter*/ {

    public HttpImageGetter(final String url, final int retriesRemaining) {
        //super(url, retriesRemaining);
    }

    public boolean work() {
        /*Log.log("Start HttpImageGetter " + getUrl());
        if (super.work()) {
            try {
                Log.log("Start HttpImageGetter extraction " + getUrl());
                final byte[] bytes = (byte[]) getResult();
                final Image image = Image.createImage(bytes, 0, bytes.length);
                setResult(image);
                Log.log("HttpImageGetter extraction complete " + getUrl());
                if (image != null) {
                    this.cache.put(getUrl(), image);
                    Log.log("Put to cache " + getUrl());
                }
                if (bytes != null) {
                    this.cache.storeToRMS(getUrl(), bytes);
                    Log.log("Stored to RMS " + getUrl());
                }
                return true;
            } catch (Exception e) {
                Log.logThrowable(e, "ImageGetter HTTP response problem at " + getUrl());
                this.exception(e);
            }
        }
        */
        return false;
    }
}
