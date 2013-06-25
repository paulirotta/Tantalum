/*
 * Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation.
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */
package com.nokia.s40.picasaviewerapp;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import org.tantalum.PlatformUtils;

import org.tantalum.net.StaticWebCache;
import org.tantalum.util.L;

/**
 * Class for displaying the featured images at Picasa web albums. Relies on the
 * ImageGridCanvas class.
 */
public final class FeaturedCanvas extends ImageGridCanvas {

    private final Command searchCommand = new Command("Search", Command.SCREEN, 1);
    private final Command exitCommand = new Command("Exit", Command.EXIT, 0);
    private Command refreshCommand;

    public FeaturedCanvas(PicasaViewer midlet) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        super(midlet);

        this.setTitle("Picasa Featured");
        if (midlet.phoneSupportsCategoryBar()) {
            try {
                refreshCommand = midlet.getRefreshIconCommand();
                com.nokia.mid.ui.VirtualKeyboard.hideOpenKeypadCommand(true);
            } catch (Exception e) {
                //#debug
                L.e("Can not initialize", "Update icon image", e);
            }
        } else {
            // Fallback to softkeys if there is no category bar
            refreshCommand = new Command("Refresh", Command.OK, 0);
            addCommand(searchCommand);
            addCommand(exitCommand);
        }
        addCommand(refreshCommand);
    }

    public void commandAction(Command c, Displayable d) {
        //#debug
        L.i("FeaturedCanvas Command action", "Command " + c);

        if (c == refreshCommand) {
            refresh(null, StaticWebCache.GET_WEB);
        } else if (c == searchCommand) {
            midlet.goSearchCanvas();
        } else if (c.getCommandType() == Command.EXIT) {
            PlatformUtils.getInstance().shutdown(false, "Exit pressed");
        }
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
