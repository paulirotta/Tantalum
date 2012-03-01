/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2.rms;

import com.futurice.tantalum2.log.Log;
import javax.microedition.lcdui.Image;

/**
 *
 * @author tsaa
 */
public final class ImageTypeHandler implements DataTypeHandler {

//    public static final Image DEFAULT_IMAGE = Image.createImage(1, 1);

    public Object convertToUseForm(final byte[] bytes) {
        try {
            return Image.createImage(bytes, 0, bytes.length);
        } catch (Exception e) {
            Log.l.log("Error converting bytes to image", bytes == null ? "" : "" + bytes.length, e);
        }

        return null;
    }
}
