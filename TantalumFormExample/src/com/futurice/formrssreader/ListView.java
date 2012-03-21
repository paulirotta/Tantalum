/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.formrssreader;

import com.futurice.tantalum2.DefaultResult;
import com.futurice.tantalum2.DefaultRunnableResult;
import com.futurice.tantalum2.net.StaticWebCache;
import com.futurice.tantalum2.Worker;
import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.rms.DataTypeHandler;
import com.futurice.tantalum2.rms.RMSUtils;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;

/**
 *
 * @author vand
 */
public class ListView extends Form implements CommandListener {

    private static ListView instance;
    private final RSSReader rssReader;
    private final DetailsView detailsView;
    private StaticWebCache feedCache;
    private final RSSModel rssModel = new RSSModel();
    public static final Font FONT_TITLE = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
    public static final Font FONT_DESCRIPTION = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    public static final Font FONT_DATE = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    public static final int MARGIN = FONT_TITLE.getHeight() / 2;
    protected int startIndex;
    private boolean loading = true;
    private Command exitCommand = new Command("Exit", Command.EXIT, 0);
    private Command reloadCommand = new Command("Reload", Command.ITEM, 0);
    private Command settingsCommand = new Command("Settings", Command.ITEM, 1);

    public ListView(RSSReader rssReader, String title) {
        super(title);

        ListView.instance = ListView.this;
        this.rssReader = rssReader;
        this.detailsView = new DetailsView(rssReader, title);
        this.feedCache = new StaticWebCache("rss", '5', new DataTypeHandler() {

            public Object convertToUseForm(byte[] bytes) {
                try {
                    rssModel.getItems().removeAllElements();
                    rssModel.setXML(bytes);

                    return rssModel;
                } catch (Exception e) {
                    Log.l.log("Error parsing XML", rssModel.toString());
                    return null;
                }
            }
        });

        this.addCommand(exitCommand);
        this.addCommand(reloadCommand);
        this.addCommand(settingsCommand);
        this.setCommandListener(this);
    }

    public void commandAction(Command command, Displayable d) {
        if (command == exitCommand) {
            rssReader.exitMIDlet();
        } else if (command == reloadCommand) {
            reload(true);
        } else if (command == settingsCommand) {
            String feedUrl = RSSReader.INITIAL_FEED_URL;

            try {
                feedUrl = RMSUtils.read("settings").toString();
            } catch (Exception e) {
                Log.l.log("Can not read settings", "", e);
            }
            rssReader.getSettingsForm().setUrlValue(feedUrl);
            rssReader.switchDisplayable(null, rssReader.getSettingsForm());
        }
    }

    /**
     * For thread safety this is only called from the EDT
     *
     * @param forceLoad
     */
    public void reload(final boolean forceLoad) {
        if (loading && !forceLoad) {
            //already loading
            return;
        }

        loading = true;
        this.startIndex = 0;
        this.deleteAll();
        paint();
        String feedUrl = RSSReader.INITIAL_FEED_URL;

        try {
            final byte[] bytes = RMSUtils.read("settings");

            if (bytes != null) {
                feedUrl = bytes.toString();
            }
        } catch (Exception e) {
            Log.l.log("Can not read settings", "", e);
        }
        if ("".equals(feedUrl)) {
            feedUrl = RSSReader.INITIAL_FEED_URL;
        }

        if (forceLoad) {
            feedCache.update(feedUrl, new DefaultRunnableResult() {

                public void run() {
                    loading = false;
                    notifyListChanged();
                }

                public void noResult() {
                    loading = false;
                }
            });
        } else {
            feedCache.get(feedUrl, new DefaultRunnableResult() {

                public void noResult() {
                    loading = false;
                    reload(true);
                }

                public void run() {
                    loading = false;
                    notifyListChanged();
                }
            });
        }
    }

    public void showDetails(final RSSItem selectedItem) {
        detailsView.setSelectedItem(selectedItem);
        rssReader.switchDisplayable(null, detailsView);
        detailsView.paint();
    }

    public void showList() {
        detailsView.getSelectedItem().setThumbnailImage(null);
        detailsView.setSelectedItem(null);
        rssReader.switchDisplayable(null, this);
    }

    public DetailsView getDetailsView() {
        return detailsView;
    }

    private void renderLoading() {
        StringItem loadingStringItem = new StringItem(null, "Loading...", StringItem.PLAIN);
        loadingStringItem.setFont(ListView.FONT_TITLE);
        loadingStringItem.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_VCENTER);
        append(loadingStringItem);
    }

    /**
     * Update the display
     */
    private void paint() {
        if (loading) {
            renderLoading();
            return;
        }

        if (rssModel.getItems().isEmpty()) {
            if (startIndex == 0) {
                deleteAll();
            }
            //no items to display
            StringItem noDataStringItem = new StringItem(null, "No data",
                    StringItem.PLAIN);
            noDataStringItem.setFont(ListView.FONT_TITLE);
            noDataStringItem.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_VCENTER);
            append(noDataStringItem);
            return;
        }

        if (startIndex == 0) {
            deleteAll();
        }

        final int len = rssModel.getItems().size();
        for (int i = startIndex; i < len; i++) {
            final RSSItem rssItem = (RSSItem) rssModel.getItems().elementAt(i);

            Worker.queueEDT(new Runnable() {

                public void run() {
                    addItem(rssItem);
                }
            });
        }
        startIndex = len;
    }

    public void addItem(final RSSItem rssItem) {
        final StringItem titleStringItem = new StringItem(null, rssItem.getTruncatedTitle(), StringItem.PLAIN);
        titleStringItem.setFont(ListView.FONT_TITLE);
        titleStringItem.setLayout(Item.LAYOUT_NEWLINE_AFTER);
        final Command cmdShow = new Command("", Command.OK, 1);
        titleStringItem.setDefaultCommand(cmdShow);
        titleStringItem.setItemCommandListener(new ItemCommandListener() {

            public void commandAction(Command c, Item item) {
                showDetails(rssItem);
            }
        });

        final StringItem dateStringItem = new StringItem(null, rssItem.getPubDate(), StringItem.PLAIN);
        dateStringItem.setFont(ListView.FONT_DATE);
        dateStringItem.setLayout(Item.LAYOUT_NEWLINE_AFTER);
        final Image separatorImage = Image.createImage(getWidth(), 1);

        append(titleStringItem);
        append(dateStringItem);
        append(separatorImage);
    }

    public static ListView getInstance() {
        return instance;
    }

    public void notifyListChanged() {
        loading = false;
        Worker.queueEDT(new Runnable() {

            public void run() {
                paint();
            }
        });
    }
}
