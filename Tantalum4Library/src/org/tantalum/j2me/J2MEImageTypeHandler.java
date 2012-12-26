package org.tantalum.j2me;

import javax.microedition.lcdui.Image;
import org.tantalum.Worker;
import org.tantalum.storage.ImageTypeHandler;
import org.tantalum.util.ImageUtils;
import org.tantalum.util.L;

/**
 * This is a helper class for creating an image class. It automatically converts
 * the byte[] to an Image as the data is loaded from the network or cache.
 *
 * @author tsaa
 */
public final class J2MEImageTypeHandler extends ImageTypeHandler {

    public Object convertToUseForm(final byte[] bytes) {
        final Image img;
        final boolean quality;
        final boolean alpha;
        final boolean aspect;
        final int w, h;
        
        synchronized (this) {
            quality = this.bestQuality;
            alpha = this.processAlpha;
            aspect = this.preserveAspectRatio;
            w = this.maxWidth;
            h = this.maxHeight;
        }

        try {
            if (w == -1) {
                //#debug
                L.i("convert image", "length=" + bytes.length);
                img = Image.createImage(bytes, 0, bytes.length);
            } else {
                synchronized (Worker.LARGE_MEMORY_MUTEX) {
                    Image temp = Image.createImage(bytes, 0, bytes.length);
                    final int tempW = temp.getWidth();
                    final int tempH = temp.getHeight();
                    int[] data = new int[tempW * tempH];
                    temp.getRGB(data, 0, tempW, 0, 0, tempW, tempH);
                    temp = null;
                    img = ImageUtils.downscaleImage(data, tempW, tempH, w, h, aspect, alpha, quality);
                    data = null;
                }
            }
        } catch (IllegalArgumentException e) {
            //#debug
            L.e("Exception converting bytes to image", "image byte length=" + bytes.length, e);
            throw e;
        }

        return img;
    }
}
