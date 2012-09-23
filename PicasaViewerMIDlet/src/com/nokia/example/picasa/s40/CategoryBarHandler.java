/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.example.picasa.s40;

import com.futurice.tantalum3.PlatformUtils;
import com.futurice.tantalum3.Worker;
import com.futurice.tantalum3.log.L;
import com.nokia.mid.ui.CategoryBar;
import com.nokia.mid.ui.DirectUtils;
import com.nokia.mid.ui.ElementListener;
import com.nokia.mid.ui.IconCommand;
import java.io.IOException;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Image;

/**
 *
 * @author phou
 */
public final class CategoryBarHandler implements ElementListener {

    private static PicasaViewer midlet;
    private CategoryBar categoryBar;
    private IconCommand featured;
    private IconCommand search;

    public CategoryBarHandler() {
        try {
            Image homeImg = Image.createImage("/home.png");
            featured = new IconCommand("Home", "Home View", homeImg, drawMaskedImage(homeImg), IconCommand.SCREEN, 1);
        } catch (IOException e) {
            //#debug
            L.e("category bar handler init", "Can not create home icon", e);
            featured = new IconCommand("Home", "Home View", IconCommand.SCREEN, 1, IconCommand.ICON_OK);
        }
        try {
            Image searchImg = Image.createImage("/search.png");
            search = new IconCommand("Search", "Search View", searchImg, drawMaskedImage(searchImg), IconCommand.SCREEN, 1);
        } catch (IOException e) {
            //#debug
            L.e("category bar handler init", "Can not create search icon", e);
            search = new IconCommand("Search", "Search View", IconCommand.SCREEN, 1, IconCommand.ICON_OK);
        }
        final IconCommand[] iconCommands = {featured, search};
        categoryBar = new CategoryBar(iconCommands, true);
        categoryBar.setVisibility(true);
        categoryBar.setElementListener(this);
    }

    public static void setMidlet(final PicasaViewer midlet) {
        CategoryBarHandler.midlet = midlet;
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
        int color = Display.getDisplay(midlet).getColor(Display.COLOR_HIGHLIGHTED_BORDER);
        // Store the pixel array of the image
        int[] sourceData = new int[image.getHeight() * image.getWidth()];
        image.getRGB(sourceData, 0, image.getWidth(), 0, 0, image.getWidth(),
                image.getHeight());

        // Overlay non-transparent pixels with the specified color
        for (int i = 0; i < sourceData.length; i++) {
            sourceData[i] = (sourceData[i] & 0xFF000000)
                    | (color & 0x00FFFFFF);
        }

        // Create the new image
        Image overlayed = DirectUtils.createImage(image.getWidth(), image.getHeight(), 0x000000);
        overlayed.getGraphics().drawRGB(sourceData, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight(), true);
        return overlayed;
    }

    public void notifyElementSelected(CategoryBar cb, int i) {
        switch (i) {
            case ElementListener.BACK:
                Worker.shutdown(false);
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
//        PlatformUtils.runOnUiThread(new Runnable() {
//            public void run() {
                categoryBar.setVisibility(visibility);
//            }
//        });
    }
}
