/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.futurice.tantalum3.util;

import javax.microedition.lcdui.Image;

/**
 * Keep recent instances of images all of which are the same size in memory.
 * 
 * The cache will automatically clear() if the image size changes. This means you
 * can be guaranteed that if an images is returned, it is the correct size.
 * 
 * @author phou
 */
public final class PoolingWeakImageHashCache extends PoolingWeakHashCache {
    
    /**
     * Request an image from the cache. It must be of the specified dimensions.
     * 
     * @param width
     * @param height
     * @return 
     */
    public Image getImageFromPool(final int width, final int height) {
        Image image = (Image) getFromPool();
        
        if (image != null && (image.getWidth() != width || image.getHeight() != height)) {
            // The cache is being re-used for a different size image. Remove the old
            image = null;
            clear();
        } 
        
        return image;
    }
}
