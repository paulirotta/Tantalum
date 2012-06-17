/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.rms;

import com.futurice.tantalum3.log.Log;
import javax.microedition.lcdui.Image;

/**
 * This is a helper class for creating an image class. It automatically converts
 * the byte[] to an Image as the data is loaded from the network or cache.
 *
 * @author tsaa
 */
public final class ImageTypeHandler implements DataTypeHandler {

    public Object convertToUseForm(final byte[] bytes) {
        try {
            return Image.createImage(bytes, 0, bytes.length);
        } catch (IllegalArgumentException e) {
            //#debug
            Log.l.log("Exception converting bytes to image", bytes == null ? "" : "" + bytes.length, e);
            throw e;
        }
    }
}
