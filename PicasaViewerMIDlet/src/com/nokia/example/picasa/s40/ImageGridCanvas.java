package com.nokia.example.picasa.s40;

import com.futurice.tantalum3.Closure;
import com.futurice.tantalum3.Task;
import com.futurice.tantalum3.log.L;
import com.nokia.example.picasa.common.ImageObject;
import com.nokia.example.picasa.common.Storage;
import com.nokia.mid.ui.LCDUIUtil;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Class responsible for drawing a grid of images fetched from picasa web
 * albums.
 */
public abstract class ImageGridCanvas extends GestureCanvas {

    protected final Hashtable images = new Hashtable();
    protected Vector imageObjects = new Vector(); // Access only from UI thread
    protected final int imageSide;
    protected int headerHeight;
    private int startDot;
    private double angle;
    private static final double R = 12;
    private double YC;
    private double XC;
    private static final int[] shades = {0x000000, 0xffffff, 0xdddddd, 0xbbbbbb, 0x999999, 0x777777, 0x333333};
    private static final int dots = shades.length;
    private static final double step = (2 * Math.PI) / dots;
    private static final double circle = (2 * Math.PI);

    public ImageGridCanvas(final PicasaViewer midlet) {
        super(midlet);

        this.setTitle("ImageGridCanvas");
        imageSide = getWidth() / 2;
        headerHeight = 0;
        XC = imageSide;
        YC = getHeight() / 2;
        angle = 0;
    }

    public void loadFeed(final boolean search, final boolean fromWeb) {
        Storage.getImageObjects(new Closure() {
            public void run() {
                try {
                    imageObjects = (Vector) get();
                    top = -((imageObjects.size() * imageSide) / 2 - getHeight() / 2);

                    repaint();
                } catch (Exception ex) {
                    //#debug
                    L.e("Can not get images to grid", "", ex);
                }
            }
        }, search, fromWeb);
    }

    public void showNotify() {
        if (midlet.phoneSupportsCategoryBar()) {
            this.setFullScreenMode(true);
        }

        // Show statusbar
        try {
            LCDUIUtil.setObjectTrait(this, "nokia.ui.canvas.status_zone", Boolean.TRUE);
        } catch (Exception e) {
            L.i("showNotify LCDUIUtil", "trait not supported, normal before SDK 2.0");
        }

        super.showNotify();
    }

    /**
     * Draw images, starting at the specified Y
     *
     * @param startY
     */
    public void drawGrid(final Graphics g, final int startY) {
        g.setColor(0x000000);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (imageObjects.isEmpty()) {
            // Loading...
            this.drawSpinner(g);
        } else {
            for (int i = 0; i < imageObjects.size(); i++) {
                int xPosition = i % 2 * (getWidth() / 2);
                int yPosition = startY + scrollY + ((i - i % 2) / 2) * imageSide;

                if (yPosition > getHeight()) {
                    break;
                }
                // If image is in RAM
                if (images.containsKey(imageObjects.elementAt(i))) {
                    g.drawImage((Image) (images.get(imageObjects.elementAt(i))), xPosition, yPosition, Graphics.LEFT | Graphics.TOP);
                } else {
                    // If there were no results
                    if (((ImageObject) imageObjects.elementAt(i)).getThumbUrl().length() == 0) {
                        g.setColor(0xFFFFFF);
                        g.drawString("No Result.", 0, headerHeight, Graphics.TOP | Graphics.LEFT);
                    } else {
                        // Start loading the image, draw a placeholder
                        Storage.imageCache.get(((ImageObject) imageObjects.elementAt(i)).getThumbUrl(),
                                new ImageResult(imageObjects.elementAt(i)));
                        g.setColor(0x111111);
                        g.fillRect(xPosition, yPosition, imageSide, imageSide);
                    }
                }
            }
        }
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
        refresh();
    }
    
    public void refresh() {
        imageObjects.removeAllElements();
        scrollY = 0;
        top = getHeight();
        loadFeed(this instanceof SearchCanvas, true);
        repaint();        
    }

    public void gestureTap(int startX, int startY) {
        final int index = getItemIndex(startX, startY);

        if (index >= 0 && index < imageObjects.size()) {
            Storage.selectedImage = (ImageObject) imageObjects.elementAt(index);
            midlet.setDetailed();
        }
    }

    public void gestureFlick(int startX, int startY, float flickDirection, int flickSpeed, int flickSpeedX, int flickSpeedY) {
        gestureHandler.kineticScroll(flickSpeed, GestureHandler.FRAME_ANIMATOR_VERTICAL, friction, flickDirection);
    }

    /**
     * Return the image index based on the X and Y coordinates.
     *
     * @param x
     * @param y
     * @return
     */
    protected int getItemIndex(int x, int y) {
        if (y > headerHeight) {
            int row = (-scrollY + y - headerHeight) / imageSide;
            int column = x < getWidth() / 2 ? 0 : 1;
            return (row * 2 + column);
        }
        return -1;
    }

    /**
     * Loading animation
     */
    public void drawSpinner(final Graphics g) {
        for (int i = 0; i < dots; i++) {
            int x = (int) (XC + R * Math.cos(angle));
            int y = (int) (YC + R * Math.sin(angle));
            g.setColor(shades[(i + startDot) % dots]);
            g.fillRoundRect(x, y, 6, 6, 3, 3);
            angle = (angle - step) % circle;
        }
        startDot++;
        startDot = startDot % dots;
    }

    public void sizeChanged(final int w, final int h) {
        YC = getHeight() / 2;

        super.sizeChanged(w, h);
    }

    /**
     * Object for adding images to hashmap when they're loaded.
     */
    protected final class ImageResult extends Task {

        private Object key;

        public ImageResult(Object o) {
            key = o;
        }

        public void set(Object o) {
            if (o != null) {
                images.put(key, o);
                repaint();
            }
            super.set(o);
        }
    }
}
