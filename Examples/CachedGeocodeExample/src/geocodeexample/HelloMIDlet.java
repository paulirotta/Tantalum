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
package geocodeexample;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.lcdui.*;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.tantalum.UITask;
import org.tantalum.Worker;
import org.tantalum.j2me.TantalumMIDlet;
import org.tantalum.util.L;
import org.tantalum.net.StaticWebCache;
import org.tantalum.storage.DataTypeHandler;

/**
 * Demonstration of a JSON service which returns locally cached values if the
 * same web service request has been performed before.
 * 
 * Note that you can force the cache to update with the latest results from the
 * web server by calling locationsCache.get() with the parameter
 * StaticWebCache.GET_WEB. You can also force offline-only operation to run off
 * the local cache using StaticWebCache.GET_LOCAL.
 * 
 * @author phou
 */
public class HelloMIDlet extends TantalumMIDlet implements CommandListener {
    private static final int NUMBER_OF_WORKERS = 1;
    private boolean midletPaused = false;

    public HelloMIDlet() {
        super(NUMBER_OF_WORKERS);
    }
    
    /*
     * Think of a cache as a Hashtable of key-value pairs, where the key is the
     * URL of a web service (HTTP GET of a unique URL).
     * 
     * This cache keep local copies of the web service to speed up the application
     * and allow it to work also when there is no network available (online-offline
     * application). When the phone runs out of flash memory for storing cached
     * data (too many pictures etc on the phone) the cache will automatically delete the
     * least-recently-used local data. If that data is requested again, it will
     * be requested from the web server.
     */
    private final StaticWebCache locationsCache = new StaticWebCache('0', new DataTypeHandler() {
        /**
         * This method converts the JSON byte[] received from the web service, or
         * stored in local flash memory, into an object we can use in the
         * application ("use form"). It is called by the cache automatically as
         * needed on a background Worker thread so the User Interface Thread (UI Thread)
         * is not blocked and the interface continues to be responsive.
         */
        public Object convertToUseForm(final byte[] bytes) {
            String out = "";
            String s = new String(bytes);
            try {
                JSONObject src = new JSONObject(s);
                JSONArray inn = src.getJSONArray("Placemark");
                JSONObject arr = inn.getJSONObject(1);
                JSONObject d = arr.getJSONObject("Point");
                JSONArray f = d.getJSONArray("coordinates");

                out = "Lat: " + f.getString(0) + " & Lon: " + f.getString(1);
            } catch (JSONException ex) {
                L.e("Can not parse JSON", s, ex);
            }

            return out;
        }
    });
//<editor-fold defaultstate="collapsed" desc=" Generated Fields ">//GEN-BEGIN:|fields|0|
    private Command exitCommand;
    private Command okCommand;
    private Form form;
    private StringItem locationStringItem;
    private TextField addressTextField;
//</editor-fold>//GEN-END:|fields|0|

//<editor-fold defaultstate="collapsed" desc=" Generated Methods ">//GEN-BEGIN:|methods|0|
//</editor-fold>//GEN-END:|methods|0|
//<editor-fold defaultstate="collapsed" desc=" Generated Method: initialize ">//GEN-BEGIN:|0-initialize|0|0-preInitialize
    /**
     * Initializes the application. It is called only once when the MIDlet is
     * started. The method is called before the
     * <code>startMIDlet</code> method.
     */
    private void initialize() {//GEN-END:|0-initialize|0|0-preInitialize
        // write pre-initialize user code here
//GEN-LINE:|0-initialize|1|0-postInitialize
        // write post-initialize user code here
        L.i("Hello", "initializing");
    }//GEN-BEGIN:|0-initialize|2|
//</editor-fold>//GEN-END:|0-initialize|2|

//<editor-fold defaultstate="collapsed" desc=" Generated Method: startMIDlet ">//GEN-BEGIN:|3-startMIDlet|0|3-preAction
    /**
     * Performs an action assigned to the Mobile Device - MIDlet Started point.
     */
    public void startMIDlet() {//GEN-END:|3-startMIDlet|0|3-preAction
        // write pre-action user code here
        switchDisplayable(null, getForm());//GEN-LINE:|3-startMIDlet|1|3-postAction
        // write post-action user code here
    }//GEN-BEGIN:|3-startMIDlet|2|
//</editor-fold>//GEN-END:|3-startMIDlet|2|

//<editor-fold defaultstate="collapsed" desc=" Generated Method: resumeMIDlet ">//GEN-BEGIN:|4-resumeMIDlet|0|4-preAction
    /**
     * Performs an action assigned to the Mobile Device - MIDlet Resumed point.
     */
    public void resumeMIDlet() {//GEN-END:|4-resumeMIDlet|0|4-preAction
        // write pre-action user code here
//GEN-LINE:|4-resumeMIDlet|1|4-postAction
        // write post-action user code here
    }//GEN-BEGIN:|4-resumeMIDlet|2|
//</editor-fold>//GEN-END:|4-resumeMIDlet|2|

//<editor-fold defaultstate="collapsed" desc=" Generated Method: switchDisplayable ">//GEN-BEGIN:|5-switchDisplayable|0|5-preSwitch
    /**
     * Switches a current displayable in a display. The
     * <code>display</code> instance is taken from
     * <code>getDisplay</code> method. This method is used by all actions in the
     * design for switching displayable.
     *
     * @param alert the Alert which is temporarily set to the display;
     * if <code>null</code>, then <code>nextDisplayable</code> is set
     * immediately
     * @param nextDisplayable the Displayable to be set
     */
    public void switchDisplayable(Alert alert, Displayable nextDisplayable) {//GEN-END:|5-switchDisplayable|0|5-preSwitch
        // write pre-switch user code here
        Display display = getDisplay();//GEN-BEGIN:|5-switchDisplayable|1|5-postSwitch
        if (alert == null) {
            display.setCurrent(nextDisplayable);
        } else {
            display.setCurrent(alert, nextDisplayable);
        }//GEN-END:|5-switchDisplayable|1|5-postSwitch
        // write post-switch user code here
    }//GEN-BEGIN:|5-switchDisplayable|2|
//</editor-fold>//GEN-END:|5-switchDisplayable|2|

//<editor-fold defaultstate="collapsed" desc=" Generated Method: commandAction for Displayables ">//GEN-BEGIN:|7-commandAction|0|7-preCommandAction
    /**
     * Called by a system to indicated that a command has been invoked on a
     * particular displayable.
     *
     * @param command the Command that was invoked
     * @param displayable the Displayable where the command was invoked
     */
    public void commandAction(Command command, Displayable displayable) {//GEN-END:|7-commandAction|0|7-preCommandAction
        // write pre-action user code here
        if (displayable == form) {//GEN-BEGIN:|7-commandAction|1|19-preAction
            if (command == exitCommand) {//GEN-END:|7-commandAction|1|19-preAction
                // write pre-action user code here
                exitMIDlet();//GEN-LINE:|7-commandAction|2|19-postAction
                // write post-action user code here
            } else if (command == okCommand) {//GEN-LINE:|7-commandAction|3|23-preAction
                // write pre-action user code here
//GEN-LINE:|7-commandAction|4|23-postAction
                // write post-action user code here
                
                /*
                 * Request with Worker.HIGH_PRIORITY to complete the action before
                 * any other pending Tasks which have not yet started. This is normal
                 * for Tasks done in the background in response to user input- the
                 * user wants to see the result of their latest action as soon as possible
                 * without the UI ever locking up.
                 */
                this.locationsCache.getAsync(getGeocodeUrl(this.getAddressTextField().getString().trim().toLowerCase()), Worker.HIGH_PRIORITY, StaticWebCache.GET_ANYWHERE, new UITask() {
                    protected void onPostExecute(Object result) {
                        // UI Thread callback on success
                        HelloMIDlet.this.getLocationStringItem().setText((String) result);
                    }

                    protected void onCanceled() {
                        // UI Thread callback if not already cached and the HTTP GET fails
                        HelloMIDlet.this.getLocationStringItem().setText("Service not available");
                    }
                });
            }//GEN-BEGIN:|7-commandAction|5|7-postCommandAction
        }//GEN-END:|7-commandAction|5|7-postCommandAction
        // write post-action user code here
    }//GEN-BEGIN:|7-commandAction|6|
//</editor-fold>//GEN-END:|7-commandAction|6|

//<editor-fold defaultstate="collapsed" desc=" Generated Getter: exitCommand ">//GEN-BEGIN:|18-getter|0|18-preInit
    /**
     * Returns an initialized instance of exitCommand component.
     *
     * @return the initialized component instance
     */
    public Command getExitCommand() {
        if (exitCommand == null) {//GEN-END:|18-getter|0|18-preInit
            // write pre-init user code here
            exitCommand = new Command("Exit", Command.EXIT, 0);//GEN-LINE:|18-getter|1|18-postInit
            // write post-init user code here
        }//GEN-BEGIN:|18-getter|2|
        return exitCommand;
    }
//</editor-fold>//GEN-END:|18-getter|2|

//<editor-fold defaultstate="collapsed" desc=" Generated Getter: form ">//GEN-BEGIN:|14-getter|0|14-preInit
    /**
     * Returns an initialized instance of form component.
     *
     * @return the initialized component instance
     */
    public Form getForm() {
        if (form == null) {//GEN-END:|14-getter|0|14-preInit
            // write pre-init user code here
            form = new Form("Geocoder", new Item[]{getAddressTextField(), getLocationStringItem()});//GEN-BEGIN:|14-getter|1|14-postInit
            form.addCommand(getExitCommand());
            form.addCommand(getOkCommand());
            form.setCommandListener(this);//GEN-END:|14-getter|1|14-postInit
            // write post-init user code here
        }//GEN-BEGIN:|14-getter|2|
        return form;
    }
//</editor-fold>//GEN-END:|14-getter|2|

//<editor-fold defaultstate="collapsed" desc=" Generated Getter: locationStringItem ">//GEN-BEGIN:|16-getter|0|16-preInit
    /**
     * Returns an initialized instance of locationStringItem component.
     *
     * @return the initialized component instance
     */
    public StringItem getLocationStringItem() {
        if (locationStringItem == null) {//GEN-END:|16-getter|0|16-preInit
            // write pre-init user code here
            locationStringItem = new StringItem("Coordinates", "(ready)");//GEN-LINE:|16-getter|1|16-postInit
            // write post-init user code here
        }//GEN-BEGIN:|16-getter|2|
        return locationStringItem;
    }
//</editor-fold>//GEN-END:|16-getter|2|

//<editor-fold defaultstate="collapsed" desc=" Generated Getter: okCommand ">//GEN-BEGIN:|22-getter|0|22-preInit
    /**
     * Returns an initialized instance of okCommand component.
     *
     * @return the initialized component instance
     */
    public Command getOkCommand() {
        if (okCommand == null) {//GEN-END:|22-getter|0|22-preInit
            // write pre-init user code here
            okCommand = new Command("Ok", Command.OK, 0);//GEN-LINE:|22-getter|1|22-postInit
            // write post-init user code here
        }//GEN-BEGIN:|22-getter|2|
        return okCommand;
    }
//</editor-fold>//GEN-END:|22-getter|2|

//<editor-fold defaultstate="collapsed" desc=" Generated Getter: addressTextField ">//GEN-BEGIN:|24-getter|0|24-preInit
    /**
     * Returns an initialized instance of addressTextField component.
     *
     * @return the initialized component instance
     */
    public TextField getAddressTextField() {
        if (addressTextField == null) {//GEN-END:|24-getter|0|24-preInit
            // write pre-init user code here
            addressTextField = new TextField("Address", "sabo, herbert macauley way, Lagos, Nigeria", 80, TextField.ANY);//GEN-BEGIN:|24-getter|1|24-postInit
            addressTextField.setPreferredSize(-1, -1);//GEN-END:|24-getter|1|24-postInit
            // write post-init user code here
        }//GEN-BEGIN:|24-getter|2|
        return addressTextField;
    }
//</editor-fold>//GEN-END:|24-getter|2|

