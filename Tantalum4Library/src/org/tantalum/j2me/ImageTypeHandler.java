package org.tantalum.j2me;

import org.tantalum.Worker;
import org.tantalum.log.L;
import org.tantalum.util.ImageUtils;
import javax.microedition.lcdui.Image;
import org.tantalum.storage.DataTypeHandler;

/**
 * This is a helper class for creating an image class. It automatically converts
 * the byte[] to an Image as the data is loaded from the network or cache.
 *
 * @author tsaa
 */
public final class ImageTypeHandler implements DataTypeHandler {

    private final int maxWidth;
    private final int maxHeight;
    private final boolean processAlpha;
    private final boolean bestQuality;

    /**
     * Create a non-scaling image cache
     *
     */
    public ImageTypeHandler() {
        maxWidth = -1;
        maxHeight = -1;
        this.processAlpha = false;
        this.bestQuality = false;
    }

    /**
     * Create an image cache which scales images on load into memory
     *
     * @param processAlpha
     * @param bestQuality
     * @param maxWidth
     * @param maxHeight
     */
    public ImageTypeHandler(final boolean processAlpha, final boolean bestQuality, final int maxWidth, final int maxHeight) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.processAlpha = processAlpha;
        this.bestQuality = bestQuality;
    }

    public Object convertToUseForm(final byte[] bytes) {
        final Image img;

        try {
            if (maxWidth == -1) {
                //#debug
                L.i("convert image", "length=" + bytes.length);
                img = Image.createImage(bytes, 0, bytes.length);
            } else {
                synchronized (Worker.LARGE_MEMORY_MUTEX) {
                    Image temp = Image.createImage(bytes, 0, bytes.length);
                    final int w = temp.getWidth();
                    final int h = temp.getHeight();
                    int[] data = new int[w * h];
                    temp.getRGB(data, 0, w, 0, 0, w, h);
                    temp = null;
                    img = ImageUtils.downscaleImage(data, w, h, maxWidth, maxHeight, true, processAlpha, bestQuality);
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
