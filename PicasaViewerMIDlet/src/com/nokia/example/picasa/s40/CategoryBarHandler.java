/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.example.picasa.s40;

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
    private int previousCategoryIndex = 0;
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
                // Confirmation alert
                Worker.shutdown(false);
//                final Alert alert = new Alert("Quit?", "Really quit?", null, AlertType.CONFIRMATION);
//                alert.addCommand(new Command("Yes", Command.OK, 1));
//                alert.addCommand(new Command("No", Command.CANCEL, 1));
//                alert.setCommandListener(new CommandListener() {
//                    public void commandAction(Command c, Displayable d) {
//                        if (c.getLabel().equals("Yes")) {
//                            Worker.shutdown(false);
//                        }
//                        if (c.getLabel().equals("No")) {
//                            midlet.goBack();
//                        }
//                    }
//                });
//                Display.getDisplay(midlet).setCurrent(alert);
                break;
            case 0:
                Display.getDisplay(midlet).setCurrent(midlet.featuredView);
                previousCategoryIndex = categoryBar.getSelectedIndex();
                break;
            case 1:
                Display.getDisplay(midlet).setCurrent(midlet.searchView);
                previousCategoryIndex = categoryBar.getSelectedIndex();
                break;
            case 2:
                midlet.setDetailed();
                break;
        }
    }

    public void setVisibility(final boolean visibility) {
        categoryBar.setVisibility(visibility);
    }

    public void goBack() {
        categoryBar.setVisibility(true);
        categoryBar.setSelectedIndex(previousCategoryIndex);
    }
}
