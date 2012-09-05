/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.s40rssreader;

import com.futurice.tantalum3.Task;
import com.futurice.tantalum3.Workable;
import com.futurice.tantalum3.Worker;
import com.futurice.tantalum3.log.L;
import com.futurice.tantalum3.net.StaticWebCache;
import com.futurice.tantalum3.net.xml.RSSModel;
import com.futurice.tantalum3.rms.DataTypeHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author phou
 */
public abstract class RSSListView extends View {

    static boolean prefetchImages = false;
    protected final RSSListView.LiveUpdateRSSModel rssModel = new RSSListView.LiveUpdateRSSModel();
    protected final StaticWebCache feedCache;
    private final Task rssResult = new Task() {
        public void set(final Object o) {
            super.set(o);
            canvas.queueRepaint();
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
                    L.l.i("Error in parsing XML", rssModel.toString());
                    return null;
                }
            }
        });
    }

    protected final void clearCache() {
        Worker.fork(new Workable() {
            public void exec() {
                try {
                    doClearCache();
                } catch (Exception e) {
                    //#debug
                    L.l.e("Can not clear cache", "", e);
                }
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
    public void reload(final boolean forceNetLoad) {
        this.renderY = 0;
        rssModel.removeAllElements();

        String feedUrl = RSSReader.INITIAL_FEED_URL;
        if (forceNetLoad) {
            feedCache.update(feedUrl, rssResult);
        } else {
            feedCache.get(feedUrl, rssResult);
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
                    canvas.queueRepaint();
                }
            }
        }
    }

    public abstract boolean setSelectedIndex(int i);

    public abstract void selectItem(final int x, final int y, boolean tapped);

    public abstract void deselectItem();
}
