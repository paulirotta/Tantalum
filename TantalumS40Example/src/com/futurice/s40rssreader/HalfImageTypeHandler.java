/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.s40rssreader;

import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.rms.DataTypeHandler;
import com.futurice.tantalum2.util.ImageUtils;
import javax.microedition.lcdui.Image;

/**
 *
 * @author phou
 */
public class HalfImageTypeHandler implements DataTypeHandler {

    public Object convertToUseForm(final byte[] bytes) {
        try {
            Image image = Image.createImage(bytes, 0, bytes.length);
            final int w = image.getWidth();
            final int[] data = new int[w * image.getHeight()];
            image.getRGB(data, 0, w, 0, 0, w, image.getHeight());
            image = null;
            return ImageUtils.quarterImage(data, w);
        } catch (Exception e) {
            Log.l.log("Error converting bytes to image", bytes == null ? "" : "" + bytes.length, e);
        }

        return null;
    }
}
