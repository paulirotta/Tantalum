/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.lwuitrssreader;

import org.tantalum.tantalum4.log.L;
import org.tantalum.tantalum4.storage.DataTypeHandler;
import com.sun.lwuit.Image;

/**
 *
 * @author phou
 */
public final class LWUITImageTypeHandler implements DataTypeHandler {

    public Object convertToUseForm(final byte[] bytes) {
        try {
            return Image.createImage(bytes, 0, bytes.length);
        } catch (Exception e) {
            L.e("Error converting bytes to LWUIT image", bytes == null ? "" : "" + bytes.length, e);
        }

        return null;
    }
}
