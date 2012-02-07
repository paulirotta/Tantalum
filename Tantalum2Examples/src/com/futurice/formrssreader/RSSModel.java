package com.futurice.formrssreader;

import com.futurice.tantalum2.Worker;
import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.net.XMLAttributes;
import com.futurice.tantalum2.net.XMLModel;
import java.io.IOException;
import java.util.Vector;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * RSS Value Object for parsing RSS
 *
 * @author ssaa
 */
public final class RSSModel extends XMLModel {
    private final Vector items = new Vector();
    private final ListNotifier listNotifier = new ListNotifier();
    private RSSItem currentItem;

    public synchronized void setXML(final String xml) throws ParserConfigurationException, SAXException, IOException {
        super.setXML(xml);

        final ListView listView = ListView.getInstance();
        if (listView instanceof ListView) {
            listView.notifyListChanged();
        }
    }

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

            if (qName.equals("item")) {
                items.addElement(currentItem);
            } else if (currentItem != null) {
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
        } catch (Exception e) {
            Log.logThrowable(e, "RSS parsing error, qname=" + qName + " - chars=" + chars);
        }
    }

    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        
        if (qName.equals("item")) {
            listNotifier.queue();
        }
    }

    public Vector getItems() {
        return items;
    }
    
    public final class ListNotifier implements Runnable {
        public volatile boolean queued = false;
        
        void queue() {
            if (!queued) {
                queued = true;
                Worker.queueEDT(this);
            }
        }
        
        public void run() {
           queued = false;
           ListView.getInstance().notifyListChanged();
        }
    }
}
