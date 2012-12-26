/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.android;

import android.graphics.BitmapFactory;
import org.tantalum.storage.ImageTypeHandler;
import org.tantalum.util.L;

/**
 * This is a helper class for creating an image class. It automatically converts
 * the byte[] to an Image as the data is loaded from the network or cache.
 *
 * @author tsaa
 */
public final class AndroidImageTypeHandler extends ImageTypeHandler {

    public Object convertToUseForm(final byte[] bytes) {
        try {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            
            //TODO downscale the image according to ImageTypeHandler parameters. In most cases not needed- Android UI will take care of that
        } catch (IllegalArgumentException e) {
            L.e("Exception converting bytes to image", bytes == null ? "" : "" + bytes.length, e);
            throw e;
        }
    }
}
