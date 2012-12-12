package org.tantalum.s40rssreader;

//#ifndef Profile
import com.nokia.mid.ui.DirectUtils;
//#endif
import org.tantalum.tantalum4.log.L;
import org.tantalum.tantalum4.net.xml.RSSItem;
import org.tantalum.tantalum4.util.PoolingWeakImageHashCache;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

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

    public VerticalListView(final RSSReaderCanvas canvas) {
        super(canvas);

        try {
            updateCommand = (Command) Class.forName("org.tantalum.s40rssreader.UpdateIconCommand").newInstance();        
        } catch (Throwable t) {
            //#debug
            L.e("IconCommand not supported", "Update", t);
        }
    }

    public Command[] getCommands() {
        return new Command[]{updateCommand, exitCommand, clearCacheCommand, prefetchImagesCommand};
    }

    public void commandAction(final Command command, final Displayable d) {
        if (command == exitCommand) {
            canvas.getRssReader().exitMIDlet(false);
        } else if (command == updateCommand) {
            reload(true);
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
     * Renders one item at the specified y-position
     *
     * @param g
     * @param curY
     * @param item
     * @param selected
     */
//    private void renderItem(Graphics g, final int curY, final DeprecatedRSSItem item, final boolean selected) {
//        if (selected) {
//            g.setColor(RSSReaderCanvas.COLOR_HIGHLIGHTED_BACKGROUND);
//            g.fillRect(0, curY, canvas.getWidth(), ITEM_HEIGHT);
//            g.setColor(RSSReaderCanvas.COLOR_HIGHLIGHTED_FOREGROUND);
//        } else {
//            g.setColor(RSSReaderCanvas.COLOR_FOREGROUND);
//        }
//
//        g.setFont(RSSReaderCanvas.FONT_TITLE);
//        g.drawString(item.getTruncatedTitle(), RSSReaderCanvas.MARGIN, curY + RSSReaderCanvas.MARGIN, Graphics.LEFT | Graphics.TOP);
//
//        g.setFont(RSSReaderCanvas.FONT_DATE);
//        g.drawString(item.getPubDate(), RSSReaderCanvas.MARGIN, curY + RSSReaderCanvas.MARGIN + RSSReaderCanvas.FONT_TITLE.getHeight(), Graphics.LEFT | Graphics.TOP);
//
//        g.setColor(RSSReaderCanvas.COLOR_BORDER);
//        g.drawLine(0, curY + ITEM_HEIGHT, canvas.getWidth(), curY + ROW_HEIGHT);
//    }
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
            //#ifndef Profile
            image = DirectUtils.createImage(width, ROW_HEIGHT, selected ? 0xFF000000 | RSSReader.COLOR_HIGHLIGHTED_BACKGROUND : 0xFF000000 | RSSReader.COLOR_BACKGROUND);
            //#else
//#             image = Image.createImage(width, ROW_HEIGHT);
            //#endif
            g = image.getGraphics();
            this.renderCache.put(item, image);
        }

        g.setColor(selected ? RSSReader.COLOR_HIGHLIGHTED_FOREGROUND : RSSReader.COLOR_FOREGROUND);
        g.setFont(RSSReaderCanvas.FONT_TITLE);
        final int w = width - 2 * RSSReaderCanvas.MARGIN;
        g.drawString(item.getTruncatedTitle(RSSReaderCanvas.FONT_TITLE, w), RSSReaderCanvas.MARGIN, RSSReaderCanvas.MARGIN, Graphics.LEFT | Graphics.TOP);
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

    protected void doClearCache() {
        super.doClearCache();

        renderCache.clear();
    }
}
