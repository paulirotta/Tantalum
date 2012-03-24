package com.futurice.s40rssreader;

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
    protected int renderY;
    protected final RSSReaderCanvas canvas;

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

    public void setRenderY(int renderY) {
        this.renderY = renderY;
    }

    public RSSReaderCanvas getCanvas() {
        return canvas;
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
        g.setColor(RSSReaderCanvas.COLOR_HIGHLIGHTED_BACKGROUND);
        g.fillRect(canvas.getWidth() - SCROLL_BAR_WIDTH, barY, SCROLL_BAR_WIDTH, barHeight);
    }
}
