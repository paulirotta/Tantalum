package com.futurice.formrssreader;

import com.futurice.tantalum2.Log;
import com.futurice.tantalum2.net.XMLAttributes;
import com.futurice.tantalum2.net.XMLVO;
import java.io.IOException;
import java.util.Vector;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * RSS Value Object for parsing RSS
 * @author ssaa
 */
public final class RSSVO extends XMLVO {

    private final Vector items = new Vector();
    private RSSItem currentItem;

    public synchronized void setXML(final String xml)  throws ParserConfigurationException, SAXException, IOException {
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
//                ListView.getInstance().notifyListChanged();
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

    public Vector getItems() {
        return items;
    }
}
