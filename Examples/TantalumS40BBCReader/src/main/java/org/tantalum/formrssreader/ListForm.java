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
package org.tantalum.formrssreader;

import javax.microedition.lcdui.*;
import org.tantalum.CancellationException;
import org.tantalum.PlatformUtils;
import org.tantalum.Task;
import org.tantalum.TimeoutException;
import org.tantalum.jme.RMSUtils;
import org.tantalum.net.StaticWebCache;
import org.tantalum.net.xml.RSSItem;
import org.tantalum.net.xml.RSSModel;
import org.tantalum.storage.DataTypeHandler;
import org.tantalum.util.L;

/**
 *
 * @author vand
 */
public final class ListForm extends Form implements CommandListener {

    private static ListForm instance;
    private final FormRSSReader rssReader;
    private final DetailsForm detailsView;
    private final StaticWebCache feedCache = StaticWebCache.getWebCache('5', PlatformUtils.PHONE_DATABASE_CACHE, new DataTypeHandler() {
        public Object convertToUseForm(final Object key, final byte[] bytes) {
            try {
                rssModel.removeAllElements();
                rssModel.setXML(bytes);

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

    public ListForm(FormRSSReader rssReader, String title) {
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
            PlatformUtils.getInstance().shutdown(false, "Exit command received");
        } else if (command == reloadCommand) {
            final Alert alert = new Alert("Reloading", "Reloading..", null, AlertType.INFO);
            alert.setTimeout(2000);
            //#debug
            L.i("Alert timeout set", "10000");
            (new Task(Task.HIGH_PRIORITY) {
                public Object exec(final Object in) {
                    try {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                        }
                        //#debug
                        L.i("start reload", ".join(10000)");
                        reload(true);
                        //#debug
                        L.i("Alert timeout set", "1");
                    } catch (Exception ex) {
                        L.e("Can not remove alert", null, ex);
                    }

                    return in;
                }
            }.setClassName("Alert")).fork();
            rssReader.switchDisplayable(alert, this);
        } else if (command == settingsCommand) {
            String feedUrl = FormRSSReader.INITIAL_FEED_URL;

            try {
                feedUrl = RMSUtils.getInstance().read("settings").toString();
            } catch (Exception e) {
                //#debug
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
    public Task reload(final boolean forceLoad) {
        if (loading && !forceLoad) {
            //already loading
            return null;
        }

        loading = true;
        String feedUrl = FormRSSReader.INITIAL_FEED_URL;

        try {
            final byte[] bytes = RMSUtils.getInstance().read("settings");

            if (bytes != null) {
                feedUrl = bytes.toString();
            }
        } catch (Exception e) {
            //#debug
            L.e("Can not read settings", "", e);
        }
        if ("".equals(feedUrl)) {
            feedUrl = FormRSSReader.INITIAL_FEED_URL;
        }

        final Task uiTask = new Task(Task.UI_PRIORITY) {
            protected Object exec(final Object in) {
                //#debug
                L.i("start postExecute on force reload", "model length=" + rssModel.size());
                loading = false;
                paint();
                return in;
            }

            protected void onCanceled(String reason) {
                //#debug
                L.i("force reload canceled", "model length=" + rssModel.size());
                loading = false;
                paint();
            }
        }.setClassName("LoadPainter");
        if (forceLoad) {
            feedCache.getAsync(feedUrl, Task.HIGH_PRIORITY, StaticWebCache.GET_WEB, uiTask);
        } else {
            feedCache.getAsync(feedUrl, Task.FASTLANE_PRIORITY, StaticWebCache.GET_ANYWHERE, uiTask);
        }

        return uiTask;
    }

    public void showDetails(final RSSItem selectedItem) {
        detailsView.setSelectedItem(selectedItem);
        rssReader.switchDisplayable(null, detailsView);
        detailsView.paint();
    }

    public void showList() {
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
