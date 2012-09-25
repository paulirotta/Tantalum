package com.nokia.example.picasa.s40;

import com.futurice.tantalum3.Worker;
import com.futurice.tantalum3.log.L;
import com.futurice.tantalum3.net.StaticWebCache;
import com.nokia.mid.ui.LCDUIUtil;
import com.nokia.mid.ui.VirtualKeyboard;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

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
                final Image iconImage = Image.createImage("/connect.png");
                refreshCommand = (Command) new com.nokia.mid.ui.IconCommand("Refresh", "Refresh images", iconImage, iconImage, Command.OK, 0);
                VirtualKeyboard.hideOpenKeypadCommand(true);
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
            Worker.shutdown(false);
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
