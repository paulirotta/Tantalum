/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.s40rssreader;

import com.futurice.tantalum4.Task;
import com.futurice.tantalum4.Workable;
import com.futurice.tantalum4.Worker;
import com.futurice.tantalum4.log.L;
import com.futurice.tantalum4.net.StaticWebCache;
import com.futurice.tantalum4.net.xml.RSSModel;
import com.futurice.tantalum4.storage.DataTypeHandler;
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

        feedCache = new StaticWebCache('5', new DataTypeHandler() {
            public Object convertToUseForm(byte[] bytes) {
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

    protected final void clearCache() {
        Worker.fork(new Workable() {
            public Object exec(final Object in) {
                try {
                    doClearCache();
                } catch (Exception e) {
                    //#debug
                    L.e("Can not clear cache", "", e);
                }

                return in;
            }
        }, Worker.HIGH_PRIORITY);
    }

    protected void doClearCache() {
        feedCache.clear();
        DetailsView.imageCache.clear();
        reload(true);
    }

    /**
     * Reloads the feed
     */
    public Task reload(final boolean forceNetLoad) {
        this.renderY = 0;
        rssModel.removeAllElements();
        final Task rssResult = new Task() {
            public Object doInBackground(final Object params) {
                canvas.repaint();

                return null;
            }
        };

        String feedUrl = RSSReader.INITIAL_FEED_URL;
        if (forceNetLoad) {
            feedCache.get(feedUrl, rssResult, StaticWebCache.GET_WEB, Worker.HIGH_PRIORITY);
        } else {
            feedCache.get(feedUrl, rssResult, StaticWebCache.GET_ANYWHERE, Worker.HIGH_PRIORITY);
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
            checkForWLAN(); // If this just came in over a WLAN net, get the images also
            super.setXML(xml);
        }

        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (currentItem != null && qName.equals("item")) {
                if (items.size() < maxLength) {
                    if (prefetchImages) {
                        DetailsView.imageCache.prefetch(currentItem.getThumbnail());
                    }
                    canvas.repaint();
                }
            }
        }
    }

    public abstract boolean setSelectedIndex(int i);

    public abstract void selectItem(final int x, final int y, boolean tapped);

    public abstract void deselectItem();
}
