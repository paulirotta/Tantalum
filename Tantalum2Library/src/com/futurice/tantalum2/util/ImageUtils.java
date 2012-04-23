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
    private static final int FP_SHIFT = 13;
    private static final int ALPHA = 0xFF000000;
    private static final int RED = 0x00FF0000;
    private static final int GREEN = 0x0000FF00;
    private static final int BLUE = 0x000000FF;

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
    public static Image halfImage(final int[] in, final int inWidth) {
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

    public static Image quarterImage(final int[] in, final int inWidth) {
        synchronized (Worker.LARGE_MEMORY_MUTEX) {
            final int inHeight = in.length / inWidth;
            final int outWidth = inWidth >> 2;
            final int outHeight = inHeight >> 2;
            int x, y, z = 0, i, r, g, b;

            for (y = 0; y < inHeight - 3; y += 4) {
                for (x = 0; x < outWidth; x++) {
                    i = y * inWidth + 4 * x;

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

    /**
     * 
     * @param srcData
     * @param srcWidth
     * @param srcHeight
     * @param maxWidth
     * @param maxHeight
     * @return 
     */
    public static Image resizeImageProportional(final int[] srcData, final int srcWidth, final int srcHeight, int maxWidth, int maxHeight) {
        final float byWidth = maxWidth / (float) srcWidth;
        final float byHeight = maxHeight / (float) srcHeight;
        
        if (byWidth < byHeight) {
            maxWidth = (int) (srcWidth * byWidth);
            maxHeight = (int) (srcHeight * byWidth);
        } else {
            maxWidth = (int) (srcWidth * byHeight);
            maxHeight = (int) (srcHeight * byHeight);            
        }

        return ImageUtils.resizeImage(srcData, srcWidth, srcHeight, maxWidth, maxHeight);
    }

    /**
     * additive blending resizeImage
     *
     * Gets a source image along with new size for it and resizes it.
     *
     * @param src The source image.
     * @param destW The new width for the destination image.
     * @param destH The new heigth for the destination image.
     * @param mode A flag indicating what type of resizing we want to do. It
     * currently supports two type: MODE_POINT_SAMPLE - point sampled resizing,
     * and MODE_BOX_FILTER - box filtered resizing (default).
     * @return The resized image.
     */
    public static Image resizeImage(final int[] srcPixels, final int srcW, final int srcH, final int destW, final int destH) {
        final int[] argb = resizeImageHorizontally(srcPixels, srcW, srcH, destW);
        return resizeImageVertically(argb, destW, srcH, destH);
    }

    private static int[] resizeImageHorizontally(int[] srcPixels, final int srcW, final int srcH, final int destW) {
        // create pixel arrays
        //final int[] destPixels = new int[destW * srcH]; // array to hold destination pixels

        // precalculate src/dest ratios
        final int ratioW = (srcW << FP_SHIFT) / destW;
        final int predictedCount = 1 + (srcW / destW);
        final int[] lut = new int[predictedCount << 8];

        // Init division lookup table
        for (int i = 0; i < lut.length; i++) {
            lut[i] = i / predictedCount;
        }

        // horizontal resampling
        for (int destY = 0; destY < srcH; ++destY) {
            for (int destX = 0; destX < destW; ++destX) {
                int count = 0;
                int a = 0;
                int r = 0;
                int b = 0;
                int g = 0; // initialize color blending vars
                int srcX = (destX * ratioW) >> FP_SHIFT; // calculate beginning of sample
                final int srcX2 = ((destX + 1) * ratioW) >> FP_SHIFT; // calculate end of sample

                // now loop from srcX to srcX2 and add up the values for each channel
                do {
                    final int argb = srcPixels[srcX + destY * srcW];
                    a += (argb & ALPHA) >>> 24; // alpha channel
                    r += argb & RED; // red channel
                    g += argb & GREEN; // green channel
                    b += argb & BLUE; // blue channel
                    ++count; // count the pixel
                    ++srcX; // move on to the next pixel
                } while (srcX <= srcX2 && srcX + destY * srcW < srcPixels.length);

                // average out the channel values
                // recreate color from the averaged channels and place it into the destination buffer
                r >>>= 16;
                g >>>= 8;
                if (count == predictedCount) {
                    srcPixels[destX + destY * destW] = (lut[a] << 24) | (lut[r] << 16) | (lut[g] << 8) | lut[b];
                } else {
                    a /= count;
                    r /= count;
                    g /= count;
                    b /= count;
                    srcPixels[destX + destY * destW] = ((a << 24) | (r << 16) | (g << 8) | b);
                }
            }
        }

        return srcPixels;
    }

    private static Image resizeImageVertically(final int[] srcPixels, final int srcW, final int srcH, final int destH) {
        // create pixel arrays
//        final int[] destPixels = new int[srcW * destH]; // array to hold destination pixels
        // precalculate src/dest ratios
        final int ratioH = (srcH << FP_SHIFT) / destH;
        final int predictedCount = 1 + (srcH / destH);
        final int[] lut = new int[predictedCount << 8];

        // Init division lookup table
        for (int i = 0; i < lut.length; i++) {
            lut[i] = i / predictedCount;
        }
        // vertical resampling of the temporary buffer (which has been horizontally resampled)
        for (int destX = 0; destX < srcW; ++destX) {
            for (int destY = 0; destY < destH; ++destY) {
                int count = 0;
                int a = 0;
                int r = 0;
                int b = 0;
                int g = 0; // initialize color blending vars
                int srcY = (destY * ratioH) >> FP_SHIFT; // calculate beginning of sample
                final int srcY2 = ((destY + 1) * ratioH) >> FP_SHIFT; // calculate end of sample

                // now loop from srcY to srcY2 and add up the values for each channel
                do {
                    final int argb = srcPixels[destX + srcY * srcW];
                    a += (argb & ALPHA) >>> 24; // alpha channel
                    r += argb & RED; // red channel
                    g += argb & GREEN; // green channel
                    b += argb & BLUE; // blue channel
                    ++count; // count the pixel
                    ++srcY; // move on to the next pixel
                } while (srcY <= srcY2 && destX + srcY * srcW < srcPixels.length);

                // average out the channel values
                r >>>= 16;
                g >>>= 8;
                if (count == predictedCount) {
                    srcPixels[destX + destY * srcW] = (lut[a] << 24) | (lut[r] << 16) | (lut[g] << 8) | lut[b];
                } else {
                    a /= count;
                    r /= count;
                    g /= count;
                    b /= count;
                    srcPixels[destX + destY * srcW] = (a << 24) | (r << 16) | (g << 8) | b;
                }
            }
        }

        // return a new image created from the destination pixel buffer
        return Image.createRGBImage(srcPixels, srcW, destH, true);
    }
}
