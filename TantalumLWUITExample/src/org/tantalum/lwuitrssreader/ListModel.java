/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.lwuitrssreader;

import org.tantalum.log.L;
import org.tantalum.net.xml.RSSModel;
import org.tantalum.storage.DataTypeHandler;
import com.sun.lwuit.events.DataChangedListener;
import com.sun.lwuit.list.DefaultListModel;
import org.xml.sax.SAXException;

/**
 *
 * @author tsaa
 */
public class ListModel extends DefaultListModel implements DataChangedListener, DataTypeHandler {

    private ListForm listForm;
    private final LiveUpdateRSSModel rssModel = new LiveUpdateRSSModel(60);

    public ListModel(ListForm listForm) {
        this.listForm = listForm;
        addDataChangedListener(this);
    }

    public void dataChanged(int type, int index) {
        listForm.repaint();
    }

    public void repaint() {
        listForm.repaint();
    }

    public Object convertToUseForm(byte[] bytes) {
        try {
            if (bytes.length > 0) {
                rssModel.setXML(bytes);
            }

            return this;
        } catch (NullPointerException e) {
            L.e("Null bytes when to RSSModel", "", e);
        } catch (Exception e) {
            L.e("Error converting bytes to RSSModel", "", e);
        }
        return null;
    }

    private class LiveUpdateRSSModel extends RSSModel {

        public LiveUpdateRSSModel(final int maxSize) {
            super(maxSize);
        }

        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            if (currentItem != null && qName.equals("item")) {
                ListModel.this.addItem(currentItem);
            }

            super.endElement(uri, localName, qName);
        }
    }
}
