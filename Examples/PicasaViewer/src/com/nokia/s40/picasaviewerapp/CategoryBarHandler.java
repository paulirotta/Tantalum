/*
 * Copyright © 2012 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */
package com.nokia.s40.picasaviewerapp;

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Image;

import org.tantalum.util.L;


import com.nokia.mid.ui.CategoryBar;
import com.nokia.mid.ui.DirectUtils;
import com.nokia.mid.ui.ElementListener;
import com.nokia.mid.ui.IconCommand;
import org.tantalum.PlatformUtils;

/**
 *
 * @author phou
 */
public final class CategoryBarHandler implements ElementListener {

    private static PicasaViewer midlet;
    private CategoryBar categoryBar;
    private IconCommand featuredIconCommand;
    private IconCommand searchIconCommand;
    private Command refreshCommand;

    public CategoryBarHandler() {
    }

    public void setMidlet(final PicasaViewer midlet) {
        CategoryBarHandler.midlet = midlet;
        try {
            Image homeImg = Image.createImage("/s40/home.png");
            featuredIconCommand = new IconCommand("Home", "Home View", homeImg, drawMaskedImage(homeImg), IconCommand.SCREEN, 1);
        } catch (IOException e) {
            //#debug
            L.e("category bar handler init", "Can not create home icon", e);
            featuredIconCommand = new IconCommand("Home", "Home View", IconCommand.SCREEN, 1, IconCommand.ICON_OK);
        }
        try {
            Image searchImg = Image.createImage("/s40/search.png");
            searchIconCommand = new IconCommand("Search", "Search View", searchImg, drawMaskedImage(searchImg), IconCommand.SCREEN, 1);
        } catch (IOException e) {
            //#debug
            L.e("category bar handler init", "Can not create search icon", e);
            searchIconCommand = new IconCommand("Search", "Search View", IconCommand.SCREEN, 1, IconCommand.ICON_OK);
        }
        try {
            final Image iconImage = Image.createImage("/s40/connect.png");
            refreshCommand = (Command) new IconCommand("Refresh", "Refresh images", iconImage, iconImage, Command.OK, 0);
        } catch (IOException e) {
            //#debug
            L.e("category bar handler init", "Can not create refresh icon", e);
            searchIconCommand = new IconCommand("Search", "Search View", IconCommand.SCREEN, 1, IconCommand.ICON_OK);
        }

        final IconCommand[] iconCommands = {featuredIconCommand, searchIconCommand};
        categoryBar = new CategoryBar(iconCommands, true);
        categoryBar.setVisibility(true);
        categoryBar.setElementListener(this);
    }

    public Command getRefreshIconCommand() throws IOException {
        return refreshCommand;
    }

    /**
     * This method takes an image and overlays all the visible pixels with the
     * specified color. This is useful for single color icons that should be
     * colored according to the theme of the device.
     *
     * @param image image
     * @return The resulting image
     */
    public Image drawMaskedImage(Image image) {
        final int color = Display.getDisplay(midlet).getColor(Display.COLOR_HIGHLIGHTED_BORDER);
        // Store the pixel array of the image
        final int[] sourceData = new int[image.getHeight() * image.getWidth()];
        image.getRGB(sourceData, 0, image.getWidth(), 0, 0, image.getWidth(),
                image.getHeight());

        // Overlay non-transparent pixels with the specified color
        for (int i = 0; i < sourceData.length; i++) {
            sourceData[i] = (sourceData[i] & 0xFF000000)
                    | (color & 0x00FFFFFF);
        }

        // Create the new image
        final Image overlayed = DirectUtils.createImage(image.getWidth(), image.getHeight(), 0x000000);
        overlayed.getGraphics().drawRGB(sourceData, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight(), true);

        return overlayed;
    }

    public void notifyElementSelected(CategoryBar cb, int i) {
        switch (i) {
            case ElementListener.BACK:
                PlatformUtils.getInstance().shutdown(false, "Back button pressed");
                break;
            case 0:
                midlet.goFeaturedCanvas();
                break;
            case 1:
                midlet.goSearchCanvas();
                break;
        }
    }

    public void setVisibility(final boolean visibility) {
        categoryBar.setVisibility(visibility);
    }
}
