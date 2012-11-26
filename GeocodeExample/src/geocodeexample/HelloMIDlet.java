/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package geocodeexample;

import com.futurice.tantalum4.TantalumMIDlet;
import com.futurice.tantalum4.Task;
import com.futurice.tantalum4.UITask;
import com.futurice.tantalum4.Worker;
import com.futurice.tantalum4.log.L;
import com.futurice.tantalum4.net.StaticWebCache;
import javax.microedition.lcdui.*;

/**
 * @author phou
 */
public class HelloMIDlet extends TantalumMIDlet implements CommandListener {

    private boolean midletPaused = false;
    private String json = "";
    private final StaticWebCache locationsCache = new StaticWebCache('0', new LocationDataTypeHandler());
//<editor-fold defaultstate="collapsed" desc=" Generated Fields ">//GEN-BEGIN:|fields|0|
    private Command exitCommand;
    private Command okCommand;
    private Form form;
    private StringItem locationStringItem;
//</editor-fold>//GEN-END:|fields|0|

    /**
     * The HelloMIDlet constructor.
     */
    public HelloMIDlet() {
        super(4);
    }

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
                final Task task = this.locationsCache.get(new Geocoder().getGeocodeUrl("sabo, herbert macauley way, Lagos, Nigeria"), new UITask() {
                    public Object doInBackground(Object in) { // Worker thread
                        json = (String) in;
                        (new Task() {
                            protected Object doInBackground(Object in) {
                                // In parallel
                                return in;
                            }
                        }).fork();

                        return in;
                    }

                    protected void onPostExecute(Object result) { // UI Thread
                        HelloMIDlet.this.getLocationStringItem().setText((String) result);
                    }

                    protected void onCanceled() { // UI Thread
                        HelloMIDlet.this.getLocationStringItem().setText("You suck");
                    }
                }, Worker.HIGH_PRIORITY, StaticWebCache.GET_ANYWHERE);
                Worker.fork(new Task() {
                    protected Object doInBackground(Object in) {
                        try {
                            HelloMIDlet.this.getLocationStringItem().setText("Joined..");
                            task.join(2000);
                            HelloMIDlet.this.getLocationStringItem().setText("After join: " + task.getStatusString());
                        } catch (Exception ex) {
                            L.e("Problem with monitor thread", "", ex);
                            HelloMIDlet.this.getLocationStringItem().setText("After join: " + ex);
                        }

                        return in;
                    }
                });

//                                HttpGetter getter = new HttpGetter(new Geocoder().getGeocodeUrl("sabo, herbert macauley way, Lagos, Nigeria"), 3);
//                Worker.fork(getter);
//                try {
//                    byte[] bytes = (byte[]) getter.get();
//                    String s = new String(bytes);
//                    L.i("Result", s);
//                } catch (Exception e) {
//                    L.e("Could not get", getter.getUrl(), e);
//                }
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
            form = new Form("Welcome", new Item[]{getLocationStringItem()});//GEN-BEGIN:|14-getter|1|14-postInit
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
            locationStringItem = new StringItem("Location", "(ready)");//GEN-LINE:|16-getter|1|16-postInit
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
        exitMIDlet(true);
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
}
