package com.futurice.tantalum3.rms;

import com.futurice.tantalum3.log.Logg;
import com.futurice.tantalum3.util.ImageUtils;
import javax.microedition.lcdui.Image;

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
            if (imageSide == -1) {
                return Image.createImage(bytes, 0, bytes.length);
            } else {                
                Image temp = Image.createImage(bytes, 0, bytes.length);
                final int w = temp.getWidth();
                final int h = temp.getHeight();
                int[] data = new int[w*h];
                temp.getRGB(data, 0, w, 0, 0, w, h);
                temp = null;
                final Image img = ImageUtils.downscaleImage(data, w, h, imageSide, imageSide, true, true, true);
                data = null;
                
                return img;
            }
        } catch (IllegalArgumentException e) {
            //#debug
            Logg.l.log("Exception converting bytes to image", bytes == null ? "" : "" + bytes.length, e);
            throw e;
        }
    }
}
