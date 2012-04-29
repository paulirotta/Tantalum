package com.futurice.s40rssreader;

import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.rms.RMSUtils;
import javax.microedition.lcdui.*;
import javax.microedition.rms.RecordStoreFullException;

/**
 * Simple settings form for setting the RSS Feed URL
 * @author ssaa
 */
public class SettingsForm extends TextBox implements CommandListener {

    private final RSSReader midlet;
    private Command saveCommand = new Command("Save", Command.OK, 0);
    private Command backCommand = new Command("Back", Command.BACK, 0);

    public SettingsForm(final RSSReader midlet) {
        super("RSS Feed URL", "", 256, TextField.URL & TextField.NON_PREDICTIVE);
        setInitialInputMode("UCB_BASIC_LATIN");
        this.midlet = midlet;

        addCommand(saveCommand);
        addCommand(backCommand);
        setCommandListener(this);
    }

    public void setUrlValue(String url) {
        setString(url);
    }

    public String getUrlValue() {
        return getString();
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == saveCommand) {
            try {
                RMSUtils.write("settings", getString().getBytes());
            } catch (RecordStoreFullException ex) {
                Log.l.log("Can not write settings", "", ex);
            }
            midlet.switchDisplayable(null, midlet.getCanvas());
            midlet.getCanvas().getListView().reload(true);
        } else if (command == backCommand) {
            midlet.switchDisplayable(null, midlet.getCanvas());
        }
    }
}
