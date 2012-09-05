/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.util;

import com.futurice.tantalum3.log.L;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Only one image processing routine will be active at a time. This is enforced
 * by internal synchronization, so image processing is thread safe with minimal
 * peak memory usage if you call these routines from multiple threads.
 *
 * @author phou
 */
public final class ImageUtils {

    private static final int M1 = 127 | 127 << 8 | 127 << 16 | 127 << 24;
    private static final int M2 = 63 | 63 << 8 | 63 << 16 | 63 << 24;
    private static final int M3 = 31 | 31 << 8 | 31 << 16 | 31 << 24;
    private static final int FP_SHIFT = 13;
    private static final int ALPHA = 0xFF000000;
    private static final int RED = 0x00FF0000;
    private static final int GREEN = 0x0000FF00;
    private static final int BLUE = 0x000000FF;

    /**
     * Return an image which is smaller then the original.
     *
     * The destination size can be defined exactly, or to fit within a bounding
     * box with aspect ratio preserved.
     *
     * To ensure your application does not use too much memory to scale the
     * image at the same time other parts of the program have a peak memory
     * usage, use the following calling pattern, modify as appropriate for your
     * needs.
     *
     * synchronized (Worker.LARGE_MEMORY_MUTEX) {
     * 
     * int[] data = new int[w * h];
     * 
     * image.getRGB(data, 0, w, 0, 0, w, h);
     * 
     * image = null;
     * 
     * image = ImageUtils.downscaleImage(data, w, h, maxW, maxH, true, false, false);
     * 
     * data = null;
     * 
     * }
     *
     * @param data - ARGB data for the image
     * @param srcW - Source data row width
     * @param srcH - Source data column height
     * @param maxW - maximum bounding width of scaled image
     * @param maxH - maximum bounding size of scaled image
     * @param preserveAspectRatio - set true except for special effects
     * @param processAlpha - set true for translucent PNG images, false for JPG
     * @param bestQuality - set true for roughly 4x slower, more accurate
     * scaling
     * @return
     */
    public static Image downscaleImage(final int[] data, int srcW, int srcH, int maxW, int maxH, final boolean preserveAspectRatio, final boolean processAlpha, final boolean bestQuality) {
        final float byWidth = maxW / (float) srcW;
        final float byHeight = maxH / (float) srcH;
        boolean widthIsMaxed = false;

        if (preserveAspectRatio) {
            if (byWidth <= byHeight) {
                maxW = (int) (srcW * byWidth);
                maxH = (int) (srcH * byWidth);
            } else {
                maxW = (int) (srcW * byHeight);
                maxH = (int) (srcH * byHeight);
            }
        }
        if (maxW >= srcW) {
            maxW = srcW;
            widthIsMaxed = true;
        }
        if (maxH >= srcH) {
            if (widthIsMaxed) {
                // No resize needed
                //#debug
                L.i("No image downscale needed", "(" + srcW + "," + srcH + ") -> (" + maxW + "," + maxH);
                maxH = srcH;
                return Image.createRGBImage(data, maxW, maxH, processAlpha);
            }
            maxH = srcH;
        }
        if (bestQuality) {
            if (processAlpha) {
                ImageUtils.pureDownscale(data, srcW, srcH, maxW, maxH, preserveAspectRatio);
            } else {
                ImageUtils.pureOpaqueDownscale(data, srcW, srcH, maxW, maxH, preserveAspectRatio);
            }
        } else {
            while (srcW >> 1 >= maxW && srcH >> 1 >= maxH) {
                ImageUtils.half(data, srcW, srcW >>= 1, srcH >>= 1);
            }
            if (srcW != maxW || srcH != maxH) {
                ImageUtils.downscale(data, srcW, srcH, maxW, maxH);
            }
        }

        return Image.createRGBImage(data, maxW, maxH, processAlpha);
    }

    /**
     * Draw at center anchor point
     *
     * @param g
     * @param x
     * @param y
     * @param data
     * @param srcW
     * @param srcH
     * @param maxW
     * @param maxH
     * @param processAlpha
     */
    public static void drawFlipshade(final Graphics g, final int x, final int y, final int[] data, int srcW, int srcH, int maxW, int maxH, final boolean processAlpha) {
        while (srcW >> 1 >= maxW && srcH >> 1 >= maxH) {
            ImageUtils.half(data, srcW, srcW >>= 1, srcH >>= 1);
        }
        maxW = Math.min(srcW, maxW);
        maxH = Math.min(srcH, maxH);
        if (srcW != maxW || srcH != maxH) {
            ImageUtils.downscale(data, srcW, srcH, maxW, maxH);
        }
        g.drawRGB(data, 0, maxW, x - (maxW >> 1), y - (maxH >> 1), maxW, maxH, processAlpha);
    }

