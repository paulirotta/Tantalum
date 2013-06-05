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

import com.nokia.mid.ui.DirectUtils;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import org.tantalum.PlatformUtils;
import org.tantalum.net.xml.RSSItem;
import org.tantalum.storage.FlashDatabaseException;
import org.tantalum.util.L;

/**
 * View for rendering list of RSS items
 *
 * @author ssaa
 */
public final class VerticalListView extends RSSListView {

    private static final int ROW_HEIGHT = RSSReaderCanvas.FONT_TITLE.getHeight()
            + RSSReaderCanvas.FONT_DATE.getHeight() + 2 * RSSReaderCanvas.MARGIN;
    private final Command exitCommand = new Command("Exit", Command.EXIT, 0);
    private Command updateCommand = new Command("Update", Command.OK, 0);
    private final Command clearCacheCommand = new Command("Clear Cache", Command.SCREEN, 5);
    private final Command prefetchImagesCommand = new Command("Prefetch Images", Command.SCREEN, 2);
    private int selectedIndex = -1;
    private RSSItem[] modelCopy = null;
    protected final PoolingWeakImageHashCache renderCache = new PoolingWeakImageHashCache();

    public VerticalListView(final RSSReaderCanvas canvas) throws FlashDatabaseException {
        super(canvas);

//        try {
//            updateCommand = (Command) Class.forName("org.tantalum.s40rssreader.UpdateIconCommand").newInstance();        
//        } catch (Throwable t) {
//            //#debug
//            L.e("IconCommand not supported", "Update", t);
//        }
    }

    public Command[] getCommands() {
        return new Command[]{updateCommand, exitCommand, clearCacheCommand, prefetchImagesCommand};
    }

    public void commandAction(final Command command, final Displayable d) {
        if (command == exitCommand) {
            PlatformUtils.getInstance().shutdown(false, "Exit command received");
        } else if (command == updateCommand) {
            reloadAsync(true);
        } else if (command == clearCacheCommand) {
            clearCache();
        } else if (command == prefetchImagesCommand) {
            prefetchImages = true;
            clearCache();
        }
    }

    /**
     * Renders the list of rss feed items
     *
     * @param g
     */
    public void render(final Graphics g, final int width, final int height) {
        try {
            modelCopy = rssModel.copy(modelCopy);
            if (modelCopy.length == 0) {
                g.setColor(RSSReader.COLOR_BACKGROUND);
                g.fillRect(0, 0, width, height);
                g.setColor(RSSReader.COLOR_FOREGROUND);
                g.drawString("Loading...", canvas.getWidth() >> 1, canvas.getHeight() >> 1, Graphics.BASELINE | Graphics.HCENTER);
                return;
            }

            final int contentHeight = modelCopy.length * ROW_HEIGHT;

            //limit the renderY not to keep the content on the screen
            if (contentHeight < height) {
                this.renderY = 0;
            } else if (this.renderY < -contentHeight + height) {
                this.renderY = -contentHeight + height;
            } else if (this.renderY > 0) {
                this.renderY = 0;
            }


            int curY = this.renderY;

            //start renreding from the first visible item
            int startIndex = -this.renderY / ROW_HEIGHT;
            curY += startIndex * ROW_HEIGHT;

            for (int i = startIndex; i < modelCopy.length; i++) {
                if (curY > -ROW_HEIGHT) {
                    Image itemImage = (Image) this.renderCache.get(modelCopy[i]);

                    if (itemImage == null) {
                        itemImage = createItemImage(modelCopy[i], width, i == selectedIndex);
                    }
                    g.drawImage(itemImage, 0, curY, Graphics.TOP | Graphics.LEFT);
                } else {
                    // Reduce load on the garbage collector when scrolling
                    renderCache.remove(modelCopy[i]);
                }
                curY += ROW_HEIGHT;

                //stop rendering below the screen
                if (curY > height) {
                    break;
                }
            }
            if (curY < height) {
                // Background fill below all items. For very short feeds
                g.setColor(RSSReader.COLOR_BACKGROUND);
                g.fillRect(0, curY, width, height);
            }

            renderScrollBar(g, contentHeight);
        } catch (Exception e) {
            //#debug
            L.e("VerticalList Render error", rssModel.toString(), e);
        }
    }

    /**
     * Get the paint-able image associated with this item. Often, this will come
     * from the cache to speed painting.
     *
     * @param item
     * @param width
     * @param selected
     * @return
     */
    private Image createItemImage(final RSSItem item, final int width, final boolean selected) {
        Image image = this.renderCache.getImageFromPool(width, ROW_HEIGHT);
        final Graphics g;

        if (image != null) {
            g = image.getGraphics();
            g.setColor(selected ? RSSReader.COLOR_HIGHLIGHTED_BACKGROUND : RSSReader.COLOR_BACKGROUND);
            g.fillRect(0, 0, width, ROW_HEIGHT);
        } else {
            image = DirectUtils.createImage(width, ROW_HEIGHT, selected ? 0xFF000000 | RSSReader.COLOR_HIGHLIGHTED_BACKGROUND : 0xFF000000 | RSSReader.COLOR_BACKGROUND);
            g = image.getGraphics();
            this.renderCache.put(item, image);
        }

        g.setColor(selected ? RSSReader.COLOR_HIGHLIGHTED_FOREGROUND : RSSReader.COLOR_FOREGROUND);
        g.setFont(RSSReaderCanvas.FONT_TITLE);
        final int w = width - 2 * RSSReaderCanvas.MARGIN;
        g.drawString(item.getTitle(), RSSReaderCanvas.MARGIN, RSSReaderCanvas.MARGIN, Graphics.LEFT | Graphics.TOP);
        g.setFont(RSSReaderCanvas.FONT_DATE);
        g.drawString(item.getPubDate(), RSSReaderCanvas.MARGIN, RSSReaderCanvas.MARGIN + RSSReaderCanvas.FONT_TITLE.getHeight(), Graphics.LEFT | Graphics.TOP);

        g.setColor(selected ? RSSReader.COLOR_HIGHLIGHTED_BORDER : RSSReader.COLOR_BORDER);
        g.drawLine(0, ROW_HEIGHT, canvas.getWidth(), ROW_HEIGHT);

        return image;
    }

    /**
     * Selects item at the specified x- and y-position (if any). If tapped makes
     * the selection, otherwise just repaints the highlighted item.
     *
     * @param x
     * @param y
     * @param tapped
     */
    public void selectItem(final int x, int y, boolean tapped) {
        y -= this.renderY;
        final int pointedIndex = y / ROW_HEIGHT;

        if (pointedIndex >= 0 && pointedIndex < rssModel.size()) {
            setSelectedIndex(pointedIndex);
            if (tapped) {
                canvas.showDetails((RSSItem) rssModel.elementAt(this.selectedIndex), 0);
            } else {
                canvas.refresh();
            }
        }
    }

    public void deselectItem() {
        if (setSelectedIndex(-1)) {
            canvas.refresh();
        }
    }

    public boolean setSelectedIndex(final int newIndex) {
        if (selectedIndex == newIndex) {
            return false;
        }
        if (selectedIndex >= 0) {
            renderCache.remove(rssModel.elementAt(selectedIndex));
        }
        if (newIndex >= 0) {
            renderCache.remove(rssModel.elementAt(newIndex));
        }
        selectedIndex = newIndex;

        return true;
    }

    public void canvasSizeChanged() {
        this.renderCache.clear();
    }

    protected void clearCache() {
        super.clearCache();

        renderCache.clear();
    }
}
