/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.lwuitrssreader;

import com.futurice.tantalum3.log.Log;
import com.futurice.tantalum3.rms.DataTypeHandler;
import com.sun.lwuit.Image;

/**
 *
 * @author phou
 */
public final class LWUITImageTypeHandler implements DataTypeHandler {

    public Object convertToUseForm(final byte[] bytes) {
        try {
            return Image.createImage(bytes, 0, bytes.length);
        } catch (Exception e) {
            Log.l.log("Error converting bytes to LWUIT image", bytes == null ? "" : "" + bytes.length, e);
        }

        return null;
    }
}
