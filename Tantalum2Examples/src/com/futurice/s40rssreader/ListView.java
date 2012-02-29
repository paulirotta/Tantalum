package com.futurice.s40rssreader;

import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.rms.DefaultResult;
import com.futurice.tantalum2.net.StaticWebCache;
import com.futurice.tantalum2.rms.DataTypeHandler;
import com.futurice.tantalum2.rms.RMSUtils;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;

/**
 * View for rendering list of RSS items
 *
 * @author ssaa
 */
public final class ListView extends View {

    private final int ITEM_HEIGHT = RSSReaderCanvas.FONT_TITLE.getHeight()
            + RSSReaderCanvas.FONT_DATE.getHeight() + 2 * RSSReaderCanvas.MARGIN;
    private final Command exitCommand = new Command("Exit", Command.EXIT, 0);
    private final Command reloadCommand = new Command("Reload", Command.ITEM, 0);
    private final Command settingsCommand = new Command("Settings", Command.ITEM, 1);
    private final RSSModel rssModel = new RSSModel();
    private final StaticWebCache staticWebCache;
    private boolean loading = true;
    private int selectedIndex = -1;

    public ListView(RSSReaderCanvas canvas) {
        super(canvas);

        staticWebCache = new StaticWebCache("rss", 1, 10, new DataTypeHandler() {

            public Object convertToUseForm(byte[] bytes) {
                try {
                    rssModel.removeAllElements();
                    rssModel.setXML(new String(bytes));

                    return rssModel;
                } catch (Exception e) {
                    Log.l.log("Error in parsing XML", rssModel.toString());
                    return null;
                }
            }
        });
    }

    public Command[] getCommands() {
        return new Command[]{reloadCommand, settingsCommand, exitCommand};
    }

    public void commandAction(Command command, Displayable d) {
        if (command == exitCommand) {
            canvas.getRssReader().exitMIDlet();
        } else if (command == reloadCommand) {
            reload(false);
        } else if (command == settingsCommand) {
            String feedUrl = RSSReader.INITIAL_FEED_URL;

            try {
                feedUrl = RMSUtils.readByteArray("settings").toString();
            } catch (Exception e) {
                Log.l.log("Can not read settings", "", e);
            }
            canvas.getRssReader().getSettingsForm().setUrlValue(feedUrl);
            canvas.getRssReader().switchDisplayable(null, canvas.getRssReader().getSettingsForm());
        }
    }

    /**
     * Called when the list of items changes
     */
    public void notifyListChanged() {
        loading = false;
        canvas.repaint();
    }

    /**
     * Renders the list of rss feed items
     *
     * @param g
     */
    public void render(Graphics g) {
        try {

            final int contentHeight = rssModel.size() * ITEM_HEIGHT;

            //limit the renderY not to keep the content on the screen
            if (contentHeight < canvas.getHeight()) {
                this.renderY = 0;
            } else if (this.renderY < -contentHeight + canvas.getHeight()) {
                this.renderY = -contentHeight + canvas.getHeight();
            } else if (this.renderY > 0) {
                this.renderY = 0;
            }

            if (loading) {
                renderLoading(g);
                return;
            }

            if (rssModel.size() == 0) {
                //no items to display
                g.drawString("No data", canvas.getWidth() / 2, canvas.getHeight() / 2, Graphics.BASELINE | Graphics.HCENTER);
                return;
            }

            int curY = this.renderY;

            //start renreding from the first visible item
            int startIndex = -this.renderY / ITEM_HEIGHT;
            curY += startIndex * ITEM_HEIGHT;

            final int len = rssModel.size();
            for (int i = startIndex; i < len; i++) {
                renderItem(g, curY, (RSSItem) rssModel.elementAt(i), i == selectedIndex);
                curY += ITEM_HEIGHT;

                //stop rendering below the screen
                if (curY > canvas.getHeight()) {
                    break;
                }
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
    private void renderItem(Graphics g, final int curY, final RSSItem item, final boolean selected) {

        if (selected) {
            g.setColor(RSSReaderCanvas.COLOR_HIGHLIGHT);
            g.fillRect(0, curY, canvas.getWidth(), ITEM_HEIGHT);
            g.setColor(RSSReaderCanvas.COLOR_HIGHLIGHT_FONT);
        } else {
            g.setColor(RSSReaderCanvas.COLOR_FONT);
        }

        g.setFont(RSSReaderCanvas.FONT_TITLE);
        g.drawString(item.getTruncatedTitle(), RSSReaderCanvas.MARGIN, curY + RSSReaderCanvas.MARGIN, Graphics.LEFT | Graphics.TOP);

        g.setFont(RSSReaderCanvas.FONT_DATE);
        g.drawString(item.getPubDate(), RSSReaderCanvas.MARGIN, curY + RSSReaderCanvas.MARGIN + RSSReaderCanvas.FONT_TITLE.getHeight(), Graphics.LEFT | Graphics.TOP);

        g.setColor(RSSReaderCanvas.COLOR_BORDER);
        g.drawLine(0, curY + ITEM_HEIGHT, canvas.getWidth(), curY + ITEM_HEIGHT);
    }

    /**
     * Renders loading status
     *
     * @param g
     */
    private void renderLoading(Graphics g) {
        g.drawString("Loading...", canvas.getWidth() / 2, canvas.getHeight() / 2, Graphics.BASELINE | Graphics.HCENTER);
    }

    /**
     * Reloads the feed
     */
    public void reload(final boolean initialLoad) {
        if (loading && !initialLoad) {
            //already loading
            return;
        }

        loading = true;
        this.renderY = 0;

        String feedUrl = RSSReader.INITIAL_FEED_URL;

        try {
            feedUrl = RMSUtils.readByteArray("settings").toString();
        } catch (Exception e) {
            Log.l.log("Can not read settings", "", e);
        }
        if (!initialLoad) {
            staticWebCache.remove(feedUrl);
        }
        staticWebCache.get(feedUrl, new DefaultResult() {

            public void run() {
                notifyListChanged();
                canvas.repaint();
            }
        });
    }

    /**
     * Selects item at the specified x- and y-position (if any). If tapped makes
     * the selection, otherwise just repaints the highlighted item.
     *
     * @param x
     * @param y
     * @param tapped
     */
    public void selectItem(final int x, final int y, boolean tapped) {

        final int absoluteY = -this.renderY + y;
        final int pointedIndex = absoluteY / ITEM_HEIGHT;

        if (pointedIndex >= 0 && pointedIndex < rssModel.size()) {
            this.selectedIndex = pointedIndex;
            if (tapped) {
                canvas.showDetails((RSSItem) rssModel.elementAt(this.selectedIndex));
            } else {
                canvas.repaint();
            }
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }
}
