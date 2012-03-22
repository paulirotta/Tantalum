package com.futurice.tantalum2.net.xml;

import com.futurice.tantalum2.log.Log;
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
    protected RSSItem currentItem;

    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (qName.equals("item")) {
            currentItem = new RSSItem();
            items.addElement(currentItem);
        }
    }

    protected void parseElement(final String qName, final String chars, final XMLAttributes attributes) {
        try {
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
                        currentItem.setThumbnail((String) attributes.getValue("url"));
                    }
                }
            }
        } catch (Exception e) {
            Log.l.log("RSS parsing error", "qname=" + qName + " - chars=" + chars, e);
        }
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
