package com.futurice.tantalum3.net.xml;

import com.futurice.tantalum3.log.L;
import java.util.Vector;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * RSS Value Object for parsing RSS
 *
 * @author ssaa
 */
public class RSSModel extends XMLModel {

    protected final Vector items = new Vector(40);
    protected RSSItem currentItem;
    protected final int maxLength;

    public RSSModel(final int maxLength) {
        this.maxLength = maxLength;
    }

    public synchronized void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (qName.equals("item")) {
            currentItem = new RSSItem();
        }
    }

    protected synchronized void parseElement(final String qName, final String chars, final XMLAttributes attributes) {
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
            L.e("RSS parsing error", "qname=" + qName + " - chars=" + chars, e);
        }
    }

    public synchronized void endElement(final String uri, final String localName, final String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        if (qName.equals("item")) {
            if (items.size() < maxLength) {
                items.addElement(currentItem);
            }
            currentItem = null;
        }
    }

    public synchronized void removeAllElements() {
        items.removeAllElements();
    }

    public synchronized int size() {
        return items.size();
    }

    public synchronized RSSItem elementAt(int i) {
        return (RSSItem) items.elementAt(i);
    }

    /**
     * Copy the current list into a working array which can safely be used
     * outside of synchronized blocks. This guards against simultaneous changes
     * to the list on another thread.
     *
     * @param copy
     * @return
     */
    public final synchronized RSSItem[] copy(RSSItem[] copy) {
        if (copy == null || copy.length != size()) {
            copy = new RSSItem[size()];
        }
        items.copyInto(copy);

        return copy;
    }

    /**
     * Return the item before or after the specified item.
     *
     * null is returned if the item is not found, or there are no more items in
     * the specified direction.
     *
     * @param item
     * @param before
     * @return
     */
    public synchronized RSSItem itemNextTo(final RSSItem item, final boolean before) {
        int i = items.indexOf(item);

        if (before) {
            if (i > 0) {
                return (RSSItem) items.elementAt(--i);
            }
        } else if (i < size() - 1) {
            return (RSSItem) items.elementAt(++i);
        }

        return null;
    }
}
