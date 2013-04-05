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

import java.util.Vector;
import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import org.tantalum.PlatformUtils;
import org.tantalum.Task;
import org.tantalum.jme.JMEFontUtils;
import org.tantalum.net.StaticWebCache;
import org.tantalum.net.xml.RSSItem;
import org.tantalum.storage.FlashDatabaseException;
import org.tantalum.util.L;

/**
 * View for rendering details of an RSS item
 *
 * @author ssaa
 */
public final class DetailsView extends View {

    public static final StaticWebCache imageCache = StaticWebCache.getWebCache('1', PlatformUtils.PHONE_DATABASE_CACHE, PlatformUtils.getInstance().getImageTypeHandler());
    private static final JMEFontUtils titleFontUtils = JMEFontUtils.getFontUtils(RSSReaderCanvas.FONT_TITLE, "...");
    private static final JMEFontUtils descriptionFontUtils = JMEFontUtils.getFontUtils(RSSReaderCanvas.FONT_DESCRIPTION, "...");

    private final Command openLinkCommand = new Command("Open link", Command.OK, 0);
    private final Command backCommand = new Command("Back", Command.BACK, 0);
    private int contentHeight;
    private volatile RSSItem currentItem; // Current article
    private volatile RSSItem leftItem; // Previous article, for image prefetch
    private volatile RSSItem rightItem; // Next article, for image prefetch
    private volatile Image leftIcon = null; // This reference prevents WeakReference garbage collect on previous article image
    private volatile Image rightIcon = null; // This reference prevents WeakReference garbage collect on next article image
    private volatile Image currentIcon = null; // This reference prevents WeakReference garbage collect on current article image
    private Image image; // Most recently used image (hard link prevents WeakReference gc)
    private int x = 0;

    public DetailsView(final RSSReaderCanvas canvas) {
        super(canvas);
    }

    public Command[] getCommands() {
        return new Command[]{openLinkCommand, backCommand};
    }

    public void commandAction(Command command, Displayable d) {
        if (command == openLinkCommand) {
            openLink();
        } else if (command == backCommand) {
            canvas.showList();
        }
    }

    /**
     * Opens a link in the browser for the selected RSS item
     *
     */
    private void openLink() {
        boolean needsToClose;
        final String url = currentItem.getLink();

        try {
            needsToClose = canvas.getRssReader().platformRequest(url);
            if (needsToClose) {
                PlatformUtils.getInstance().shutdown(false);
            }
        } catch (ConnectionNotFoundException ex) {
            //#debug
            L.e("Can not open browser to URL", url, ex);
        }
    }

    /*
     * Renders the details of the selected item
     */
    public void render(final Graphics g, int width, final int height) {
        final RSSItem item = currentItem;

        x >>= 1;
        if (contentHeight < canvas.getHeight()) {
            this.renderY = 0;
        } else if (this.renderY < -contentHeight + canvas.getHeight()) {
            this.renderY = -contentHeight + canvas.getHeight();
        } else if (this.renderY > 0) {
            this.renderY = 0;
        }

        int curY = renderY;

        g.setColor(RSSReader.COLOR_HIGHLIGHTED_BACKGROUND);
        g.fillRect(x, 0, x + width + SCROLL_BAR_WIDTH, height);

        g.setFont(RSSReaderCanvas.FONT_TITLE);
        g.setColor(RSSReader.COLOR_HIGHLIGHTED_FOREGROUND);
        final Vector lines = titleFontUtils.splitToLines(item.getTitle(), width - 2 * RSSReaderCanvas.MARGIN);
        curY = renderLines(g, x, curY, RSSReaderCanvas.FONT_TITLE.getHeight(), lines);

        if (!canvas.isPortrait()) {
            width >>= 1;
        }

        g.setFont(RSSReaderCanvas.FONT_DATE);
        g.drawString(item.getPubDate(), 10 + x, curY, Graphics.LEFT | Graphics.TOP);

        curY += RSSReaderCanvas.FONT_DATE.getHeight() << 1;

        g.setFont(RSSReaderCanvas.FONT_DESCRIPTION);
        final Vector lines2 = descriptionFontUtils.splitToLines(item.getDescription(), width - 2 * RSSReaderCanvas.MARGIN);
        curY = renderLines(g, x, curY, RSSReaderCanvas.FONT_DESCRIPTION.getHeight(), lines2);

        curY += RSSReaderCanvas.FONT_DESCRIPTION.getHeight();

        final String url = item.getThumbnail();
        if (url != null) {
            try {
                image = (Image) imageCache.synchronousRAMCacheGet(url);
            } catch (FlashDatabaseException ex) {
                //#debug
                L.e("Can not get image", url, ex);
            }
            if (image != null) {
                currentIcon = image;
                if (canvas.isPortrait()) {
                    g.drawImage(image, (x + canvas.getWidth()) >> 1, curY, Graphics.TOP | Graphics.HCENTER);
                    curY += image.getHeight() + RSSReaderCanvas.FONT_TITLE.getHeight();
                } else {
                    g.drawImage(image, (canvas.getWidth() >> 1) + (x + canvas.getWidth()) >> 1, renderY + height >> 1, Graphics.VCENTER | Graphics.HCENTER);
                }
            } else if (!item.isLoadingImage()) {
                // Not already loading image, so request it
                item.setLoadingImage(true);
                imageCache.getAsync(item.getThumbnail(), Task.HIGH_PRIORITY, StaticWebCache.GET_ANYWHERE, new Task() {
                    public Object exec(final Object in) {
                        item.setLoadingImage(false);
                        if (currentItem == item) {
                            currentIcon = (Image) in;
                        }
                        canvas.refresh();

                        return in;
                    }

                    public boolean cancel(boolean mayInterruptIfNeeded) {
                        item.setLoadingImage(false);

                        return false;
                    }
                });
            }
        }

        contentHeight = curY - renderY;

        if (x == 0) {
            renderScrollBar(g, contentHeight);
        } else {
            canvas.refresh();
        }
    }

