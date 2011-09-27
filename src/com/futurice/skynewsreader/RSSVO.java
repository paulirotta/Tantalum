package com.futurice.skynewsreader;

import com.futurice.tantalum2.StaticWebCache;
import com.futurice.tantalum2.net.XMLAttributes;
import com.futurice.tantalum2.net.XMLVO;
import com.futurice.tantalum2.rms.ImageTypeHandler;
import java.util.Vector;

/**
 *
 * @author tsaa
 */
public class RSSVO extends XMLVO {

    private RSSItem current;
    private ListModel listModel;
    private StaticWebCache imgsCache = new StaticWebCache("imgs", 1, 1, new ImageTypeHandler());

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
                            current.setPubDate("");
                        }
                    }
                    if (lastQname.equals("enclosure")) {
                        if (lastChar != null) {
                            current.setImgSrc(((XMLAttributes) attributeStack.elementAt(3)).getValue("url"));
                            imgsCache.get(current.getImgSrc(), new ImageCacheGetResult(current, listModel));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
