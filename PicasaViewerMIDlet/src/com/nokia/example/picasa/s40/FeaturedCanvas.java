package com.nokia.example.picasa.s40;

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
}