    /**
     * Return an ARGB image where width and height are half the original.
     *
     * 4 pixels are combined into 1 with 6 bit accuracy.
     *
     * @param in
     * @param srcW
     * @return
     */
    private static void half(final int[] in, final int srcW, final int w, final int h) {
        int x, y = 0, z = 0, i;

        for (; y < h; y++) {
            i = (y << 1) * srcW;
            for (x = 0; x < w; x++) {
                final int e = (in[i++] >>> 2 & M2) + (in[i] >>> 2 & M2);
                i += srcW;
                in[z++] = e + (in[i--] >>> 2 & M2) + (in[i++] >>> 2 & M2);
                i++;
                i -= srcW;
            }
        }
    }

    /**
     * Special thanks to Dr Teemu Korhonen for the original, very fast Matlab
     * algorithm and tests. A weighted "X" is slid across the source image to
     * generate destination pixels.
     *
     * @param in
     * @param srcW
     * @param srcH
     * @param w
     * @param h
     */
    private static void downscale(final int[] in, final int srcW, final int srcH, final int w, final int h) {
        final float dx = srcW / (float) (w + 1);
        final float dy = srcH / (float) (h + 1);
        int x, y = 0, z = 0, e;

        for (; y < h - 1; y++) {
            final int rowstart = 1 + srcW + (srcW * (int) (y * dy));
            for (x = 0; x < w; x++) {
                int i = rowstart + (int) (x * dx);
                e = in[i--] >>> 1 & M1;
                i -= srcW;
                e += (in[i++] >>> 3 & M3) + (in[++i] >>> 3 & M3);
                i += srcW << 1;
                in[z++] = e + (in[i--] >>> 3 & M3) + (in[--i] >>> 3 & M3);
            }
        }
    }

    /**
     * Additive blending shrinkImage, 8 bit accuracy. For speed, integers are
     * used with fixed point accuracy instead of floats.
     *
     * Gets a source image along with new size for it and resizes it to fit
     * within max dimensions.
     *
     * @param data - ARGB image
     * @param srcW - source image width
     * @param srcH - source image height
     * @param w - final image width
     * @param h - final image height
     * @param preserveAspectRatio
     */
    private static void pureDownscale(final int[] data, final int srcW, final int srcH, final int w, final int h, final boolean preserveAspectRatio) {
        final int predictedCount = 1 + (srcW / w);
        final int[] lut = new int[predictedCount << 8];

        // Init division lookup table
        for (int i = 0; i < lut.length; i++) {
            lut[i] = i / predictedCount;
        }
        {
            // precalculate src/dest ratios
            final int ratioW = (srcW << FP_SHIFT) / w;

            // horizontal resampling (srcY = destY)
            for (int destY = 0; destY < srcH; ++destY) {
                final int srcRowStartIndex = destY * srcW;
                final int destRowStartIndex = destY * w;

                for (int destX = 0; destX < w; ++destX) {
                    int srcX = (destX * ratioW) >> FP_SHIFT; // calculate beginning of sample
                    final int initialSrcX = srcX;
                    final int srcX2 = ((destX + 1) * ratioW) >> FP_SHIFT; // calculate end of sample
                    int a = 0;
                    int r = 0;
                    int g = 0;
                    int b = 0;

                    // now loop from srcX to srcX2 and add up the values for each channel
                    do {
                        final int argb = data[srcX + srcRowStartIndex];
                        a += (argb & ALPHA) >>> 24;
                        r += argb & RED;
                        g += argb & GREEN;
                        b += argb & BLUE;
                        ++srcX; // move on to the next pixel
                    } while (srcX <= srcX2 && srcX + srcRowStartIndex < data.length);

                    // average out the channel values
                    // recreate color from the averaged channels and place it into the destination buffer
                    r >>>= 16;
                    g >>>= 8;
                    final int count = srcX - initialSrcX;
                    if (count == predictedCount) {
                        data[destX + destRowStartIndex] = (lut[a] << 24) | (lut[r] << 16) | (lut[g] << 8) | lut[b];
                    } else {
                        a /= count;
                        r /= count;
                        g /= count;
                        b /= count;
                        data[destX + destRowStartIndex] = ((a << 24) | (r << 16) | (g << 8) | b);
                    }
                }
            }
        }

        // precalculate src/dest ratios
        final int predictedCount2;
        final int[] lut2;
        if (preserveAspectRatio) {
            predictedCount2 = predictedCount;
            lut2 = lut;
        } else {
            predictedCount2 = 1 + (srcH / h);
            lut2 = new int[predictedCount2 << 8];

            // Init division lookup table
            for (int i = 0; i < lut2.length; i++) {
                lut2[i] = i / predictedCount2;
            }
        }
        // vertical resampling (srcX = destX)
        final int ratioH = (srcH << FP_SHIFT) / h;
        for (int destX = 0; destX < w; ++destX) {
            for (int destY = 0; destY < h; ++destY) {
                int srcY = (destY * ratioH) >> FP_SHIFT; // calculate beginning of sample
                final int initialSrcY = srcY;
                final int srcY2 = ((destY + 1) * ratioH) >> FP_SHIFT; // calculate end of sample
                int a = 0;
                int r = 0;
                int g = 0;
                int b = 0;

                // now loop from srcY to srcY2 and add up the values for each channel
                do {
                    final int argb = data[destX + srcY * w];
                    a += (argb & ALPHA) >>> 24;
                    r += argb & RED;
                    g += argb & GREEN;
                    b += argb & BLUE;
                    ++srcY; // move on to the next pixel
                } while (srcY <= srcY2 && destX + srcY * w < data.length);

                // average out the channel values
                r >>>= 16;
                g >>>= 8;
                final int count = srcY - initialSrcY;
                if (count == predictedCount2) {
                    data[destX + destY * w] = (lut2[a] << 24) | (lut2[r] << 16) | (lut2[g] << 8) | lut2[b];
                } else {
                    a /= count;
                    r /= count;
                    g /= count;
                    b /= count;
                    data[destX + destY * w] = (a << 24) | (r << 16) | (g << 8) | b;
                }
            }
        }
    }

