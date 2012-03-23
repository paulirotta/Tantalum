/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.lwuitrssreader;

import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.net.xml.RSSModel;
import com.futurice.tantalum2.rms.DataTypeHandler;

/**
 *
 * @author tsaa
 */
public class RSSTypeHandler implements DataTypeHandler {

    final ListModel model;

    public RSSTypeHandler(ListModel model) {
        this.model = model;
    }

    public Object convertToUseForm(byte[] bytes) {
        try {
            final RSSModel rssModel = new RSSModel();

            if (bytes.length > 0) {
                rssModel.setXML(bytes);
                model.repaint();
            }
            
            return rssModel;
        } catch (NullPointerException e) {
            Log.l.log("Null bytes when to RSSModel", "", e);
        } catch (Exception e) {
            Log.l.log("Error converting bytes to RSSModel", "", e);
        }
        return null;
    }
}
