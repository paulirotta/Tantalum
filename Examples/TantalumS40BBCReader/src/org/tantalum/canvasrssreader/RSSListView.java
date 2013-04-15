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

import org.tantalum.PlatformUtils;
import org.tantalum.Task;
import org.tantalum.net.StaticWebCache;
import org.tantalum.net.xml.RSSModel;
import org.tantalum.storage.DataTypeHandler;
import org.tantalum.storage.FlashDatabaseException;
import org.tantalum.util.L;
import org.xml.sax.SAXException;

/**
 *
 * @author phou
 */
public abstract class RSSListView extends View {

    static boolean prefetchImages = false;
    protected final RSSListView.LiveUpdateRSSModel rssModel = new RSSListView.LiveUpdateRSSModel();
    protected final StaticWebCache feedCache;

    public RSSListView(final RSSReaderCanvas canvas) {
        super(canvas);

        feedCache = StaticWebCache.getWebCache('5', PlatformUtils.PHONE_DATABASE_CACHE, new DataTypeHandler() {
            public Object convertToUseForm(final Object key, byte[] bytes) {
                try {
                    rssModel.setXML(bytes);

                    return rssModel;
                } catch (Exception e) {
                    //#debug
                    L.i("Error in parsing XML", rssModel.toString());
                    return null;
                }
            }
        });
    }

    protected void clearCache() {
        feedCache.clearAsync(new Task() {
            protected Object exec(final Object in) {
                reloadAsync(true);

                return in;
            }
        }.setClassName("ClearCache"));
        DetailsView.imageCache.clearAsync(null);
    }

    /**
     * Reloads the feed
     */
    public Task reloadAsync(final boolean forceNetLoad) {
        this.renderY = 0;
        rssModel.removeAllElements();
        final Task rssResult = new Task() {
            public Object exec(final Object in) {
                //#debug
                L.i(this, "canvas.refresh()", "rssModelLength=" + rssModel.size());
                canvas.refresh();

                return in;
            }
        }.setClassName("RSSResult");

        String feedUrl = RSSReader.INITIAL_FEED_URL;
        //#debug
        L.i(this, "getAsync()", "forceNetLoad=" + forceNetLoad);
        if (forceNetLoad) {
            feedCache.getAsync(feedUrl, Task.HIGH_PRIORITY, StaticWebCache.GET_WEB, rssResult);
        } else {
            feedCache.getAsync(feedUrl, Task.FASTLANE_PRIORITY, StaticWebCache.GET_ANYWHERE, rssResult);
        }

        return rssResult;
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

    protected final class LiveUpdateRSSModel extends RSSModel {

        LiveUpdateRSSModel() {
            super(60);
        }

        public void setXML(final byte[] xml) throws SAXException, IllegalArgumentException {
            checkForWLAN(); // If this just came in over a WLAN net, getAsync the images also
            super.setXML(xml);
        }

        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (currentItem != null && qName.equals("item")) {
                if (items.size() < maxLength) {
                    if (prefetchImages) {
                        try {
                            DetailsView.imageCache.prefetch(currentItem.getThumbnail());
                        } catch (FlashDatabaseException ex) {
                            //#debug
                            L.e("Can not get prefetch image", currentItem.getThumbnail(), ex);
                        }
                    }
                    canvas.refresh();
                }
            }
        }
    }

    public abstract boolean setSelectedIndex(int i);

    public abstract void selectItem(final int x, final int y, boolean tapped);

    public abstract void deselectItem();
}