    /**
     * Additive blending shrinkImage, 8 bit accuracy. Slightly faster because
     * Alpha is not calculated.
     *
     * @param data - Opaque RGB image
     * @param srcW - source image width
     * @param srcH - source image height
     * @param w - final image width
     * @param h - final image height
     * @param preserveAspectRatio
     */
    private static void pureOpaqueDownscale(final int[] data, final int srcW, final int srcH, final int w, final int h, final boolean preserveAspectRatio) {
        final int predictedCount = 1 + (srcW / w);
        final int[] lut = new int[predictedCount << 8];

        // Init division lookup table
        for (int i = 0; i < lut.length; i++) {
            lut[i] = i / predictedCount;
        }
        {
            // precalculate src/dest ratios
            final int ratioW = (srcW << FP_SHIFT) / w;

            // horizontal resampling (srcY = destY)
            for (int destY = 0; destY < srcH; ++destY) {
                final int srcRowStartIndex = destY * srcW;
                final int destRowStartIndex = destY * w;

                for (int destX = 0; destX < w; ++destX) {
                    int srcX = (destX * ratioW) >> FP_SHIFT; // calculate beginning of sample
                    final int initialSrcX = srcX;
                    final int srcX2 = ((destX + 1) * ratioW) >> FP_SHIFT; // calculate end of sample
                    int r = 0;
                    int g = 0;
                    int b = 0;

                    // now loop from srcX to srcX2 and add up the values for each channel
                    do {
                        final int rgb = data[srcRowStartIndex + srcX];
                        r += rgb & RED;
                        g += rgb & GREEN;
                        b += rgb & BLUE;
                        ++srcX; // move on to the next pixel
                    } while (srcX <= srcX2 && srcRowStartIndex + srcX < data.length);

                    // average out the channel values
                    // recreate color from the averaged channels and place it into the destination buffer
                    r >>>= 16;
                    g >>>= 8;
                    final int count = srcX - initialSrcX;
                    if (count == predictedCount) {
                        data[destX + destRowStartIndex] = (lut[r] << 16) | (lut[g] << 8) | lut[b];
                    } else {
                        r /= count;
                        g /= count;
                        b /= count;
                        data[destX + destRowStartIndex] = (r << 16) | (g << 8) | b;
                    }
                }
            }
        }

        // precalculate src/dest ratios
        final int predictedCount2;
        final int[] lut2;
        if (preserveAspectRatio) {
            predictedCount2 = predictedCount;
            lut2 = lut;
        } else {
            predictedCount2 = 1 + (srcH / h);
            lut2 = new int[predictedCount2 << 8];

            // Init division lookup table
            for (int i = 0; i < lut2.length; i++) {
                lut2[i] = i / predictedCount2;
            }
        }
        // vertical resampling (srcX = destX)
        final int ratioH = (srcH << FP_SHIFT) / h;
        for (int destX = 0; destX < w; ++destX) {
            for (int destY = 0; destY < h; ++destY) {
                int srcY = (destY * ratioH) >> FP_SHIFT; // calculate beginning of sample
                final int initialSrcY = srcY;
                final int columnStart = srcY * w;
                final int srcY2 = ((destY + 1) * ratioH) >> FP_SHIFT; // calculate end of sample
                int r = 0;
                int g = 0;
                int b = 0;

                // now loop from srcY to srcY2 and add up the values for each channel
                do {
                    final int argb = data[columnStart + destX];
                    r += argb & RED;
                    g += argb & GREEN;
                    b += argb & BLUE;
                    ++srcY; // move on to the next pixel
                } while (srcY <= srcY2 && columnStart + destX < data.length);

                // average out the channel values
                r >>>= 16;
                g >>>= 8;
                final int count = srcY - initialSrcY;
                if (count == predictedCount2) {
                    data[destX + destY * w] = (lut2[r] << 16) | (lut2[g] << 8) | lut2[b];
                } else {
                    r /= count;
                    g /= count;
                    b /= count;
                    data[destX + destY * w] = (r << 16) | (g << 8) | b;
                }
            }
        }
    }
}
