package com.nokia.example.picasa.s40;

import com.futurice.tantalum3.AsyncCallbackTask;
import com.futurice.tantalum3.Task;
import com.futurice.tantalum3.log.L;
import com.nokia.example.picasa.common.PicasaImageObject;
import com.nokia.example.picasa.common.PicasaStorage;
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
    protected final Vector imageObjectModel = new Vector(); // Access only from UI thread
    protected final int imageSide;
    protected int headerHeight;
    protected Boolean statusBarVisible = Boolean.TRUE;
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

        setTitle("ImageGridCanvas");
        imageSide = getWidth() / 2;
        headerHeight = 0;
        XC = imageSide;
        YC = getHeight() / 2;
        angle = 0;
    }

    public AsyncCallbackTask loadFeed(final String search, final int getType) {
        final AsyncCallbackTask task = new AsyncCallbackTask() {
            protected void onPostExecute(final Object result) {
                try {
                    imageObjectModel.removeAllElements();
                    if (result != null) {
                        final Vector newModel = (Vector) result;
                        for (int i = 0; i < newModel.size(); i++) {
                            imageObjectModel.addElement(newModel.elementAt(i));
                        }
                    }
                    top = -((imageObjectModel.size() * imageSide) / 2 - getHeight() / 2);
                    midlet.stopReloadAnimation();
                    repaint();
                } catch (Exception ex) {
                    //#debug
                    L.e("Can not get images to grid", "", ex);
                }
            }

            public void onCancelled() {
                midlet.stopReloadAnimation();
                repaint();
            }
        };

        //#debug
        L.i("loadFeed", search);
        PicasaStorage.getImageObjects(task, search, getType);

        return task;
    }

    public void showNotify() {
        midlet.setCategoryBarVisibility(true);

        // Show statusbar
        try {
            LCDUIUtil.setObjectTrait(this, "nokia.ui.canvas.status_zone", statusBarVisible);
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
        g.fillRect(0, startY, getWidth(), getHeight() - startY);

        for (int i = 0; i < imageObjectModel.size(); i++) {
            int xPosition = i % 2 * (getWidth() / 2);
            int yPosition = startY + scrollY + ((i - i % 2) / 2) * imageSide;

            if (yPosition > getHeight()) {
                break;
            }
            // If image is in RAM
            if (images.containsKey(imageObjectModel.elementAt(i))) {
                g.drawImage((Image) (images.get(imageObjectModel.elementAt(i))), xPosition, yPosition, Graphics.LEFT | Graphics.TOP);
            } else {
                // If there were no results
                if (((PicasaImageObject) imageObjectModel.elementAt(i)).thumbUrl.length() == 0) {
                    g.setColor(0xFFFFFF);
                    g.drawString("No Result.", 0, headerHeight, Graphics.TOP | Graphics.LEFT);
                } else {
                    // Start loading the image, draw a placeholder
                    PicasaStorage.imageCache.get(((PicasaImageObject) imageObjectModel.elementAt(i)).thumbUrl,
                            new ImageResult(imageObjectModel.elementAt(i)));
                    g.setColor(0x111111);
                    g.fillRect(xPosition, yPosition, imageSide, imageSide);
                }
            }
        }
        if (isSpinning()) {
            // Loading...
            drawSpinner(g);
        }
    }

    public Task refresh(final String url, final int getType) {
        midlet.startReloadAnimation();
        imageObjectModel.removeAllElements();
        images.clear();
        scrollY = 0;
        top = -getHeight();
        repaint();

        return loadFeed(url, getType);
    }

    public boolean gestureTap(int startX, int startY) {
        if (!super.gestureTap(startX, startY)) {
            final int index = getItemIndex(startX, startY);

            if (index >= 0 && index < imageObjectModel.size()) {
                PicasaStorage.setSelectedImage((PicasaImageObject) imageObjectModel.elementAt(index));
                midlet.setDetailed();
                return true;
            }
        }

        return false;
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
    protected final class ImageResult extends AsyncCallbackTask {

        private final Object key;

        public ImageResult(Object key) {
            this.key = key;
        }

        public Object doInBackground(final Object in) {
            if (in != null) {
                images.put(key, in);
            }

            return in;
        }

        protected void onPostExecute(Object result) {
            repaint();
        }
    }
}
