package org.tantalum.j2me;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import org.tantalum.PlatformUtils;
import org.tantalum.Worker;

/**
 * This is a convenience class to embody the best practice patterns for starting
 * and stopping an app which uses Tantalum. Using this class is optional, but
 * makes your life simpler. You can either extend it, or implement your own
 * variant.
 *
 * @author phou
 */
public abstract class TantalumMIDlet extends MIDlet {

    /**
     * If you create a MIDlet constructor, you must call super() as the first
     * line of your MIDlet's constructor. Alternatively, you can call
     * Worker.init() yourself with custom parameters for special needs.
     * 
     * 4 worker threads are recommended for most applications.
     * 
     * @param numberOfThreads 
     * 
     */
    protected TantalumMIDlet() {
        super();
        
        PlatformUtils.setProgram(this);
    }

    /**
     * Call this to close your MIDlet in an orderly manner, exactly the same
     * way it is closed if the system sends you a destoryApp().
     * 
     * Ongoing Work tasks will complete, or if you set unconditional then they
     * will complete within 3 seconds.
     * 
     * @param unconditional 
     */
    public void shutdown(final boolean unconditional) {
        Worker.shutdown(unconditional);        
    }

    /**
     * Do not call this directly. Call exitMidlet(false) instead.
     * 
     * This is part of MIDlet lifecycle model and may be called by the platform,
     * for example when a users presses and holds the RED button on their phone
     * to force an application to close.
     *
     * @param unconditional
     * @throws MIDletStateChangeException
     */
    protected final void destroyApp(final boolean unconditional) throws MIDletStateChangeException {
        shutdown(unconditional);
    }

    /**
     * Do nothing, not generally used
     * 
     */
    protected void pauseApp() {
    }
}
