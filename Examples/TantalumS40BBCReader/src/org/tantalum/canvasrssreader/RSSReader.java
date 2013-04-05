/*
 Copyright (c) 2013, Paul Houghton and Futurice Oy
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
package org.tantalum.canvasrssreader;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import org.tantalum.PlatformUtils;
import org.tantalum.Task;
import org.tantalum.util.L;

/**
 * @author ssaa
 */
public class RSSReader extends MIDlet implements CommandListener {

    // This is read from the JAD-file
    public static final String INITIAL_FEED_URL = "http://feeds.bbci.co.uk/news/rss.xml";
    private RSSReaderCanvas canvas;
    private Displayable currentDisplayable;
    public static int COLOR_BACKGROUND;
    public static int COLOR_HIGHLIGHTED_BACKGROUND;
    public static int COLOR_FOREGROUND;
    public static int COLOR_HIGHLIGHTED_FOREGROUND;
    public static int COLOR_BORDER;
    public static int COLOR_HIGHLIGHTED_BORDER;

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
        try {
            PlatformUtils.getInstance().setProgram(this, 4, PlatformUtils.NORMAL_LOG_MODE);
            final Task reloadTask = getCanvas().getListView().reloadAsync(false);
            final Display display = getDisplay();
            COLOR_BACKGROUND = display.getColor(Display.COLOR_BACKGROUND);
            COLOR_HIGHLIGHTED_BACKGROUND = display.getColor(Display.COLOR_HIGHLIGHTED_BACKGROUND);
            COLOR_FOREGROUND = display.getColor(Display.COLOR_FOREGROUND);
            COLOR_HIGHLIGHTED_FOREGROUND = display.getColor(Display.COLOR_HIGHLIGHTED_FOREGROUND);
            COLOR_BORDER = display.getColor(Display.COLOR_BORDER);
            COLOR_HIGHLIGHTED_BORDER = display.getColor(Display.COLOR_HIGHLIGHTED_BORDER);
            try {
                reloadTask.join(200);
            } catch (Exception ex) {
                //#debug
                L.e("Startup reloadAsync timeout", "This is normal if loading from the net", ex);
            }
            switchDisplayable(null, canvas);
        } catch (Exception ex) {
            //#debug
            L.e("Startup exception", "", ex);
            PlatformUtils.getInstance().shutdown(false);
        }
    }

    protected void pauseApp() {
    }

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        PlatformUtils.getInstance().shutdown(unconditional);
    }
}
