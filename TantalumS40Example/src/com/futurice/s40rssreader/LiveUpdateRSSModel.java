package com.futurice.s40rssreader;

import com.futurice.tantalum2.Worker;
import com.futurice.tantalum2.net.xml.RSSItem;
import com.futurice.tantalum2.net.xml.RSSModel;
import org.xml.sax.SAXException;

/**
 * RSS Value Object for parsing RSS
 *
 * @author ssaa
 */
public class LiveUpdateRSSModel extends RSSModel {

    private static final Runnable updateRunnable = new Runnable() {

        public void run() {
            RSSReaderCanvas.getInstance().getListView().notifyListChanged();
        }
    };

    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        if (currentItem != null && qName.equals("item")) {
            Worker.queueEDT(updateRunnable);
        }

        super.endElement(uri, localName, qName);
    }
}