    /**
     * Returns a display instance.
     *
     * @return the display instance.
     */
    public Display getDisplay() {
        return Display.getDisplay(this);
    }

    /**
     * Exits MIDlet.
     */
    public void exitMIDlet() {
        shutdown(true);
    }

    /**
     * Called when MIDlet is started. Checks whether the MIDlet have been
     * already started and initialize/starts or resumes the MIDlet.
     */
    public void startApp() {
        if (midletPaused) {
            resumeMIDlet();
        } else {
            initialize();
            startMIDlet();
        }
        midletPaused = false;
    }

    /**
     * Called when MIDlet is paused.
     */
    public void pauseApp() {
        midletPaused = true;
    }

    public String getGeocodeUrl(String address) {
        return "http://maps.google.com/maps/geo?q=" + urlEncode(address) + "&output=json";
    }
    
    private final static String UNRESERVED_CHARS = ".-_0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQURSTUVWXYZ*~";
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();
    public static String urlEncode(final String in) {
        StringBuffer buf = new StringBuffer();
        byte[] bytes = null;
        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream(in.length() * 5 / 4);
            final DataOutputStream dos = new DataOutputStream(bos);
            dos.writeUTF(in);
            bytes = bos.toByteArray();
            dos.close();
        } catch (IOException e) {
            //#debug
            L.e("Can not urlEncode", in, e);
        }
        for (int i = 2; i < bytes.length; i++) {
            byte b = bytes[i];
            if (UNRESERVED_CHARS.indexOf(b) >= 0) {
                buf.append((char) b);
            } else {
                buf.append('%');
                buf.append(HEX[(b >>> 4) & 0x0F]);
                buf.append(HEX[b & 0x0F]);
            }
        }
        return buf.toString();
    }
}
