/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.skynewsreader;

import com.futurice.tantalum2.DefaultCacheGetResult;
import com.sun.lwuit.Image;

/**
 *
 * @author tsaa
 */
public class ImageCacheGetResult extends DefaultCacheGetResult {
    
    private RSSItem item;
    private ListModel model;
    
    public ImageCacheGetResult(RSSItem item, ListModel model) {
        this.item = item;
        this.model = model;
    }
    
    public void run() {
        item.setImg(Image.createImage((javax.microedition.lcdui.Image) getResult()));
        model.dataChanged(ListModel.CHANGED, 0);
    }
    
}
