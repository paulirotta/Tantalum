package com.futurice.s40rssreader;

import com.futurice.tantalum2.Result;
import com.futurice.tantalum2.Workable;
import com.futurice.tantalum2.Worker;
import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.net.StaticWebCache;
import com.futurice.tantalum2.net.xml.RSSItem;
import com.futurice.tantalum2.net.xml.RSSModel;
import com.futurice.tantalum2.rms.DataTypeHandler;
import com.futurice.tantalum2.rms.RMSUtils;
import com.futurice.tantalum2.util.PoolingWeakHashCache;
import com.nokia.mid.ui.DirectUtils;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import org.xml.sax.SAXException;

/**
 * View for rendering list of RSS items
 *
 * @author ssaa
 */
public final class ListView extends View {

    private static boolean prefetchImages = false;
    private final int ITEM_HEIGHT = RSSReaderCanvas.FONT_TITLE.getHeight()
            + RSSReaderCanvas.FONT_DATE.getHeight() + 2 * RSSReaderCanvas.MARGIN;
    private final Command exitCommand = new Command("Exit", Command.EXIT, 0);
    private final Command reloadCommand = new Command("Reload", Command.ITEM, 0);
    private final Command settingsCommand = new Command("Settings", Command.ITEM, 1);
    private final Command clearCacheCommand = new Command("Clear Cache", Command.SCREEN, 5);
    private final Command prefetchImagesCommand = new Command("Prefetch Images", Command.SCREEN, 2);
    private final LiveUpdateRSSModel rssModel = new LiveUpdateRSSModel();
    private final StaticWebCache feedCache;
    private int selectedIndex = -1;
    private final PoolingWeakHashCache renderCache = new PoolingWeakHashCache();

    public ListView(final RSSReaderCanvas canvas) {
        super(canvas);

        feedCache = new StaticWebCache('5', new DataTypeHandler() {

            public Object convertToUseForm(byte[] bytes) {
                try {
                    rssModel.removeAllElements();
                    rssModel.setXML(bytes);

                    return rssModel;
                } catch (Exception e) {
                    Log.l.log("Error in parsing XML", rssModel.toString());
                    return null;
                }
            }
        });
    }

    /**
     * Network access type of active connection or a set default access point.
     *
     * pd, pd.EDGE, pd.3G, pd.HSDPA, csd, bt_pan, wlan, na (can't tell)
     *
     * @return
     */
    public static void checkForWLAN() {
        final String status = System.getProperty("com.nokia.network.access");

        prefetchImages |= status != null && status.equals("wlan");
    }

    public Command[] getCommands() {
        return new Command[]{reloadCommand, settingsCommand, exitCommand, clearCacheCommand};
    }

    public void commandAction(final Command command, final Displayable d) {
        if (command == exitCommand) {
            canvas.getRssReader().exitMIDlet(false);
        } else if (command == reloadCommand) {
            reload(true);
        } else if (command == clearCacheCommand) {
            clearCache();
        } else if (command == prefetchImagesCommand) {
            prefetchImages = true;
            clearCache();
        } else if (command == settingsCommand) {
            String feedUrl = RSSReader.INITIAL_FEED_URL;

            try {
                final byte[] bytes = RMSUtils.read("settings");

                if (bytes != null) {
                    feedUrl = bytes.toString();
                }
            } catch (Exception e) {
                Log.l.log("Can not read settings", "", e);
            }
            canvas.getRssReader().getSettingsForm().setUrlValue(feedUrl);
            canvas.getRssReader().switchDisplayable(null, canvas.getRssReader().getSettingsForm());
        }
    }

    private void clearCache() {
        Worker.queue(new Workable() {

            public boolean work() {
                renderCache.clear();
                feedCache.clear();
                DetailsView.imageCache.clear();
                reload(true);

                return false;
            }
        });
    }

    /**
     * Called when the list of items changes
     */
    public void notifyListChanged() {
        canvas.repaint();
    }

    public void canvasSizeChanged() {
        this.renderCache.clear();
    }

