/*
 Copyright (c) 2013 Nokia Corporation. All rights reserved.
 Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation.
 Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 affiliates. Other product and company names mentioned herein may be trademarks
 or trade names of their respective owners.

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

import org.tantalum.CancellationException;
import org.tantalum.PlatformUtils;
import org.tantalum.Task;
import org.tantalum.TimeoutException;
import org.tantalum.net.StaticWebCache;
import org.tantalum.net.xml.RSSModel;
import org.tantalum.storage.CacheView;
import org.tantalum.storage.FlashDatabaseException;
import org.tantalum.util.L;
import org.tantalum.util.LOR;
import org.xml.sax.SAXException;

/**
 *
 * @author phou
 */
public abstract class RSSListView extends View {

    protected final Object MUTEX = new Object();
    static boolean prefetchImages = false;
    protected final RSSListView.LiveUpdateRSSModel rssModel = new RSSListView.LiveUpdateRSSModel();
    protected final StaticWebCache feedCache;

    public RSSListView(final RSSReaderCanvas canvas) throws FlashDatabaseException {
        super(canvas);

        feedCache = StaticWebCache.getWebCache('5', PlatformUtils.PHONE_DATABASE_CACHE, new CacheView() {
            public Object convertToUseForm(final Object key, final LOR bytesReference) {
                try {
                    rssModel.setXML(bytesReference.getBytes());
                    bytesReference.clear();
                    //#debug
                    L.i(this, "convertToUseForm", "" + rssModel);

                    return rssModel;
                } catch (Exception e) {
                    //#debug
                    L.i("Error in parsing XML", rssModel.toString());
                    return null;
                }
            }
        }, null, null);
    }

    protected void clearCache() {
        try {
            final Task imageClearTask = canvas.imageCache.clearAsync(null);

            feedCache.clearAsync(new Task(Task.FASTLANE_PRIORITY) {
                protected Object exec(final Object in) {
                    try {
                        imageClearTask.join();
                        reloadAsync(true);
                    } catch (CancellationException ex) {
                        //#debug
                        L.e(this, "Can not clear cache", null, ex);
                    } catch (TimeoutException ex) {
                        //#debug
                        L.e(this, "Can not clear cache", null, ex);
                    }

                    return in;
                }
            }.setClassName("ClearCache"));
        } catch (FlashDatabaseException ex) {
            //#debug
            L.e(this, "Can not clear cache", null, ex);
        }
    }

    /**
     * Reloads the feed
     */
    public Task reloadAsync(final boolean forceNetLoad) {
        this.renderY = 0;
        rssModel.removeAllElements();
        final Task rssResult = new Task(Task.FASTLANE_PRIORITY) {
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
            return feedCache.getAsync(feedUrl, Task.HIGH_PRIORITY, StaticWebCache.GET_WEB, rssResult);
        } else {
            return feedCache.getAsync(feedUrl, Task.FASTLANE_PRIORITY, StaticWebCache.GET_ANYWHERE, rssResult);
        }
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

        public void setXML(final byte[] xml) throws SAXException {
            checkForWLAN(); // If this just came in over a WLAN net, getAsync the images also
            super.setXML(xml);
        }

        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (currentItem != null && qName.equals("item")) {
                if (items.size() < maxLength) {
                    if (prefetchImages) {
                        try {
                            canvas.imageCache.prefetch(currentItem.getThumbnail());
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
