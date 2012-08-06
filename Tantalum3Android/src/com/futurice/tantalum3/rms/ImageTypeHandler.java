/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.rms;

import android.graphics.BitmapFactory;
import com.futurice.tantalum3.log.Log;

/**
 * This is a helper class for creating an image class. It automatically converts
 * the byte[] to an Image as the data is loaded from the network or cache.
 *
 * @author tsaa
 */
public final class ImageTypeHandler implements DataTypeHandler {

    private int imageSide;

    public ImageTypeHandler() {
        imageSide = -1;
    }

    public ImageTypeHandler(int side) {
        imageSide = side;
    }


    public Object convertToUseForm(final byte[] bytes) {
        try {
           // if (imageSide == -1) {
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
           /* } else {
                Bitmap temp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                return Bitmap..createBitmap(temp, 0, 0, imageSide, imageSide);
            }*/

        } catch (IllegalArgumentException e) {
            //#debug
            Log.l.log("Exception converting bytes to image", bytes == null ? "" : "" + bytes.length, e);
            throw e;
        }
    }
}