    /**
     * Renders the list of rss feed items
     *
     * @param g
     */
    public void render(final Graphics g, final int width, final int height) {
        try {
            final int contentHeight = rssModel.size() * ITEM_HEIGHT;

            //limit the renderY not to keep the content on the screen
            if (contentHeight < height) {
                this.renderY = 0;
            } else if (this.renderY < -contentHeight + height) {
                this.renderY = -contentHeight + height;
            } else if (this.renderY > 0) {
                this.renderY = 0;
            }

            if (rssModel.size() == 0) {
                g.setColor(RSSReader.COLOR_BACKGROUND);
                g.fillRect(0, 0, width, height);
                g.setColor(RSSReader.COLOR_FOREGROUND);
                g.drawString("Loading...", canvas.getWidth() >> 1, canvas.getHeight() >> 1, Graphics.BASELINE | Graphics.HCENTER);
                return;
            }

            int curY = this.renderY;

            //start renreding from the first visible item
            int startIndex = -this.renderY / ITEM_HEIGHT;
            curY += startIndex * ITEM_HEIGHT;

            for (int i = startIndex; i < rssModel.size(); i++) {
                if (curY > -ITEM_HEIGHT) {
//                    renderItem(g, curY, (DeprecatedRSSItem) rssModel.elementAt(i), i == selectedIndex);
                    final RSSItem item = rssModel.elementAt(i);
                    Image itemImage = (Image) this.renderCache.get(item);

                    if (itemImage == null) {
                        itemImage = getItemImage(item, width, i == selectedIndex);
                    }
                    g.drawImage(itemImage, 0, curY, Graphics.TOP | Graphics.LEFT);
                } else {
                    // Reduce load on the garbage collector when scrolling
                    renderCache.remove(rssModel.elementAt(i));
                }
                curY += ITEM_HEIGHT;

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
            Log.l.log("Render error", rssModel.toString(), e);
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
//        g.drawLine(0, curY + ITEM_HEIGHT, canvas.getWidth(), curY + ITEM_HEIGHT);
//    }
    private Image getItemImage(final RSSItem item, final int width, final boolean selected) {
        Image image = (Image) this.renderCache.getFromPool();
        final Graphics g;

        if (image != null) {
            g = image.getGraphics();
            g.setColor(selected ? RSSReader.COLOR_HIGHLIGHTED_BACKGROUND : RSSReader.COLOR_BACKGROUND);
            g.fillRect(0, 0, width, ITEM_HEIGHT);
        } else {
            image = DirectUtils.createImage(width, ITEM_HEIGHT, selected ? 0xFF000000 | RSSReader.COLOR_HIGHLIGHTED_BACKGROUND : 0xFF000000 | RSSReader.COLOR_BACKGROUND);
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
        g.drawLine(0, ITEM_HEIGHT, canvas.getWidth(), ITEM_HEIGHT);

        return image;
    }

    /**
     * Reloads the feed
     */
    public void reload(final boolean forceNetLoad) {
        this.renderY = 0;
        this.renderCache.clear();

        String feedUrl = RSSReader.INITIAL_FEED_URL;

        try {
            final byte[] bytes = RMSUtils.read("settings");
            if (bytes != null) {
                feedUrl = bytes.toString();
            }
        } catch (Exception e) {
            Log.l.log("Can not read settings", "", e);
        }
        if (forceNetLoad) {
            feedCache.update(feedUrl, new Result() {

                public void setResult(final Object o) {
                    notifyListChanged();
                }
            });
        } else {
            feedCache.get(feedUrl, new Result() {

                public void setResult(final Object o) {
                    notifyListChanged();
                }
            });
        }
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
        final int pointedIndex = y / ITEM_HEIGHT;

        if (pointedIndex >= 0 && pointedIndex < rssModel.size()) {
            setSelectedIndex(pointedIndex);
            if (tapped) {
                canvas.showDetails((RSSItem) rssModel.elementAt(this.selectedIndex));
            } else {
                canvas.repaint();
            }
        }
    }

    public void deselectItem() {
        if (setSelectedIndex(-1)) {
            canvas.repaint();
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

    private class LiveUpdateRSSModel extends RSSModel {

        private final Runnable updateRunnable = new Runnable() {

            public void run() {
                RSSReaderCanvas.getInstance().getListView().notifyListChanged();
            }
        };

        public void setXML(final byte[] xml) throws SAXException, IllegalArgumentException {
            checkForWLAN(); // If this just came in over a WLAN net, get the images also
            super.setXML(xml);
        }

        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            if (currentItem != null && qName.equals("item")) {
                Worker.queueEDT(updateRunnable);
                if (prefetchImages) {
                    DetailsView.imageCache.prefetch(currentItem.getThumbnail());
                }
            }

            super.endElement(uri, localName, qName);
        }
    }
}
