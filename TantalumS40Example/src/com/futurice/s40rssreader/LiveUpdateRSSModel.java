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
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        if (currentItem != null && qName.equals("item")) {
            final RSSItem item = currentItem;
            Worker.queueEDT(new Runnable() {

                public void run() {
                    RSSReaderCanvas.getInstance().getListView().notifyListChanged();
                }
            });
        }

        super.endElement(uri, localName, qName);
    }
}
