/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.s40rssreader;

import com.futurice.tantalum3.log.Log;
import com.nokia.mid.ui.IconCommand;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Image;

/**
 *
 * @author phou
 */
public class UpdateIconCommand extends IconCommand {
    static Image image = null;
    
    static {
        try {
            image = Image.createImage("/connect.png");
        } catch (Exception e) {
            Log.l.log("Can not initialize", "Update icon image", e);
        }
    }
    
    public UpdateIconCommand() {
        super("Update", "Update article list", image, image, Command.OK, 0);
    }
}
