/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.s40rssreader;

import com.futurice.tantalum2.Result;
import com.futurice.tantalum2.Workable;
import com.futurice.tantalum2.Worker;
import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.net.StaticWebCache;
import com.futurice.tantalum2.net.xml.RSSModel;
import com.futurice.tantalum2.rms.DataTypeHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author phou
 */
public abstract class RSSListView extends View {

    static boolean prefetchImages = false;
    protected final RSSListView.LiveUpdateRSSModel rssModel = new RSSListView.LiveUpdateRSSModel();
    protected final StaticWebCache feedCache;
    private final Result rssResult = new Result() {

        public void setResult(final Object o) {
            notifyListChanged();
        }
    };

    public RSSListView(final RSSReaderCanvas canvas) {
        super(canvas);

        feedCache = new StaticWebCache('5', new DataTypeHandler() {

            public Object convertToUseForm(byte[] bytes) {
                try {
                    rssModel.setXML(bytes);

                    return rssModel;
                } catch (Exception e) {
                    //#debug
                    Log.l.log("Error in parsing XML", rssModel.toString());
                    return null;
                }
            }
        });
    }

    protected final void clearCache() {
        Worker.queuePriority(new Workable() {

            public boolean work() {
                try {
                    doClearCache();
                } catch (Exception e) {
                    //#debug
                    Log.l.log("Can not clear cache", "", e);
                }

                return false;
            }
        });
    }

    protected void doClearCache() {
        feedCache.clear();
        DetailsView.imageCache.clear();
        reload(true);
    }

    /**
     * Reloads the feed
     */
    public void reload(final boolean forceNetLoad) {
        this.renderY = 0;
        rssModel.removeAllElements();

        String feedUrl = RSSReader.INITIAL_FEED_URL;
        if (forceNetLoad) {
            feedCache.update(feedUrl, rssResult);
        } else {
            feedCache.get(feedUrl, rssResult, true);
        }
    }

    /**
     * Called when the list of items changes
     */
    public void notifyListChanged() {
        canvas.queueRepaint();
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
        private final Runnable updateRunnable = new Runnable() {

            public void run() {
                notifyListChanged();
            }
        };

        public void setXML(final byte[] xml) throws SAXException, IllegalArgumentException {
            checkForWLAN(); // If this just came in over a WLAN net, get the images also
            super.setXML(xml);
        }

        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            if (currentItem != null && qName.equals("item")) {
                if (items.size() < maxLength) {
                    Worker.queueEDT(updateRunnable);
                    if (prefetchImages) {
                        DetailsView.imageCache.prefetch(currentItem.getThumbnail());
                    }
                }
            }

            super.endElement(uri, localName, qName);
        }
    }
}