    private int renderLines(final Graphics g, final int x, int curY, final int lineHeight, final Vector lines) {
        final int len = lines.size();

        for (int i = 0; i < len; i++) {
            g.drawString((String) lines.elementAt(i), RSSReaderCanvas.MARGIN + x, curY, Graphics.LEFT | Graphics.TOP);
            curY += lineHeight;
        }

        return curY;
    }

    /**
     * Set the current list item, and also the items immediately to the left and
     * right. Set the initial x offset for side scroll entry animation if
     * needed.
     *
     * @param currentItem
     * @param leftItem
     * @param rightItem
     * @param x
     */
    public void setCurrentItem(final RSSItem currentItem, final RSSItem leftItem, final RSSItem rightItem, final int x) {
        setRenderY(0);
        this.x = x;
        this.currentItem = currentItem;
        this.leftItem = leftItem;
        this.rightItem = rightItem;
        if (leftItem != null) {
            imageCache.getAsync(leftItem.getThumbnail(), Task.HIGH_PRIORITY, StaticWebCache.GET_ANYWHERE, new Task() {
                public Object exec(final Object params) {
                    final Object r = getValue();

                    if (DetailsView.this.leftItem == leftItem) {
                        leftIcon = (Image) r;
                    }

                    return r;
                }
            });
        }
        if (rightItem != null) {
            imageCache.getAsync(rightItem.getThumbnail(), Task.HIGH_PRIORITY, StaticWebCache.GET_WEB, new Task() {
                public Object exec(final Object params) {
                    final Object r = getValue();

                    if (DetailsView.this.rightItem == rightItem) {
                        rightIcon = (Image) r;
                    }

                    return r;
                }
            });
        }
    }

    /**
     * Detect and respond to horizontal flick gestures to change the current
     * article.
     *
     * @param flickDirection
     * @return s
     */
    public boolean horizontalFlick(final float flickDirection) {
        final float fd = Math.abs(flickDirection);
        final boolean horizontal = fd < Math.PI / 4 || fd > Math.PI * 3 / 4;
        if (horizontal) {
            boolean left = Math.abs(flickDirection) < Math.PI / 2;
            if (left) {
                if (leftItem != null) {
                    canvas.showDetails(leftItem, -canvas.getWidth());
                } else {
                    canvas.showList();
                }
            } else if (rightItem != null) {
                canvas.showDetails(rightItem, canvas.getWidth());
            } else {
                canvas.showList();
            }
        }

        return horizontal;
    }

    /**
     * Clear memory when the details view is hidden.
     *
     */
    public void hide() {
        this.currentItem = null;
        this.leftItem = null;
        this.rightItem = null;
        this.leftIcon = null;
        this.rightIcon = null;
        this.currentIcon = null;
    }
}
