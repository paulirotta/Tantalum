/*
 Copyright (c) 2013, Paul Houghton and Futurice Oy
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
package org.tantalum.canvasrssreader;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import org.tantalum.jme.JMEImageUtils;

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
        JMEImageUtils.drawFlipshade(g, x, y, data, data, image.getWidth(), image.getHeight(), (int) w, (int) h, false);
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
