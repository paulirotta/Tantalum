/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.blackberry;

import net.rim.device.api.ui.UiApplication;
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
 * @author ADIKSONLINE
 */
public class TantalumBB {
    
    /**
     * A good starting point for how many thread pool threads to create. You can
     * later tune this up and down based on phone details and what you are
     * doing. One of these threads is reserved for Task.FASTLANE_PRIORITY Tasks.
     *
     * Generally you can use larger numbers on multi-core and high performance
     * devices. Be aware that this also increases the number of concurrent HTTP
     * operations.
     */
    public static final int DEFAULT_NUMBER_OF_WORKER_THREADS = 4;
    
    /**
     * Start the Tantalum Mobile utilities such as logging and worker thread
     * pool.
     * 
     * @param app 
     */
    public static void start(final UiApplication app){
        PlatformUtils.getInstance().setProgram(app, DEFAULT_NUMBER_OF_WORKER_THREADS);
    }
    
    /**
     * Start the Tantalum Mobile utilities such as logging and worker thread
     * pool.
     * 
     * @param app
     * @param numberOfWorkerThreads 
     */
    public static void start(final UiApplication app, final int numberOfWorkerThreads){
        PlatformUtils.getInstance().setProgram(app, numberOfWorkerThreads);
    }
    
    /**
     * Start the Tantalum Mobile utilities such as logging and worker thread
     * pool.
     * 
     * @param app
     * @param numberOfWorkerThreads
     * @param logMode 
     */
    public static void start(final UiApplication app, final int numberOfWorkerThreads, final int logMode){
        PlatformUtils.getInstance().setProgram(app, numberOfWorkerThreads, logMode);
    }
    
    /**
     * Shut down the Tantalum utilities. This will block for up to 3 seconds
     * while flash memory writes to phone database (TantalumAndroid-debug.jar
     * only) occur, then the Activity will exit.
     *
     * @param reason - added to the log
     */
    public static void stop(final String reason){
        PlatformUtils.getInstance().shutdown(true, reason);
    }
    
}
