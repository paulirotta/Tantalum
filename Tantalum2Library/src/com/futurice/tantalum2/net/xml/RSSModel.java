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

    protected final Vector items = new Vector(50);
    protected RSSItem currentItem;
    protected final int maxLength;

    public RSSModel(final int maxLength) {
        this.maxLength = maxLength;
    }

    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (qName.equals("item")) {
            currentItem = new RSSItem();
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
            //#debug
            Log.l.log("RSS parsing error", "qname=" + qName + " - chars=" + chars, e);
        }
    }

    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        if (qName.equals("item")) {
            if (items.size() < maxLength) {
                items.addElement(currentItem);
            }
            currentItem = null;
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
