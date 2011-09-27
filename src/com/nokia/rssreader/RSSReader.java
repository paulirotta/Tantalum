package com.nokia.rssreader;

import com.futurice.tantalum2.Log;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

/**
 * @author ssaa
 */
public class RSSReader extends MIDlet implements CommandListener {

    // This is read from the JAD-file
    public static String INITIAL_FEED_URL = null;

    private boolean midletPaused = false;

    private RSSReaderCanvas canvas;
    private SettingsForm settingsForm;
    private Displayable currentDisplayable;

    /**
     * The RSSReader constructor.
     */
    public RSSReader() {
    }

    /* Initilizes the application.
     * It is called only once when the MIDlet is started. The method is called before the <code>startMIDlet</code> method.
     */
    private void initialize() {
        Log.init(this);
        com.futurice.tantalum2.Worker.init(this, 2);
        INITIAL_FEED_URL = getAppProperty("RSS-Feed-Url");
    }

    /**
     * Performs an action assigned to the Mobile Device - MIDlet Started point.
     */
    public void startMIDlet() {
        switchDisplayable(null, getCanvas());
        getCanvas().getListView().reload(true);
    }

    /**
     * Performs an action assigned to the Mobile Device - MIDlet Resumed point.
     */
    public void resumeMIDlet() {
    }

    /**
     * Switches a current displayable in a display. The <code>display</code> instance is taken from <code>getDisplay</code> method. This method is used by all actions in the design for switching displayable.
     * @param alert the Alert which is temporarily set to the display; if <code>null</code>, then <code>nextDisplayable</code> is set immediately
     * @param nextDisplayable the Displayable to be set
     */
    public void switchDisplayable(Alert alert, Displayable nextDisplayable) {
        Display display = getDisplay();
        if (alert == null) {
            display.setCurrent(nextDisplayable);
        } else {
            display.setCurrent(alert, nextDisplayable);
        }
    }

    /**
     * Called by a system to indicated that a command has been invoked on a particular displayable.
     * @param command the Command that was invoked
     * @param displayable the Displayable where the command was invoked
     */
    public void commandAction(Command command, Displayable displayable) {
        if (displayable instanceof Alert) {
            switchDisplayable(null, currentDisplayable);
        }
    }

    /**
     * Returns an initiliazed instance of canvas component.
     * @return the initialized component instance
     */
    public RSSReaderCanvas getCanvas() {
        if (canvas == null) {
            canvas = new RSSReaderCanvas(this);         
        }
        return canvas;
    }

    /**
     * Returns an initiliazed instance of settingsForm component.
     * @return the initialized component instance
     */
    public SettingsForm getSettingsForm() {
        if (settingsForm == null) {
            settingsForm = new SettingsForm(this);
        }
        return settingsForm;
    }

    /**
     * Shows an error popup
     * @param errorMessage
     */
    public void showError(final String errorMessage) {
        Alert alert = new Alert("Error", errorMessage, null, AlertType.ERROR);
        alert.addCommand(new Command("Ok", Command.OK, 0));
        alert.setCommandListener(this);
        alert.setTimeout(Alert.FOREVER);
        currentDisplayable = getDisplay().getCurrent();
        switchDisplayable(alert, currentDisplayable);
    }

    /**
     * Returns a display instance.
     * @return the display instance.
     */
    public Display getDisplay () {
        return Display.getDisplay(this);
    }

    /**
     * Exits MIDlet.
     */
    public void exitMIDlet() {
        switchDisplayable (null, null);
        destroyApp(true);
        notifyDestroyed();
    }

    /**
     * Called when MIDlet is started.
     * Checks whether the MIDlet have been already started and initialize/starts or resumes the MIDlet.
     */
    public void startApp() {
        if (midletPaused) {
            resumeMIDlet ();
        } else {
            initialize ();
            startMIDlet ();
        }
        midletPaused = false;
    }

    /**
     * Called when MIDlet is paused.
     */
    public void pauseApp() {
        midletPaused = true;
    }

    /**
     * Called to signal the MIDlet to terminate.
     * @param unconditional if true, then the MIDlet has to be unconditionally terminated and all resources has to be released.
     */
    public void destroyApp(boolean unconditional) {
    }
}
