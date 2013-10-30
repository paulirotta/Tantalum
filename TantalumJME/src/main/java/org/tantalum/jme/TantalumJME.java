/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.jme;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import org.tantalum.PlatformUtils;

/**
 * Convenience constructor and destructor methods
 *
 * You can attach Tantalum Mobile to any existing app by calling these methods.
 *
 * If you do not call stop(), things work except Task.SHUTDOWN_PRIORTY and cache
 * shutdown tasks will not be run. There is also a risk that your application
 * closes during flash write operations, therefore calling stop() to exit your
 * application is recommended.
 *
 * @author phou
 */
public final class TantalumJME {

    /**
     * A good starting point for how many thread pool threads to create. You can
     * later tune this up and down based on phone details and what you are
     * doing. One of these threads is reserved for Task.FASTLANE_PRIORITY Tasks.
     *
     * Be aware that this also increases the number of concurrent HTTP
     * operations.
     */
    public static final int DEFAULT_NUMBER_OF_WORKER_THREADS = 4;

    /**
     * Start the Tantalum Mobile utilities such as logging and worker thread
     * pool
     *
     * @param midlet
     */
    public static void start(final MIDlet midlet) {
        PlatformUtils.getInstance().setProgram(midlet, DEFAULT_NUMBER_OF_WORKER_THREADS);
    }

    /**
     * Start the Tantalum Mobile utilities such as logging and worker thread
     * pool
     *
     * @param midlet
     * @param numberOfWorkerThreads
     * @param logMode - A log mode constant from <code>PlatformUtils</code>
     */
    public static void start(final MIDlet midlet, final int numberOfWorkerThreads, final int logMode) {
        PlatformUtils.getInstance().setProgram(midlet, numberOfWorkerThreads, logMode);
    }

    /**
     * A thread-safe convenience class for getting the program
     * 
     * @return 
     */
    public static MIDlet getMIDlet() {
        final MIDlet midlet = (MIDlet) PlatformUtils.getInstance().getProgram();

        
        if (midlet == null) {
            throw new IllegalStateException("You first need to initialize by calling TantalumJME.start()");
        }
        
        return midlet;
    }
    

    /**
     * A thread-safe convenience class for getting the display
     * 
     * @return 
     */
    public static Display getDisplay() {
        return Display.getDisplay(getMIDlet());
    }
    
    /**
     * Shut down the Tantalum utilities. This will block for up to 3 seconds
     * while flash memory writes and log writes (TantalumJME-debug.jar with
     * Task. only) occur, then the MIDlet will exit.
     *
     * @param reason - added to the log
     */
    public static void stop(final String reason) {
        PlatformUtils.getInstance().shutdown(reason);
    }
}
