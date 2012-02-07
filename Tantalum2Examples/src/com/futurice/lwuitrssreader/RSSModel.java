package com.futurice.lwuitrssreader;

import com.futurice.formrssreader.ListView;
import com.futurice.tantalum2.net.XMLAttributes;
import com.futurice.tantalum2.net.XMLModel;
import java.io.IOException;
import java.util.Vector;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author tsaa
 */
public class RSSModel extends XMLModel {

    private RSSItem current;
    private ListModel listModel;

    public RSSModel(ListModel listModel) {
        this.listModel = listModel;
    }

    public synchronized void setXML(final String xml)  throws ParserConfigurationException, SAXException, IOException {
        super.setXML(xml);
        
        listModel.repaint();
        ListView.getInstance().notifyListChanged();
    }
    

    protected void element(Vector charStack, Vector qnameStack, Vector attributeStack) {
        try {
            String lastChar = (String) charStack.lastElement();
            String lastQname = (String) qnameStack.lastElement();

            if (lastQname.equals("item")) {
                current = new RSSItem();
            } else {
                if (current != null) {
                    if (lastQname.equals("title")) {
                        if (lastChar != null) {
                            current.setTitle(lastChar);
                            listModel.addItem(current);
                        }
                    }
                    if (lastQname.equals("description")) {
                        if (lastChar != null) {
                            current.setDescription(lastChar);
                        }
                    }
                    if (lastQname.equals("link")) {
                        if (lastChar != null) {
                            current.setLink(lastChar);
                        }
                    }   
                    if (lastQname.equals("pubDate")) {
                        if (lastChar != null) {
                            current.setPubDate(lastChar);
                        }
                    }                      
                    if (lastQname.equals("media:thumbnail")) {
                        if (lastChar != null) {
                            current.setImgSrc(((XMLAttributes) attributeStack.lastElement()).getValue("url"));
                        }
                    }                       
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
