import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import org.tantalum.PlatformUtils;
import org.tantalum.util.L;

public final class HelloWorldTantalumMIDlet extends MIDlet implements CommandListener, ItemCommandListener {

    /*
     * Tune this number up and down for best concurrency of your application. Less
     * than 2 is not allowed and more than 4 probably introduces too much context
     * switching between threads.
     */
    private static final int NUMBER_OF_WORKER_THREADS = 4;
    private final Form exampleForm = new Form("Example Form");
    private final Command exitCommand = new Command("Exit", Command.EXIT, 0);

    /**
     * This is called by the phone after the MIDlet is constructed. You must set
     * something as the current display item before this routine returns.
     */
    protected void startApp() {
        PlatformUtils.getInstance().setProgram(this, NUMBER_OF_WORKER_THREADS, PlatformUtils.NORMAL_LOG_MODE);
        init();
        Display.getDisplay(this).setCurrent(exampleForm);
    }

    /**
     * Initialize the application and first screen
     * 
     */
    private void init() {
        L.i("App init", "Started");
        exampleForm.setCommandListener(this);
        exampleForm.addCommand(exitCommand);
        exampleForm.append("Hello, Tantalum");
    }
 
    /**
     * Most phones including Nokia Series40 do not ever enter the "paused" state
     * so they do not call this.
     */
    protected void pauseApp() {
    }

    /**
     * Do not call this directly. Call
     * PlatformUtils.getInstance().shutdown(false) instead.
     *
     * This is part of MIDlet lifecycle model and may be called by the platform
     * at any time, for example when a users presses and holds the RED button on
     * their phone to force an application to close.
     *
     * @param unconditional
     */
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        L.i("destroyApp", "unconditional=" + unconditional);
        PlatformUtils.getInstance().shutdown(unconditional);
    }

    /**
     * Commands added to a Form or Canvas appear as menu items on the phone.
     * When the user presses that menu item, this method is called.
     * 
     * @param c
     * @param d 
     */
    public void commandAction(Command c, Displayable d) {
        L.i("Displayable commandAction", "command=" + c.getLabel());
        if (c.getCommandType() == Command.EXIT) {
            PlatformUtils.getInstance().shutdown(false);
        }
    }

    /**
     * Form Items use Commands of type Command.ITEM which are handled here.
     * 
     * Delete this if you do not use Forms
     * 
     * @param c
     * @param item 
     */
    public void commandAction(Command c, Item item) {
        L.i("Item commandAction", "command=" + c.getLabel());
    }
}
