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
package org.tantalum;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;
import org.tantalum.android.AndroidPlatformAdapter;
import org.tantalum.j2me.J2MEPlatformAdapter;
import org.tantalum.storage.FlashCache;
import org.tantalum.storage.ImageTypeHandler;
import org.tantalum.util.L;

/**
 * PlatformUtils aids in cross-platform development by starting and stopping the
 * background Worker threads when the program starts and stops. It is also used
 * to provide access to platform-independent functions such as object
 * persistence and networking utilities. The objects returned by many of these
 * methods are platform-specific implementations which hide API differences.
 *
 * @author phou
 */
public final class PlatformUtils {

    private static final String UNSUPPORTED_PLATFORM_MESSAGE = "Unsupported platform- getIntance(program) argument must be J2ME MIDlet or Android Activity";
    /**
     * PlatformUtils.setProgram() has not yet been called. Usually this is done
     * by overriding a platform-specific base class such as TantalumMIDlet or
     * TantalumActivity and calling the super() constructor.
     */
    public static final int PLATFORM_NOT_INITIALIZED = 0;
    /**
     * Automatically detected that we are in a J2ME phone
     */
    public static final int PLATFORM_J2ME = 1;
    /**
     * Automatically detected that we are in an Android phone
     */
    public static final int PLATFORM_ANDROID = 2;
    private int platform = PLATFORM_NOT_INITIALIZED;
    private PlatformAdapter platformAdapter = null;
    /**
     * How many Worker threads have been started
     */
    protected int numberOfWorkers = 0;
    /**
     * The base class for the application on this platform. On J2ME this is a
     * MIDlet, on Android this is an Activity
     */
    protected Object program;
    /**
     * The user interface thread for this program. This is initialized shortly
     * after application startup.
     */
    protected volatile Thread uiThread = null;
    /**
     * The platform-specific persistent memory handler
     */
    protected FlashCache flashCache;

    /**
     * Singleton constructor
     */
    private PlatformUtils() {
    }

    private static final class PlatformUtilsHolder {

        private static final PlatformUtils instance = new PlatformUtils();
    }

    /**
     * Access the singleton instance of PlatformUtils
     *
     * @return
     */
    public static PlatformUtils getInstance() {
        return PlatformUtils.PlatformUtilsHolder.instance;
    }

    /**
     * During startup, this is automatically called when you extend a
     * platform-specific base class such as TantalumMIDlet or TantalumActivity.
     * This initializes the background Worker threads and provides hooks for
     * orderly program shutdown with time for background tasks such as write to
     * flash memory to complete.
     *
     * @param program
     * @param numberOfWorkers
     * @param routeDebugOutputToSerialPort
     */
    public void setProgram(final Object program, final int numberOfWorkers, final boolean routeDebugOutputToSerialPort) {
        if (this.numberOfWorkers != 0) {
            throw new UnsupportedOperationException("You can only call PlatformUtils.getInstance().setProgram() one time per application");
        }
        if (numberOfWorkers < 2 || numberOfWorkers > 16) {
            throw new IllegalArgumentException("Less than 2 or more than 16 workers threads is not supported: " + numberOfWorkers);
        }

        this.program = program;
        this.numberOfWorkers = numberOfWorkers;

        try {
            if (Class.forName("org.tantalum.android.TantalumActivity").isAssignableFrom(program.getClass())) {
                platform = PLATFORM_ANDROID;
                platformAdapter = new AndroidPlatformAdapter();
                init();
                return;
            }
        } catch (Throwable t) {
            System.out.println("Can not init Android in setProgram(" + program.getClass().getName() + ") : " + t);
        }
        try {
            if (Class.forName("org.tantalum.j2me.TantalumMIDlet").isAssignableFrom(program.getClass())
                    || program.getClass().getName().toLowerCase().indexOf("test") > 0) {
                platform = PLATFORM_J2ME;
                platformAdapter = new J2MEPlatformAdapter(routeDebugOutputToSerialPort);
                init();
                return;
            }
        } catch (Throwable t) {
            System.out.println("Can not init J2ME in setProgram(" + program.getClass().getName() + ") : " + t);
        }

        throw new UnsupportedOperationException("SET PROGRAM: " + UNSUPPORTED_PLATFORM_MESSAGE + " : " + program.getClass().getName());
    }

    /**
     * Return a reference to the main program object appropriate for this phone
     * platform (J2ME MIDlet, Android Activity, ...)
     *
     * @return
     */
    public Object getProgram() {
        return program;
    }

    /**
     * Complete cross-platform initialization
     *
     */
    private void init() {
        Worker.init(numberOfWorkers);
        runOnUiThread(new Runnable() {
            public void run() {
                uiThread = Thread.currentThread();
            }
        });
    }

    /**
     * Check if the current execution thread is the user interface thread
     *
     * @return
     */
    public boolean isUIThread() {
        return Thread.currentThread() == uiThread;
    }

    /**
     * Indicate if this is probably a single core device and a hence parallel
     * fork() of CPU-intensive code is unlikely to lead to a speedup and may
     * lead to temporarily increased memory consumption.
     *
     * @return
     */
    public boolean isSingleCore() {
        //TODO check Android devices for number of cores at start time
        return PlatformUtils.getInstance().platform == PLATFORM_J2ME;
    }

