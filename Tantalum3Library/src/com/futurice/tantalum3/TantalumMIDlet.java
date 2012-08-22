package com.futurice.tantalum3;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

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
    protected TantalumMIDlet(final int numberOfThreads) {
        PlatformUtils.setProgram(this);
        Worker.init(numberOfThreads);
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
    public void exitMIDlet(final boolean unconditional) {
        Worker.shutdown(unconditional);        
    }

    /**
     * Do not call this directly. Instead, call exitMidlet(false) to avoid
     * common errors.
     * 
     * If you do for some reason call this directly, realize the MIDlet will
     * exit immediately after the call is complete, rather than wait for you
     * to call notifyDestroyed() once an ongoing file or RMS write actions are
     * completed. Usually, this is not desirable, call exitMidlet() instead
     * and ongoing tasks will complete.
     *
     * If you want something done during shutdown, use
     * Worker.queueShutdownTask(Workable) and it will be handled for you.
     *
     * @param unconditional
     * @throws MIDletStateChangeException
     */
    protected final void destroyApp(final boolean unconditional) throws MIDletStateChangeException {
        exitMIDlet(unconditional);
    }

    /**
     * Do nothing, not generally used
     * 
     */
    protected void pauseApp() {
    }
}
