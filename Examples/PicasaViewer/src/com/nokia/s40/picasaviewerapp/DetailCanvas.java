/*
 * Copyright © 2012 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */
package com.nokia.s40.picasaviewerapp;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import org.tantalum.Task;
import org.tantalum.TimeoutException;
import org.tantalum.net.StaticWebCache;
import org.tantalum.util.L;

import com.nokia.common.picasaviewerapp.PicasaImageObject;
import com.nokia.common.picasaviewerapp.PicasaStorage;
import org.tantalum.jme.JMEFontUtils;

/**
 * The class displaying the larger image, title and photographer.
 *
 */
public final class DetailCanvas extends GestureCanvas {

    private static final int PADDING = 10;
    private volatile Image image;
    private static final double R = 12;
    private final double YC = 50.0;
    private final int width;
    private final Vector titleLines = new Vector();
    private int textY = 0;
    private int bottom = 0;
    private final Command backCommand = new Command("Back", Command.BACK, 0);
    private final JMEFontUtils fontUtils = JMEFontUtils.getFontUtils(Font.getDefaultFont(), " ..");

    public DetailCanvas(final PicasaViewer midlet) {
        super(midlet);

        width = getWidth();
        setFullScreenMode(true);
        setTitle("DetailedCanvas");
        if (!midlet.phoneSupportsCategoryBar()) {
            addCommand(backCommand);
        }
    }

    public void commandAction(Command c, Displayable d) {
        //#debug
        L.i("DetailedCanvas Command action", "Command " + c);
        if (c.getCommandType() == Command.BACK) {
            midlet.goBack();
        }
    }

    public void paint(final Graphics g) {
        final PicasaImageObject selectedImage = PicasaStorage.getSelectedImage();
        if (selectedImage != null) {
            if (titleLines.isEmpty()) {
                fontUtils.splitToLines(titleLines, selectedImage.title, width, true);
            }
        }

        //#debug
        L.i("Paint DetailCanvas", selectedImage.imageUrl);

        g.setColor(0x000000);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(0xffffff);

        boolean startingSpin = false;
        if (selectedImage != null) {
            // If we do not have the image and we are not loading it, start loading it.
            if (image == null && !isSpinning()) {
                startSpinner();
                startingSpin = true;
                try {
                    PicasaStorage.imageCache.getAsync(selectedImage.imageUrl,
                            Task.HIGH_PRIORITY,
                            StaticWebCache.GET_ANYWHERE,
                            new Task() {
                        public Object exec(final Object in) {
                            if (in != null && selectedImage == PicasaStorage.getSelectedImage()) {
                                image = (Image) in;
                                stopSpinner();
                            }
                            return in;
                        }
                    }.setClassName("DisplayCachedOnStartupOrSpinner")).fork().join(100);
                } catch (TimeoutException ex) {
                    // Normal for slow load
                } catch (Exception ex) {
                    //#debug
                    L.e("Can not join image load", selectedImage.imageUrl, ex);
                }
            }
            if (isSpinning()) {
                if (!startingSpin) {
                    drawSpinner(g);
                }
            } else if (image != null) {
                // Done, draw image.
                g.drawImage(image, getWidth() / 2, scrollY, Graphics.TOP | Graphics.HCENTER);
            }
            textY = image == null ? (int) YC + ((int) R) << 1 : image.getHeight() + scrollY;
            g.setColor(0xFFFFFF);

            for (int i = 0; i < titleLines.size(); i++) {
                g.drawString((String) titleLines.elementAt(i), PADDING, textY, Graphics.LEFT | Graphics.TOP);
                textY = textY + Font.getDefaultFont().getHeight();
            }
            if (-textY < bottom) {
                bottom = -textY;
            }

            g.drawString(selectedImage.author, PADDING, textY, Graphics.LEFT | Graphics.TOP);
        }
        drawBackIcon(g);
    }

    public void sizeChange(int w, int h) {
        top = -h;

        super.sizeChanged(w, h);
    }

    public boolean gestureTap(int startX, int startY) {
        //#debug
        L.i("gestureTap", "go back from DetailCanvas");
        midlet.goBack();

        return true;
    }

    public void hideNotify() {
        image = null;
        stopSpinner();
        super.hideNotify();
        titleLines.removeAllElements();
    }

    public void gestureDrag(int startX, int startY, int dragDistanceX, int dragDistanceY) {
        // Do nothing
    }

    public void gestureFlick(int startX, int startY, float flickDirection, int flickSpeed, int flickSpeedX, int flickSpeedY) {
        // Do nothing
    }
}
