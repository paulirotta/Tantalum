/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.formrssreader;

import com.futurice.tantalum2.rms.DefaultCacheGetResult;
import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.net.StaticWebCache;
import com.futurice.tantalum2.Worker;
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
    private StaticWebCache staticWebCache;
    private RSSVO rSSVO;
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
        this.rssReader = rssReader;
        this.instance = ListView.this;
        this.detailsView = new DetailsView(rssReader, title);
        this.rSSVO = new RSSVO();
        this.staticWebCache = new StaticWebCache("rss", 1, 10, new DataTypeHandler() {

            public Object convertToUseForm(byte[] bytes) {
                try {
                    rSSVO.getItems().removeAllElements();
                    rSSVO.setXML(new String(bytes));
                    return rSSVO;
                } catch (Exception e) {
                    Log.log("Error in parsing XML.");
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
            reload(false);
        } else if (command == settingsCommand) {
            String feedUrl = RMSUtils.readString("settings");
            if ("".equals(feedUrl)) {
                feedUrl = RSSReader.INITIAL_FEED_URL;
            }
            rssReader.getSettingsForm().setUrlValue(feedUrl);
            rssReader.switchDisplayable(null, rssReader.getSettingsForm());
        }
    }

    public void reload(final boolean initialLoad) {
        if (loading && !initialLoad) {
            //already loading
            return;
        }

        loading = true;
        this.startIndex = 0;
        this.deleteAll();
        String feedUrl = RMSUtils.readString("settings");
        if ("".equals(feedUrl)) {
            feedUrl = RSSReader.INITIAL_FEED_URL;
        }
        staticWebCache.get(feedUrl, new DefaultCacheGetResult() {

            public void run() {
                notifyListChanged();
            }
        });

        paint();
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

    public void paint() {

        if (loading) {
            renderLoading();
            return;
        }

        if (rSSVO.getItems().isEmpty()) {
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

        final int len = rSSVO.getItems().size();
        for (int i = startIndex; i < len; i++) {
            final RSSItem rSSItem = (RSSItem) rSSVO.getItems().elementAt(i);

            Worker.queueEDT(new Runnable() {

                public void run() {
                    final StringItem titleStringItem = new StringItem(null, rSSItem.getTruncatedTitle(), StringItem.PLAIN);
                    titleStringItem.setFont(ListView.FONT_TITLE);
                    titleStringItem.setLayout(Item.LAYOUT_NEWLINE_AFTER);
                    final Command cmdShow = new Command("", Command.OK, 1);
                    titleStringItem.setDefaultCommand(cmdShow);
                    titleStringItem.setItemCommandListener(new ItemCommandListener() {

                        public void commandAction(Command c, Item item) {
                            showDetails(rSSItem);
                        }
                    });

                    final StringItem dateStringItem = new StringItem(null, rSSItem.getPubDate(), StringItem.PLAIN);
                    dateStringItem.setFont(ListView.FONT_DATE);
                    dateStringItem.setLayout(Item.LAYOUT_NEWLINE_AFTER);
                    final Image separatorImage = Image.createImage(getWidth(), 1);

                    append(titleStringItem);
                    append(dateStringItem);
                    append(separatorImage);
                }
            });
        }
        startIndex = len;
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
