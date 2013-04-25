/*
 * Copyright © 2012 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */
package com.nokia.s40.picasaviewerapp;

import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import org.tantalum.Task;
import org.tantalum.net.StaticWebCache;
import org.tantalum.util.L;

import com.nokia.common.picasaviewerapp.PicasaImageObject;
import com.nokia.common.picasaviewerapp.PicasaStorage;

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

    public ImageGridCanvas(final PicasaViewer midlet) {
        super(midlet);

        setTitle("ImageGridCanvas");
        imageSide = getWidth() / 2;
        headerHeight = 0;
    }

    public Task loadFeed(final String search, final int getType) {
        final Task loadTask = new Task(Task.FASTLANE_PRIORITY) {

            protected Object exec(final Object in) {
                try {
                    //#debug
                    L.i("Load feed success, type=" + getType, search);
                    scrollY = 0;
                    imageObjectModel.removeAllElements();
                    images.clear();
                    final Vector newModel = (Vector) in;
                    for (int i = 0; i < newModel.size(); i++) {
                        imageObjectModel.addElement(newModel.elementAt(i));
                        PicasaStorage.imageCache.prefetch(((PicasaImageObject) imageObjectModel.elementAt(i)).thumbUrl);
                    }
                    top = -((imageObjectModel.size() * imageSide) / 2 - getHeight() / 2) + imageSide - 20;
                    stopSpinner();
                } catch (Exception ex) {
                    //#debug
                    L.e("Can not get images to grid", "", ex);
                }

                return in;
            }
            
            public void onCanceled(String reason) {
                //#debug
                L.i("Load feed canceled, type=" + getType + " reason=" + reason, search);
                if (getType == StaticWebCache.GET_LOCAL) {
                    imageObjectModel.removeAllElements();
                    images.clear();
                    top = -getHeight();
                }
                stopSpinner();
            }
        }.setClassName("LoadFeed");

        //#debug
        L.i("loadFeed", search);
        PicasaStorage.getImageObjects(search, Task.HIGH_PRIORITY, getType, loadTask);
        if (getType != StaticWebCache.GET_LOCAL) {
            startSpinner();
        }

        return loadTask;
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
                    PicasaStorage.imageCache.getAsync(((PicasaImageObject) imageObjectModel.elementAt(i)).thumbUrl,
                            Task.NORMAL_PRIORITY, StaticWebCache.GET_ANYWHERE, new ImageResult(imageObjectModel.elementAt(i)));
                    g.setColor(0x111111);
                    g.fillRect(xPosition, yPosition, imageSide, imageSide);
                }
            }
        }
        drawSpinner(g);
    }
    
    public void refresh(final String url, final int getType) {
        top = -getHeight();
        repaint();
        loadFeed(url, getType);
    }

    public boolean gestureTap(int startX, int startY) {
        if (!super.gestureTap(startX, startY)) {
            final int index = getItemIndex(startX, startY);

            if (index >= 0 && index < imageObjectModel.size()) {
                PicasaStorage.setSelectedImage((PicasaImageObject) imageObjectModel.elementAt(index));
                //#debug
                L.i("select image", PicasaStorage.getSelectedImage().toString());
                midlet.goDetailCanvas();
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
     * Object for adding images to hashmap when they're loaded.
     */
    protected final class ImageResult extends Task {
        private final Object key;

        public ImageResult(Object key) {
            super(Task.FASTLANE_PRIORITY);
            this.key = key;
        }

        public Object exec(final Object in) {
            if (in != null) {
                images.put(key, in);
                repaint();
            }

            return in;
        }
    }
}