    /**
     * The application calls this when all Workers have completed shutdown to
     * inform the phone that the program has closed itself.
     *
     * Do not call this directly, call Worker.shutdown() to initiate a close
     *
     * @param reasonDestroyed
     */
    public void notifyDestroyed(final String reasonDestroyed) {
        //#debug
        L.i("Call to notifyDestroyed", reasonDestroyed);
        platformAdapter.doNotifyDestroyed();
    }

    /**
     * Get a persistent cache implementation appropriate for this phone platform
     *
     * @param priority - a program-unique identifier, lower values are garbage
     * collected first
     * @return
     */
    public FlashCache getFlashCache(final char priority) {
        return platformAdapter.doGetFlashCache(priority);
    }

    /**
     * Get a DataTypeHandler for converting from compressed byte[] format (JPG,
     * PNG) to platform-specific image class.
     *
     * This is used by StaticCache and may include steps to shrink the image
     * before it is returned.
     *
     * @return
     */
    public ImageTypeHandler getImageTypeHandler() {
        return platformAdapter.doGetImageTypeHandler();
    }

    /**
     * This is used during application startup to get a platform-specific
     * logging helper class.
     *
     * Do not call this method directly. Call L.i() and L.e() instead.
     *
     * @return
     */
    public L getLog() {
        return platformAdapter.doGetLog();
    }

    /**
     * Execute the run() method of the action on the platform's user interface
     * thread. The object will be queued by the platform and run soon, after
     * previously queued incoming events and tasks.
     *
     * Beware that calling this too frequently may make your application user
     * interface laggy (response after a long time). If your interface responds
     * slowly, check first that you are not doing slow processes on the UI
     * thread, then try to reduce the number of calls to this method.
     *
     * Unfortunately there is no visibility to how large the user interface
     * event queue has grown so this warning can not be automated.
     *
     * @param action
     */
    public void runOnUiThread(final Runnable action) {
        platformAdapter.doRunOnUiThread(action);
    }

    /**
     * Create an HTTP GET connection appropriate for this phone platform
     *
     * @param url
     * @param requestPropertyKeys
     * @param requestPropertyValues
     * @return
     * @throws IOException
     */
    public HttpConn getHttpGetConn(final String url, final Vector requestPropertyKeys, final Vector requestPropertyValues) throws IOException {
        return platformAdapter.doGetHttpConn(url, requestPropertyKeys, requestPropertyValues, null, "GET");
    }

    /**
     * Create an HTTP POST connection appropriate for this phone platform
     *
     * @param url
     * @param requestPropertyKeys
     * @param requestPropertyValues
     * @param bytes
     * @return
     * @throws IOException
     */
    public HttpConn getHttpPostConn(final String url, final Vector requestPropertyKeys, final Vector requestPropertyValues, final byte[] bytes) throws IOException {
        return PlatformUtils.getInstance().platformAdapter.doGetHttpConn(url, requestPropertyKeys, requestPropertyValues, bytes, "POST");
    }

    /**
     * A convenience class abstracting HTTP connection operations between
     * different platforms.
     *
     * This is a platform-specific HTTP network connection. It provides access
     * to response codes and the HTTP header with the corresponding key-value
     * pairs
     */
    public static interface HttpConn {

        /**
         * Get the InputStream from the platform-specific HTTP connection
         *
         * @return
         * @throws IOException
         */
        public InputStream getInputStream() throws IOException;

        /**
         * Get the response code provided by the HTTP server after the
         * connection is made
         *
         * @return
         * @throws IOException
         */
        public int getResponseCode() throws IOException;

        /**
         * Get the HTTP header fields provided by the server
         *
         * @param headers
         * @throws IOException
         */
        public void getResponseHeaders(final Hashtable headers) throws IOException;

        /**
         * Get the length in bytes of the HTTP response body that the server
         * will send. Not all servers send this, in which case the response will
         * be of indeterminate length.
         *
         * @return
         */
        public long getLength();

        /**
         * Close the network connection. The underlying platform HTTP
         * implementation of the platform may choose to keep and re-use HTTP 1.1
         * connections after you close() to reduce connection time on additional
         * calls to this server.
         *
         * @throws IOException
         */
        public void close() throws IOException;
    }

    /**
     * Close the current application after all current queued and shutdown Tasks
     * are completed. Resources held by the system will be closed and queued
     * compute such as writing to the RMS or file system will complete.
     *
     * @param block Block the calling thread up to three seconds to allow
     * orderly shutdown. This is only needed in MIDlet.doNotifyDestroyed(true)
     * which is called for example by the user pressing the red HANGUP button.
     *
     * Ongoing Tasks will be canceled or complete depending on their current run
     * state and shutdown preference. The default is for Tasks not at
     * Task.PRIORITY_SHUTDOWN to be canceled immediately. In any case, all work
     * must finish within 3 seconds or risk being terminated by the phone OS if
     * this is an system-initiated application close.
     *
     * @param unconditional
     */
    public void shutdown(final boolean block) {
        Worker.shutdown(block);
    }
}
