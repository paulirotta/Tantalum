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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Graphics;

/**
 * Abstract View class
 * @author ssaa
 */
public abstract class View implements CommandListener {
    public static final int SCROLL_BAR_WIDTH = 4;
    public static final int SCROLL_BAR_COLOR = 0x44000000;
    protected final RSSReaderCanvas canvas;
    protected int renderY;

    public View(final RSSReaderCanvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Renders the view using the Graphics object
     * @param g
     */
    public abstract void render(Graphics g, int width, int height);

    /**
     * Returns array of Commands related to this view
     * @return Command[]
     */
    public abstract Command[] getCommands();

    public int getRenderY() {
        return renderY;
    }

    public void setRenderY(final int renderY) {
        this.renderY = renderY;
    }

    public void renderScrollBar(final Graphics g, final int contentHeight) {
        final int visibleAreaHeight = canvas.getHeight();

        if (contentHeight < visibleAreaHeight) {
            //no need for a scrollbar
            return;
        }

        //fill background with transparent color
        g.setColor(SCROLL_BAR_COLOR);
        g.fillRect(canvas.getWidth() - SCROLL_BAR_WIDTH, 0, SCROLL_BAR_WIDTH, canvas.getHeight());

        final int barY = -renderY * canvas.getHeight() / contentHeight;
        final int barHeight = Math.max(2, canvas.getHeight() * canvas.getHeight() / contentHeight);

        //fill bar
        g.setColor(RSSReader.COLOR_HIGHLIGHTED_BACKGROUND);
        g.fillRect(canvas.getWidth() - SCROLL_BAR_WIDTH, barY, SCROLL_BAR_WIDTH, barHeight);
    }
}
