package com.futurice.lwuitrssreader;

import com.futurice.tantalum2.net.XMLAttributes;
import com.futurice.tantalum2.net.XMLVO;
import java.util.Vector;

/**
 *
 * @author tsaa
 */
public class RSSVO extends XMLVO {

    private RSSItem current;
    private ListModel listModel;

    public RSSVO(ListModel listModel) {
        this.listModel = listModel;
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
