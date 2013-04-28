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
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.tantalum.storage.FlashCache;
import org.tantalum.storage.FlashDatabaseException;
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

    /**
     * A cache type stored in the flash memory on the phone in the phone's own
     * database format
     */
    public static final int PHONE_DATABASE_CACHE = 0;
    /**
     * A cache type stored in the flash memory on the phone in the file system
     *
     * This type is not yet supported- value is reserved for future use
     */
    public static final int PHONE_FILESYSTEM_CACHE = 1;
    /**
     * A cache type stored in the flash memory on the phone's memory card in the
     * file system
     *
     * This type is not yet supported- value is reserved for future use
     */
    public static final int MEMORY_CARD_FILESYSTEM_CACHE = 2;
    /**
     * Send log output to the standard output device, usually your IDE log
     * window
     */
    public static final int NORMAL_LOG_MODE = 0;
    /**
     * Open a serial port connection to terminal-emulator software opened to the
     * USB serial port on your computer. On Windows open Control Panel - System
     * - Devices to set the maximum baud rate, no parity and hardware CTS flow
     * control and to the same in terminal emulation software such as Putty
     *
     * With the release build of the Tantalum library, this setting is ignored
     * and there will not be any log output.
     */
    public static final int USB_SERIAL_PORT_LOG_MODE = 1;
    /**
     * Store the most recent run log data as "tantalum.log" on the phone's
     * memory card in the root directory.
     *
     * With the release build of the Tantalum library, this setting is ignored
     * and there will not be any log output.
     */
    public static final int MEMORY_CARD_LOG_MODE = 2;
    private static final String UNSUPPORTED_PLATFORM_MESSAGE = "Unsupported platform- getIntance(program) argument must be JME MIDlet or Android Activity";
    /**
     * PlatformUtils.setProgram() has not yet been called. Usually this is done
     * by overriding a platform-specific base class such as TantalumMIDlet or
     * TantalumActivity and calling the super() constructor.
     */
    public static final int PLATFORM_NOT_INITIALIZED = 0;
    /**
     * Automatically detected that we are in a JME phone
     */
    public static final int PLATFORM_JME = 1;
    /**
     * Automatically detected that we are in an Android phone
     */
    public static final int PLATFORM_ANDROID = 2;
    /**
     * Automatically detects that we are using a Blackberry device
     */
    public static final int PLATFORM_BLACKBERRY = 3;
    
    private int platform = PLATFORM_NOT_INITIALIZED;
    private PlatformAdapter platformAdapter = null;
    /**
     * How many Worker threads have been started
     */
    protected int numberOfWorkers = 0;
    /**
     * The base class for the application on this platform. On JME this is a
     * MIDlet, on Android this is an Activity
     */
    protected Object program;
    /**
     * The user interface thread for this program. This is initialized shortly
     * after application startup.
     */
    protected volatile Thread uiThread = null;
    private static final Object MUTEX = new Object();
    private boolean shutdownComplete = false;

    /*
     * When can we next fire the vibrate mode. This filter prevents too-frequence calls to vibrate
     */
    private long nextAvailableVibrationtime = 0;

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
     * During startup, specify the TantalumMIDlet or TantalumActivity that
     * drives the Tantalum lifecycle.
     *
     * @param program
     * @param numberOfWorkers
     */
    public void setProgram(final Object program, final int numberOfWorkers) {
        setProgram(program, numberOfWorkers, 0);
    }

    /**
     * During startup, specify the TantalumMIDlet or TantalumActivity that
     * drives the Tantalum lifecycle. You do not need to extend these classes,
     * you can call this method directly.
     *
     * Calling this method one time as the application starts initializes the
     * background Worker threads and provides hooks for orderly program shutdown
     * with time for background tasks such as write to flash memory to complete.
     *
     * @param program
     * @param numberOfWorkers
     * @param logMode
     */
    public void setProgram(final Object program, final int numberOfWorkers, final int logMode) {
        if (this.numberOfWorkers != 0) {
            throw new UnsupportedOperationException("You can only call PlatformUtils.getInstance().setProgram() one time per application");
        }
        if (numberOfWorkers < 2 || numberOfWorkers > 16) {
            throw new IllegalArgumentException("Less than 2 or more than 16 workers threads is not supported: " + numberOfWorkers);
        }

        this.program = program;
        this.numberOfWorkers = numberOfWorkers;

        try {
            if (Class.forName("android.app.Activity").isAssignableFrom(program.getClass())) {
                platform = PLATFORM_ANDROID;
                platformAdapter = (PlatformAdapter) Class.forName("org.tantalum.android.AndroidPlatformAdapter").newInstance();
                init(logMode);
                return;
            }
        } catch (Throwable t) {
            System.out.println("Can not init Android in setProgram(" + program.getClass().getName() + ") : " + t);
        }
        try {
            if (Class.forName("net.rim.device.api.ui.UiApplication").isAssignableFrom(program.getClass())) {
                platform = PLATFORM_BLACKBERRY;
                platformAdapter = (PlatformAdapter) Class.forName("org.tantalum.blackberry.BBPlatformAdapter").newInstance();
                init(logMode);
                return;
            }
        } catch (Throwable t) {
            System.out.println("Can not init Blackberry in setProgram(" + program.getClass().getName() + ") : " + t);
        }
        try {
            if (Class.forName("javax.microedition.midlet.MIDlet").isAssignableFrom(program.getClass())
                    || program.getClass().getName().toLowerCase().indexOf("test") > 0) {
                platform = PLATFORM_JME;
                platformAdapter = (PlatformAdapter) Class.forName("org.tantalum.jme.JMEPlatformAdapter").newInstance();
                init(logMode);
                return;
            }
        } catch (Throwable t) {
            System.out.println("Can not init JME in setProgram(" + program.getClass().getName() + ") : " + t);
        }

        throw new UnsupportedOperationException("SET PROGRAM: " + UNSUPPORTED_PLATFORM_MESSAGE + " : " + program.getClass().getName());
    }

    /**
     * Return a reference to the main program object appropriate for this phone
     * platform (JME MIDlet, Android Activity, ...)
     *
     * @return
     */
    public Object getProgram() {
        return program;
    }

    /**
     * Complete cross-platform initialization
     *
     * @param logMode
     */
    private void init(final int logMode) {
        platformAdapter.init(logMode);
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
        return PlatformUtils.getInstance().platform == PLATFORM_JME;
    }

    /**
     * The application calls this when all Workers have completed shutdown to
     * inform the phone that the program has closed itself.
     *
     * Do not call this directly, call Worker.shutdown() to initiate a close
     *
     * @param reasonDestroyed
     * @return
     */
    public boolean shutdownComplete(final String reasonDestroyed) {
        synchronized (MUTEX) {
            if (shutdownComplete) {
                return shutdownComplete;
            }
            shutdownComplete = true;
        }
        //#mdebug
        L.i(this, "Call to notifyDestroyed", reasonDestroyed);
        L.shutdown();
        //#enddebug
        platformAdapter.shutdownComplete();

        return true;
    }

    /**
     * Vibrate the phone
     *
     * You can specify a lockoutTime to filter out repeated vibrations in too
     * short an interval
     *
     * @param duration in milliseconds. Zero is allowed, there will be no
     * vibration
     * @param lockoutTime if we vibrate, for how long after vibration stops
     * should the phone ignore additional vibration requests prevent a new
     * vibration from starting. Set zero to not set a timeout
     */
    public void vibrateAsync(final int duration, final int lockoutTime) {
        if (duration < 0 || lockoutTime < 0) {
            throw new IllegalArgumentException("vibrateAsync() requires positive duration and lockTimeout: duration=" + duration + " lockTimeout=" + lockoutTime);
        }
        if (duration == 0) {
            return;
        }

        Runnable timekeeperLambda = null;
        if (lockoutTime > 0) {
            timekeeperLambda = new Runnable() {
                public void run() {
                    synchronized (MUTEX) {
                        nextAvailableVibrationtime = System.currentTimeMillis() + duration + lockoutTime;
                    }
                }
            };
        }

        synchronized (MUTEX) {
            if (System.currentTimeMillis() > nextAvailableVibrationtime) {
                //#debug
                L.i(this, "Call to vibrate", "duration=" + duration + " lockoutTime=" + lockoutTime);
                if (timekeeperLambda != null) {
                    // We update the filter when actually running async on the UI thread
                    nextAvailableVibrationtime = Long.MAX_VALUE;
                } else {
                    nextAvailableVibrationtime = 0;
                }
                platformAdapter.vibrateAsync(duration, timekeeperLambda);
            } else {
                //#debug
                L.i("Vibrate filtered out- we are still in the previous vibration's lockout time window", "duration=" + duration + " lockoutTime=" + lockoutTime);
            }
        }
    }

    /**
     * Get a persistent cache implementation appropriate for this phone platform
     *
     * @param priority - a program-unique identifier, lower values are garbage
     * collected first
     * @param cacheType
     * @return the new or existing cache object
     * @throws FlashDatabaseException
     */
    public FlashCache getFlashCache(final char priority, final int cacheType) throws FlashDatabaseException {
        return platformAdapter.getFlashCache(priority, cacheType);
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
        return platformAdapter.getImageTypeHandler();
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
        return platformAdapter.getLog();
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
        platformAdapter.runOnUiThread(action);
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
        return platformAdapter.getHttpConn(url, requestPropertyKeys, requestPropertyValues, null, "GET");
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
        return PlatformUtils.getInstance().platformAdapter.getHttpConn(url, requestPropertyKeys, requestPropertyValues, bytes, "POST");


    }

    /**
     * Close the current application after all current queued and shutdown Tasks
     * are completed. Resources held by the system will be closed and queued
     * compute such as writing to the RMS or file system will complete.
     *
     * @param block Block the calling thread up to three seconds to allow
     * orderly shutdown. This is only needed in shutdown(true) which is called
     * for example by the user pressing the red HANGUP button.
     *
     * Ongoing Tasks will be canceled or complete depending on their current run
     * state and shutdown preference. The default is for Tasks not at
     * Task.PRIORITY_SHUTDOWN to be canceled immediately. In any case, all work
     * must finish within 3 seconds or risk being terminated by the phone OS if
     * this is an system-initiated application close.
     *
     */
    public void shutdown(final boolean block) {
        //#debug
        L.i(this, "Shutdown", "block calling thread up to 3 seconds=" + block);
        Worker.shutdown(block);
    }

    /**
     * A human-readable copy of the response headers
     *
     * @return
     */
    public static String responseHeadersToString(final Hashtable responseHeaders) {
        if (responseHeaders.isEmpty()) {
            return "(no HTTP response headers)";
        }

        final StringBuffer sb = new StringBuffer();
        final Enumeration keys = responseHeaders.keys();

        while (keys.hasMoreElements()) {
            final String k = (String) keys.nextElement();
            final String[] values = (String[]) responseHeaders.get(k);
            for (int i = 0; i < values.length; i++) {
                sb.append("\r\n   ");
                sb.append(k);
                sb.append(": ");
                sb.append(values[i]);
            }
        }

        return sb.toString();
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
         * Get the OutputStream from the platform-specific HTTP connection
         *
         * @return
         * @throws IOException
         */
        public OutputStream getOutputStream() throws IOException;

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
         * Since a key may occur multiple times in the response header, as with
         * cookies, the value associated with each key is a String[] of all
         * associated values. In most cases there is only one response and you
         * can safely use [0] if the result is non-null.
         *
         * Since
         * <code>Hashtable</code> is not thread-safe, you are responsible for
         * synchronizing your provided
         * <code>Hashtable</code> if the connection is kept around and the
         * values are used by more than one thread. Keeping the connection after
         * the data is checks is unusual, so there is not normally a concern.
         *
         * @param headers
         * @throws IOException
         */
        public void getResponseHeaders(Hashtable headers) throws IOException;

        /**
         * Get the length in bytes of the HTTP response body that the server
         * will send. Not all servers send this, in which case the response will
         * be of indeterminate length.
         *
         * @return
         */
        public long getLength();

        /**
         * If the routine will read the entire HTTP contents into memory in one
         * operation, what is the maximum Content-Length header we should accept
         * before automatically canceling the operation to prevent a likely
         * OutOfMemoryError
         *
         * @return
         */
        public long getMaxLengthSupportedAsBlockOperation();

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
}
