package com.futurice.s40rssreader;

import com.futurice.tantalum3.CancellationException;
import com.futurice.tantalum3.ExecutionException;
import com.futurice.tantalum3.TantalumMIDlet;
import com.futurice.tantalum3.Task;
import com.futurice.tantalum3.TimeoutException;
import com.futurice.tantalum3.Worker;
import com.futurice.tantalum3.log.L;
import javax.microedition.lcdui.*;

/**
 * @author ssaa
 */
public class RSSReader extends TantalumMIDlet implements CommandListener {

    // This is read from the JAD-file
    public static String INITIAL_FEED_URL = null;
    private RSSReaderCanvas canvas;
    private Displayable currentDisplayable;
    public static int COLOR_BACKGROUND;
    public static int COLOR_HIGHLIGHTED_BACKGROUND;
    public static int COLOR_FOREGROUND;
    public static int COLOR_HIGHLIGHTED_FOREGROUND;
    public static int COLOR_BORDER;
    public static int COLOR_HIGHLIGHTED_BORDER;

    public RSSReader() {
        super(4);
    }

    /**
     * Switches a current displayable in a display. The
     * <code>display</code> instance is taken from
     * <code>getDisplay</code> method. This method is used by all actions in the
     * design for switching displayable.
     *
     * @param alert the Alert which is temporarily set to the display; if
     * <code>null</code>, then <code>nextDisplayable</code> is set immediately
     * @param nextDisplayable the Displayable to be set
     */
    public void switchDisplayable(Alert alert, Displayable nextDisplayable) {
        final Display display = getDisplay();

        if (alert == null) {
            display.setCurrent(nextDisplayable);
        } else {
            display.setCurrent(alert, nextDisplayable);
        }
    }

    /**
     * Called by a system to indicated that a command has been invoked on a
     * particular displayable.
     *
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
     *
     * @return the initialized component instance
     */
    public RSSReaderCanvas getCanvas() {
        if (canvas == null) {
            canvas = new RSSReaderCanvas(this);
        }
        return canvas;
    }

    /**
     * Shows an error popup
     *
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
     *
     * @return the display instance.
     */
    public Display getDisplay() {
        return Display.getDisplay(this);
    }

    /**
     * Called when MIDlet is started. Checks whether the MIDlet have been
     * already started and initialize/starts or resumes the MIDlet.
     */
    public void startApp() {
        INITIAL_FEED_URL = getAppProperty("RSS-Feed-Url");
        try {
            getCanvas();
            final Task reloadTask = new Task() {
                public void exec() {
                    canvas.getListView().reload(false);
                }
            };
            Worker.fork(reloadTask);
            final Display display = getDisplay();
            COLOR_BACKGROUND = display.getColor(Display.COLOR_BACKGROUND);
            COLOR_HIGHLIGHTED_BACKGROUND = display.getColor(Display.COLOR_HIGHLIGHTED_BACKGROUND);
            COLOR_FOREGROUND = display.getColor(Display.COLOR_FOREGROUND);
            COLOR_HIGHLIGHTED_FOREGROUND = display.getColor(Display.COLOR_HIGHLIGHTED_FOREGROUND);
            COLOR_BORDER = display.getColor(Display.COLOR_BORDER);
            COLOR_HIGHLIGHTED_BORDER = display.getColor(Display.COLOR_HIGHLIGHTED_BORDER);
            try {
                reloadTask.join(2000);
            } catch (TimeoutException ex) {
                //#debug
                L.l.e("Startup reload timeout", "This is normal if loading from the net", ex);
            }
            switchDisplayable(null, canvas);
        } catch (Exception ex) {
            //#debug
            L.l.e("Startup execption", "", ex);
            Worker.shutdown(false);
        }
    }
}
