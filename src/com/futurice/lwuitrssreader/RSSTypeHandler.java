/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.lwuitrssreader;

import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.rms.DataTypeHandler;

/**
 *
 * @author tsaa
 */
public class RSSTypeHandler implements DataTypeHandler {

    ListModel model;

    public RSSTypeHandler(ListModel model) {
        this.model = model;
    }

    public Object convertToUseForm(byte[] bytes) {
        try {
            RSSModel rssvo = new RSSModel(model);
            rssvo.setXML(new String(bytes));
            return rssvo;
        } catch (Exception e) {
            Log.log("Error converting bytes to RSSVO");
        }
        return null;
    }
}
