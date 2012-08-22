/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.formrssreader;

import com.futurice.tantalum3.PlatformUtils;
import com.futurice.tantalum3.RunnableResult;
import com.futurice.tantalum3.Worker;
import com.futurice.tantalum3.log.Log;
import com.futurice.tantalum3.net.StaticWebCache;
import com.futurice.tantalum3.net.xml.RSSItem;
import com.futurice.tantalum3.net.xml.RSSModel;
import com.futurice.tantalum3.rms.DataTypeHandler;
import com.futurice.tantalum3.rms.RMSUtils;
import javax.microedition.lcdui.*;

/**
 *
 * @author vand
 */
public class ListForm extends Form implements CommandListener {

    private static ListForm instance;
    private final RSSReader rssReader;
    private final DetailsForm detailsView;
    private StaticWebCache feedCache;
    private final RSSModel rssModel = new RSSModel(60);
    public static final Font FONT_TITLE = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
    public static final Font FONT_DESCRIPTION = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    public static final Font FONT_DATE = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    public static final int MARGIN = FONT_TITLE.getHeight() / 2;
    protected int startIndex;
    private boolean loading = false;
    private Command exitCommand = new Command("Exit", Command.EXIT, 0);
    private Command reloadCommand = new Command("Reload", Command.ITEM, 0);
    private Command settingsCommand = new Command("Settings", Command.ITEM, 1);

    public ListForm(RSSReader rssReader, String title) {
        super(title);

        ListForm.instance = ListForm.this;
        this.rssReader = rssReader;
        this.detailsView = new DetailsForm(rssReader, title);
        this.feedCache = new StaticWebCache('5', new DataTypeHandler() {

            public Object convertToUseForm(final byte[] bytes) {
                try {
                    rssModel.removeAllElements();
                    rssModel.setXML(bytes);
                    notifyListChanged();

                    return rssModel;
                } catch (Exception e) {
                    //#debug
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
            //#debug
            Log.l.log("Can not read settings", "", e);
        }
        if ("".equals(feedUrl)) {
            feedUrl = RSSReader.INITIAL_FEED_URL;
        }

        if (forceLoad) {
            feedCache.update(feedUrl, new RunnableResult() {

                public void run() {
                    loading = false;
                    notifyListChanged();
                }

                public void noResult() {
                    loading = false;
                }
            });
        } else {
            feedCache.get(feedUrl, new RunnableResult() {

                public void noResult() {
                    loading = false;
                    reload(true);
                }

                public void run() {
                    loading = false;
                    notifyListChanged();
                }
            }, true);
        }
    }

    public void showDetails(final RSSItem selectedItem) {
        detailsView.setSelectedItem(selectedItem);
        rssReader.switchDisplayable(null, detailsView);
        detailsView.paint();
    }

    public void showList() {
        detailsView.setSelectedItem(null);
        rssReader.switchDisplayable(null, this);
    }

    public DetailsForm getDetailsView() {
        return detailsView;
    }

    private void renderLoading() {
        StringItem loadingStringItem = new StringItem(null, "Loading...", StringItem.PLAIN);
        loadingStringItem.setFont(ListForm.FONT_TITLE);
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

        if (rssModel.size() == 0) {
            if (startIndex == 0) {
                deleteAll();
            }
            //no items to display
            StringItem noDataStringItem = new StringItem(null, "No data",
                    StringItem.PLAIN);
            noDataStringItem.setFont(ListForm.FONT_TITLE);
            noDataStringItem.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_VCENTER);
            append(noDataStringItem);
            return;
        }

        if (startIndex == 0) {
            deleteAll();
        }

        final int len = rssModel.size();
        for (int i = startIndex; i < len; i++) {
            final RSSItem rssItem = (RSSItem) rssModel.elementAt(i);

            PlatformUtils.runOnUiThread(new Runnable() {

                public void run() {
                    addItem(rssItem);
                }
            });
        }
        startIndex = len;
    }

    public void addItem(final RSSItem rssItem) {
        final StringItem titleStringItem = new StringItem(null, rssItem.getTitle(), StringItem.PLAIN);
        titleStringItem.setFont(ListForm.FONT_TITLE);
        titleStringItem.setLayout(Item.LAYOUT_NEWLINE_AFTER);
        final Command cmdShow = new Command("", Command.OK, 1);
        titleStringItem.setDefaultCommand(cmdShow);
        titleStringItem.setItemCommandListener(new ItemCommandListener() {

            public void commandAction(Command c, Item item) {
                showDetails(rssItem);
            }
        });

        final StringItem dateStringItem = new StringItem(null, rssItem.getPubDate(), StringItem.PLAIN);
        dateStringItem.setFont(ListForm.FONT_DATE);
        dateStringItem.setLayout(Item.LAYOUT_NEWLINE_AFTER);
        final Image separatorImage = Image.createImage(getWidth(), 1);

        append(titleStringItem);
        append(dateStringItem);
        append(separatorImage);
    }

    public static ListForm getInstance() {
        return instance;
    }

    public void notifyListChanged() {
        loading = false;
        PlatformUtils.runOnUiThread(new Runnable() {

            public void run() {
                paint();
            }
        });
    }
}
