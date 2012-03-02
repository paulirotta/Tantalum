package com.futurice.s40rssreader;

import com.futurice.tantalum2.Worker;
import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.net.xml.XMLAttributes;
import com.futurice.tantalum2.net.xml.XMLModel;
import java.util.Vector;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * RSS Value Object for parsing RSS
 *
 * @author ssaa
 */
public class RSSModel extends XMLModel {

    private final Vector items = new Vector();
    private RSSItem currentItem;

    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (qName.equals("item")) {
            currentItem = new RSSItem();
        }
    }

    protected void element(final Vector charStack, final Vector qnameStack, final Vector attributeStack) {
        String chars = "";
        String qName = "";

        try {
            chars = (String) charStack.lastElement();
            qName = (String) qnameStack.lastElement();

            if (currentItem != null) {
                synchronized (currentItem) {
                    if (qName.equals("title")) {
                        currentItem.setTitle(chars);
                    } else if (qName.equals("description")) {
                        currentItem.setDescription(chars);
                    } else if (qName.equals("link")) {
                        currentItem.setLink(chars);
                    } else if (qName.equals("pubDate")) {
                        currentItem.setPubDate(chars);
                    } else if (qName.equals("media:thumbnail")) {
                        currentItem.setThumbnail(((XMLAttributes) attributeStack.lastElement()).getValue("url"));
                    }
                }
            }
        } catch (Exception e) {
            Log.l.log("RSS parsing error", "qname=" + qName + " - chars=" + chars, e);
        }
    }

    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        if (currentItem != null && qName.equals("item")) {
            final RSSItem item = currentItem;
            Worker.queueEDT(new Runnable() {

                public void run() {
                    items.addElement(item);
                    RSSReaderCanvas.getInstance().getListView().notifyListChanged();
                }
            });
        }

        super.endElement(uri, localName, qName);
    }

    public void removeAllElements() {
        items.removeAllElements();
    }

    public int size() {
        return items.size();
    }

    public RSSItem elementAt(int i) {
        return (RSSItem) items.elementAt(i);
    }
}
