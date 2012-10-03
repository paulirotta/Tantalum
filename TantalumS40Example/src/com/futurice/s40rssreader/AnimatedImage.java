/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.s40rssreader;

import com.futurice.tantalum4.util.ImageUtils;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 *
 * @author phou
 */
public class AnimatedImage {

    public final Image image;
    private float w;
    private float h;
    private final int endW;
    private final int endH;
    private final int numberOfAnimationFrames;
    private final float deltaW;
    private final float deltaH;
    private boolean animationComplete = false;
    private int frame = 0;
    private static int[] data;

    public AnimatedImage(final Image image, final int startW, final int startH, final int endW, final int endH, final int numberOfAnimationFrames) {
        this.image = image;
        this.w = startW;
        this.h = startH;
        this.endW = endW;
        this.endH = endH;
        this.numberOfAnimationFrames = numberOfAnimationFrames;
        this.deltaW = (endW - startW) / (float) numberOfAnimationFrames;
        this.deltaH = (endH - startH) / (float) numberOfAnimationFrames;
    }

    public boolean animate(final Graphics g, final int x, final int y) {
        if (data == null || data.length < image.getWidth() * image.getHeight()) {
            data = new int[image.getWidth() * image.getHeight()];
        }
        image.getRGB(data, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
        ImageUtils.drawFlipshade(g, x, y, data, image.getWidth(), image.getHeight(), (int) w, (int) h, false);
        animationComplete = ++frame >= numberOfAnimationFrames;
        if (!animationComplete) {
            // Linear
            this.w += deltaW;
            this.h += deltaH;
        } else {
            this.w = endW;
            this.h = endH;
        }

        return animationComplete;
    }
}
