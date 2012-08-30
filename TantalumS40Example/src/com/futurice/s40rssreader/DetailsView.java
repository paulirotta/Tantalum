package com.futurice.s40rssreader;

import com.futurice.tantalum3.Task;
import com.futurice.tantalum3.Worker;
import com.futurice.tantalum3.log.Log;
import com.futurice.tantalum3.net.StaticWebCache;
import com.futurice.tantalum3.net.xml.RSSItem;
import com.futurice.tantalum3.rms.ImageTypeHandler;
import com.futurice.tantalum3.util.StringUtils;
import java.util.Vector;
import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * View for rendering details of an RSS item
 *
 * @author ssaa
 */
public final class DetailsView extends View {

    public static final StaticWebCache imageCache = new StaticWebCache('1', new ImageTypeHandler());
    private final Command openLinkCommand = new Command("Open link", Command.ITEM, 0);
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
                canvas.getRssReader().exitMIDlet(false);
            }
        } catch (ConnectionNotFoundException ex) {
            //#debug
            Log.l.log("Can not open browser to URL", url, ex);
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
        curY = renderLines(g, x, curY, RSSReaderCanvas.FONT_TITLE.getHeight(), StringUtils.splitToLines(new Vector(), item.getTitle(), RSSReaderCanvas.FONT_TITLE, width - 2 * RSSReaderCanvas.MARGIN));

        if (!canvas.isPortrait()) {
            width >>= 1;
        }

        g.setFont(RSSReaderCanvas.FONT_DATE);
        g.drawString(item.getPubDate(), 10 + x, curY, Graphics.LEFT | Graphics.TOP);

        curY += RSSReaderCanvas.FONT_DATE.getHeight() << 1;

        g.setFont(RSSReaderCanvas.FONT_DESCRIPTION);
        curY = renderLines(g, x, curY, RSSReaderCanvas.FONT_DESCRIPTION.getHeight(), StringUtils.splitToLines(new Vector(), item.getDescription(), RSSReaderCanvas.FONT_DESCRIPTION, width - 2 * RSSReaderCanvas.MARGIN));

        curY += RSSReaderCanvas.FONT_DESCRIPTION.getHeight();

        final String url = item.getThumbnail();
        if (url != null) {
            image = (Image) imageCache.synchronousRAMCacheGet(url);
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
                imageCache.get(item.getThumbnail(), new Task() {

                    public void set(final Object o) {
                        super.set(o);
                        item.setLoadingImage(false);
                        if (currentItem == item) {
                            currentIcon = (Image) o;
                        }
                        canvas.queueRepaint();
                    }

                    public void onCancel() {
                        item.setLoadingImage(false);
                    }
                }, Worker.HIGH_PRIORITY);
            }
        }

        contentHeight = curY - renderY;

        if (x == 0) {
            renderScrollBar(g, contentHeight);
        } else {
            canvas.queueRepaint();
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
            imageCache.get(leftItem.getThumbnail(), new Task() {

                public void set(final Object o) {
                    super.set(o);
                    if (DetailsView.this.leftItem == leftItem) {
                        leftIcon = (Image) o;
                    }
                }
            }, Worker.HIGH_PRIORITY);
        }
        if (rightItem != null) {
            imageCache.get(rightItem.getThumbnail(), new Task() {

                public void set(final Object o) {
                    super.set(o);
                    if (DetailsView.this.rightItem == rightItem) {
                        rightIcon = (Image) o;
                    }
                }
            }, Worker.HIGH_PRIORITY);
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
