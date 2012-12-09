/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.formrssreader;

import com.futurice.tantalum4.UITask;
import com.futurice.tantalum4.Workable;
import com.futurice.tantalum4.Worker;
import com.futurice.tantalum4.log.L;
import com.futurice.tantalum4.net.StaticWebCache;
import com.futurice.tantalum4.net.xml.RSSItem;
import com.futurice.tantalum4.net.xml.RSSModel;
import com.futurice.tantalum4.storage.DataTypeHandler;
import com.futurice.tantalum4.storage.RMSUtils;
import javax.microedition.lcdui.*;

/**
 *
 * @author vand
 */
public final class ListForm extends Form implements CommandListener {

    private static ListForm instance;
    private final RSSReader rssReader;
    private final DetailsForm detailsView;
    private final StaticWebCache feedCache = new StaticWebCache('5', new DataTypeHandler() {
        public Object convertToUseForm(final byte[] bytes) {
            try {
                rssModel.removeAllElements();
                synchronized (Worker.LARGE_MEMORY_MUTEX) {
                    rssModel.setXML(bytes);
                }
                return rssModel;
            } catch (Exception e) {
                //#debug
                L.i("Error parsing XML", rssModel.toString());
                return null;
            }
        }
    });
    private final RSSModel rssModel = new RSSModel(40);
    public static final Font FONT_TITLE = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
    public static final Font FONT_DESCRIPTION = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    public static final Font FONT_DATE = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    public static final int MARGIN = FONT_TITLE.getHeight() / 2;
    private boolean loading = false;
    private final Command exitCommand = new Command("Exit", Command.EXIT, 0);
    private final Command reloadCommand = new Command("Reload", Command.OK, 0);
    private final Command settingsCommand = new Command("Settings", Command.SCREEN, 1);

    public ListForm(RSSReader rssReader, String title) {
        super(title);

        //#debug
        L.i("Start start thread reload", "");
        reload(false);
        ListForm.instance = ListForm.this;
        this.rssReader = rssReader;
        detailsView = new DetailsForm(rssReader, title);
        addCommand(exitCommand);
        addCommand(reloadCommand);
        addCommand(settingsCommand);
        setCommandListener(this);
        //#debug
        L.i("End start thread reload", "");
    }

    public void commandAction(final Command command, final Displayable d) {
        if (command == exitCommand) {
            rssReader.exitMIDlet();
        } else if (command == reloadCommand) {
            final Alert alert = new Alert("Reloading", "Reloading..", null, AlertType.INFO);
            alert.setTimeout(10000);
            //#debug
            L.i("Alert timeout set", "10000");
            Worker.fork(new Workable() {
                public Object exec(final Object in) {
                    try {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                        }
                        //#debug
                        L.i("start reload", ".join(10000)");
                        reload(true).join(10000);
                        alert.setTimeout(1);
                        //#debug
                        L.i("Alert timeout set", "1");
                    } catch (Exception ex) {
                        L.e("Can not remove alert", null, ex);
                    }

                    return in;
                }
            });
            rssReader.switchDisplayable(alert, this);
        } else if (command == settingsCommand) {
            String feedUrl = RSSReader.INITIAL_FEED_URL;

            try {
                feedUrl = RMSUtils.read("settings").toString();
            } catch (Exception e) {
                L.e("Can not read settings", "", e);
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
    public UITask reload(final boolean forceLoad) {
        final UITask uiTask;

        if (loading && !forceLoad) {
            //already loading
            return null;
        }

        loading = true;
        String feedUrl = RSSReader.INITIAL_FEED_URL;

        try {
            final byte[] bytes = RMSUtils.read("settings");

            if (bytes != null) {
                feedUrl = bytes.toString();
            }
        } catch (Exception e) {
            //#debug
            L.e("Can not read settings", "", e);
        }
        if ("".equals(feedUrl)) {
            feedUrl = RSSReader.INITIAL_FEED_URL;
        }

        if (forceLoad) {
            uiTask = new UITask() {
                public void onPostExecute(final Object result) {
                    //#debug
                    L.i("start postExecute on force reload", "model length=" + rssModel.size());
                    loading = false;
                    paint();
                }

                protected void onCanceled() {
                    //#debug
                    L.i("force reload canceled", "model length=" + rssModel.size());
                    loading = false;
                    paint();
                }
            };
            feedCache.get(feedUrl, uiTask, Worker.HIGH_PRIORITY, StaticWebCache.GET_WEB);
        } else {
            uiTask = new UITask() {
                public void onPostExecute(final Object result) {
                    //#debug
                    L.i("start postExecute on reload", "model length=" + rssModel.size());
                    loading = false;
                    paint();
                }

                public boolean cancel(final boolean mayInterruptIfNeeded) {
                    //#debug
                    L.i("reload canceled, not found locally, try again to get from net", "model length=" + rssModel.size());
                    loading = false;
                    reload(true);
                    paint();

                    return false;
                }
            };
            feedCache.get(feedUrl, uiTask, Worker.HIGH_PRIORITY, StaticWebCache.GET_ANYWHERE);
        }

        return uiTask;
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
        loadingStringItem.setFont(FONT_TITLE);
        loadingStringItem.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_VCENTER);
        append(loadingStringItem);
    }

    /**
     * Update the display
     *
     * Only call this from the UI thread
     */
    private void paint() {
        deleteAll();
        if (loading) {
            renderLoading();
            return;
        }

        final int len = rssModel.size();
        for (int i = 0; i < len; i++) {
            addItem((RSSItem) rssModel.elementAt(i));
        }
    }

    public void addItem(final RSSItem rssItem) {
        final StringItem titleStringItem = new StringItem(null, rssItem.getTitle(), StringItem.PLAIN);
        titleStringItem.setFont(FONT_TITLE);
        titleStringItem.setLayout(Item.LAYOUT_NEWLINE_AFTER);
        final Command cmdShow = new Command("", Command.ITEM, 0);
        titleStringItem.setDefaultCommand(cmdShow);
        titleStringItem.setItemCommandListener(new ItemCommandListener() {
            public void commandAction(Command c, Item item) {
                showDetails(rssItem);
            }
        });

        final StringItem dateStringItem = new StringItem(null, rssItem.getPubDate(), StringItem.PLAIN);
        dateStringItem.setFont(FONT_DATE);
        dateStringItem.setLayout(Item.LAYOUT_NEWLINE_AFTER);
        final Image separatorImage = Image.createImage(getWidth(), 1);

        append(titleStringItem);
        append(dateStringItem);
        append(separatorImage);
    }

    public static ListForm getInstance() {
        return instance;
    }
}
