package com.futurice.s40rssreader;

import com.nokia.mid.ui.DirectGraphics;
import com.nokia.mid.ui.DirectUtils;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Graphics;

/**
 * Abstract View class
 * @author ssaa
 */
public abstract class View implements CommandListener {

    protected int renderY;
    protected final RSSReaderCanvas canvas;

    public View(final RSSReaderCanvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Renderds the view using the Graphics object
     * @param g
     */
    public abstract void render(final Graphics g);

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
        final DirectGraphics dg = DirectUtils.getDirectGraphics(g);
        dg.setARGBColor(0x44000000);
        g.fillRect(canvas.getWidth() - 4, 0, 4, canvas.getHeight());

        int barY = -renderY * canvas.getHeight() / contentHeight;
        int barHeight = canvas.getHeight() * canvas.getHeight() / contentHeight;

        if (barHeight < 2) barHeight = 2;

        //fill bar
        g.setColor(RSSReaderCanvas.COLOR_HIGHLIGHT);
        g.fillRect(canvas.getWidth() - 4, barY, 4, barHeight);
    }
}
