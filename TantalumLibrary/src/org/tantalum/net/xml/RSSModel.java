/*
 Copyright (c) 2013, Paul Houghton and Futurice Oy
 All rights reserved.

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
package org.tantalum.net.xml;

import java.util.Vector;
import org.tantalum.util.L;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * RSS Value Object for parsing RSS
 *
 * @author ssaa
 */
public class RSSModel extends XMLModel {

    /**
     * The collection of RSSItems parsed from XML
     */
    protected final Vector items = new Vector(40);
    /**
     * The currently-being-parsed RSSItem
     */
    protected RSSItem currentItem;
    /**
     * The maximum length of the model. Items after this length is reached will
     * not be parsed, thereby limiting the total memory consumption.
     */
    protected final int maxLength;

    /**
     * Create a new RSS feed data model which will never parse more than the
     * specified maxLength items.
     *
     * @param maxLength
     */
    public RSSModel(final int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * When parsing this RSS XML document, this indicates the beginning of an
     * XML tag
     *
     * @param uri
     * @param localName
     * @param qName
     * @param attributes
     * @throws SAXException
     */
    public synchronized void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (qName.equals("item")) {
            currentItem = new RSSItem();
        }
    }

    /**
     * When parsing this RSS XML document, this indicates the body of an XML tag
     *
     * @param qname
     * @param chars
     * @param attributes
     */
    protected synchronized void parseElement(final String qname, final String chars, final XMLAttributes attributes) {
        try {
            if (currentItem != null) {
                synchronized (currentItem) {
                    if (qname.equals("title")) {
                        currentItem.setTitle(chars);
                    } else if (qname.equals("description")) {
                        currentItem.setDescription(chars);
                    } else if (qname.equals("link")) {
                        currentItem.setLink(chars);
                    } else if (qname.equals("pubDate")) {
                        currentItem.setPubDate(chars);
                    } else if (qname.equals("media:thumbnail")) {
                        currentItem.setThumbnail((String) attributes.getValue("url"));
                    }
                }
            }
        } catch (Exception e) {
            //#debug
            L.e("RSS parsing error", "qname=" + qname + " - chars=" + chars, e);
        }
    }

    /**
     * When parsing this RSS XML document, this indicates the end of an XML tag
     *
     * @param uri
     * @param localName
     * @param qname
     * @throws SAXException
     */
    public void endElement(final String uri, final String localName, final String qname) throws SAXException {
        super.endElement(uri, localName, qname);

        if (qname.equals("item")) {
            if (items.size() < maxLength) {
                items.addElement(currentItem);
            }
            currentItem = null;
        }
    }

    /**
     * Empty the data model
     *
     */
    public synchronized void removeAllElements() {
        items.removeAllElements();
    }

    /**
     * Return the number of RSSItem elements in the model
     *
     * @return
     */
    public synchronized int size() {
        return items.size();
    }

    /**
     * Return the RSSItem at the specified position
     *
     * @param i
     * @return
     */
    public synchronized RSSItem elementAt(final int i) {
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
        RSSItem adjacentItem = null;
        int i = items.indexOf(item);

        if (before) {
            if (i > 0) {
                adjacentItem = (RSSItem) items.elementAt(--i);
            }
        } else if (i < size() - 1) {
            adjacentItem = (RSSItem) items.elementAt(++i);
        }

        return adjacentItem;
    }
}
