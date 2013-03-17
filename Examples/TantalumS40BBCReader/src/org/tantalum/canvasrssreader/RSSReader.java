/*
 Copyright © 2012 Paul Houghton and Futurice on behalf of the Tantalum Project.
 All rights reserved.

 Tantalum software shall be used to make the world a better place for everyone.

 This software is licensed for use under the Apache 2 open source software license,
 http://www.apache.org/licenses/LICENSE-2.0.html

 You are kindly requested to return your improvements to this library to the
 open source community at http://projects.developer.nokia.com/Tantalum

 The above copyright and license notice notice shall be included in all copies
 or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package org.tantalum.canvasrssreader;

import javax.microedition.lcdui.*;
import org.tantalum.Task;
import org.tantalum.TimeoutException;
import org.tantalum.Worker;
import org.tantalum.j2me.TantalumMIDlet;
import org.tantalum.util.L;

/**
 * @author ssaa
 */
public class RSSReader extends TantalumMIDlet implements CommandListener {

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

    public RSSReader() {
        super(DEFAULT_NUMBER_OF_WORKER_THREADS);
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
        try {
            final Task reloadTask = new Task() {
                public Object exec(final Object params) {
                    getCanvas().getListView().reloadAsync(false);
                    
                    return null;
                }
            };
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
            L.e("Startup execption", "", ex);
            Worker.shutdown(false);
        }
    }
}
