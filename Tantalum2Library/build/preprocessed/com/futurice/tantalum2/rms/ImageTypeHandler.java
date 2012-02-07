/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2.rms;

import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.rms.DataTypeHandler;
import javax.microedition.lcdui.Image;

/**
 *
 * @author tsaa
 */
public class ImageTypeHandler implements DataTypeHandler {
    private static final Image DEFAULT_IMAGE = null;// this caused exception: Image.createImage(0, 0);
    
    public Object convertToUseForm(byte[] bytes) {
        try {
            return Image.createImage(bytes,0,bytes.length);
        } catch (Exception e) {
            Log.log("Error converting bytes to image");
        }
        return DEFAULT_IMAGE;
    }

}
