/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2.util;

import com.futurice.tantalum2.Worker;
import javax.microedition.lcdui.Image;

/**
 * Only one image processing routine will be active at a time. This is enforced
 * by internal synchronization, so image processing is thread safe with minimal
 * peak memory usage if you call these routines from multiple threads.
 *
 * @author phou
 */
public final class ImageUtils {

    private static final int RED = 0xFF0000;
    private static final int GREEN = 0x00FF00;
    private static final int BLUE = 0x0000FF;

    /**
     * Return an RGB image where width and height are half the original.
     *
     * 4 pixels are combined into 1. The image is passed in as a reference to an
     * integer array to minimize peak memory usage during image processing. You
     * should delete all strong references to the Image from which this int[] is
     * created _before_ calling this routine. The int[] will be altered by this
     * routine.
     *
     * @param in
     * @param inWidth
     * @return
     */
    public static final Image halfImage(final int[] in, final int inWidth) {
        synchronized (Worker.LARGE_MEMORY_MUTEX) {
            final int inHeight = in.length / inWidth;
            final int outWidth = inWidth >> 1;
            final int outHeight = inHeight >> 1;
            int x, y, z = 0, i, r, g, b;

            for (y = 0; y < inHeight - 1; y += 2) {
                i = y * inWidth;
                for (x = 0; x < outWidth; x++) {
                    r = in[i] & RED;
                    g = in[i] & GREEN;
                    b = in[i] & BLUE;
                    i++;
                    r += in[i] & RED;
                    g += in[i] & GREEN;
                    b += in[i] & BLUE;
                    i += inWidth;
                    r += in[i] & RED;
                    g += in[i] & GREEN;
                    b += in[i] & BLUE;
                    i--;
                    r += in[i] & RED;
                    g += in[i] & GREEN;
                    b += in[i] & BLUE;
                    in[z] = ((r & (RED << 2)) | (g & (GREEN << 2)) | (b & (BLUE << 2))) >>> 2;
                    z++;
                    i -= inWidth - 2;
                }
            }

            return Image.createRGBImage(in, outWidth, outHeight, false);
        }
    }

    public static final Image quarterImage(final int[] in, final int inWidth) {
        synchronized (Worker.LARGE_MEMORY_MUTEX) {
            final int inHeight = in.length / inWidth;
            final int outWidth = inWidth >> 2;
            final int outHeight = inHeight >> 2;
            int x, y, z = 0, i, r, g, b;

            for (y = 0; y < inHeight - 3; y += 4) {
                for (x = 0; x < outWidth; x++) {
                    i = y * inWidth + 4*x;

                    // Row 1
                    r = in[i] & RED;
                    g = in[i] & GREEN;
                    b = in[i] & BLUE;
                    i++;
                    r += in[i] & RED;
                    g += in[i] & GREEN;
                    b += in[i] & BLUE;
                    i++;
                    r += in[i] & RED;
                    g += in[i] & GREEN;
                    b += in[i] & BLUE;
                    i++;
                    r += in[i] & RED;
                    g += in[i] & GREEN;
                    b += in[i] & BLUE;
                    
                    // Row 2
                    i += inWidth;
                    r += in[i] & RED;
                    g += in[i] & GREEN;
                    b += in[i] & BLUE;
                    i--;
                    r += in[i] & RED;
                    g += in[i] & GREEN;
                    b += in[i] & BLUE;
                    i--;
                    r += in[i] & RED;
                    g += in[i] & GREEN;
                    b += in[i] & BLUE;
                    i--;
                    r += in[i] & RED;
                    g += in[i] & GREEN;
                    b += in[i] & BLUE;
                    
                    // Row 3
                    i += inWidth;
                    r += in[i] & RED;
                    g += in[i] & GREEN;
                    b += in[i] & BLUE;
                    i++;
                    r += in[i] & RED;
                    g += in[i] & GREEN;
                    b += in[i] & BLUE;
                    i++;
                    r += in[i] & RED;
                    g += in[i] & GREEN;
                    b += in[i] & BLUE;
                    i++;
                    r += in[i] & RED;
                    g += in[i] & GREEN;
                    b += in[i] & BLUE;

                    // Row 4
                    i += inWidth;
                    r += in[i] & RED;
                    g += in[i] & GREEN;
                    b += in[i] & BLUE;
                    i--;
                    r += in[i] & RED;
                    g += in[i] & GREEN;
                    b += in[i] & BLUE;
                    i--;
                    r += in[i] & RED;
                    g += in[i] & GREEN;
                    b += in[i] & BLUE;
                    i--;
                    r += in[i] & RED;
                    g += in[i] & GREEN;
                    b += in[i] & BLUE;

                    in[z] = ((r & (RED << 4)) | (g & (GREEN << 4)) | (b & BLUE << 4)) >>> 4;
                    z++;
                }
            }

            return Image.createRGBImage(in, outWidth, outHeight, false);
        }
    }
}
