package com.nokia.example.picasa.s40;

import com.futurice.tantalum3.net.StaticWebCache;
import javax.microedition.lcdui.Graphics;

/**
 * Class for displaying the featured images at picasa web albums. Relies on the
 * ImageGridCanvas class.
 */
public final class FeaturedCanvas extends ImageGridCanvas {

    public FeaturedCanvas(PicasaViewer midlet) {
        super(midlet);

        this.setTitle("Picasa Featured");
    }

    public void paint(final Graphics g) {
        checkScroll();        
        drawGrid(g, 0);
    }

    public void gesturePinch(
            int pinchDistanceStarting,
            int pinchDistanceCurrent,
            int pinchDistanceChange,
            int centerX,
            int centerY,
            int centerChangeX,
            int centerChangeY) {
        // Pinch to reload
        refresh(null, StaticWebCache.GET_WEB);
    }    
}
