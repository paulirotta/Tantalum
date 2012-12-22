/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import org.tantalum.util.L;
import org.tantalum.storage.FlashCache;

/**
 *
 * @author phou
 */
public abstract class PlatformUtils {

    private static final String UNSUPPORTED_PLATFORM_MESSAGE = "Unsupported platform- getIntance(program) argument must be J2ME MIDlet or Android Activity";
    public static final int PLATFORM_NOT_INITIALIZED = 0;
    public static final int PLATFORM_J2ME = 1;
    public static final int PLATFORM_ANDROID = 2;
    private static int platform = PLATFORM_NOT_INITIALIZED;
    protected static int numberOfWorkers = 0;
    public static PlatformUtils platformUtils = null;
    protected static Object program;
    protected static volatile Thread uiThread = null;
    protected static FlashCache flashCache;

    public static synchronized void setNumberOfWorkers(final int numberOfWorkers) {
        PlatformUtils.numberOfWorkers = numberOfWorkers;
    }

    public static synchronized void setProgram(final Object program) {
        try {
            if (program.getClass().isAssignableFrom(Class.forName("javax.microedition.midlet.MIDlet"))) {
                PlatformUtils.platform = PLATFORM_J2ME;
                PlatformUtils.program = program;
                PlatformUtils.platformUtils = (PlatformUtils) Class.forName("javax.microedition.midlet.MIDlet").newInstance();
                Worker.init(numberOfWorkers);

                return;
            }
        } catch (Throwable t) {
        }
        try {
            if (program.getClass().isAssignableFrom(Class.forName("android.app.Activity"))) {
                PlatformUtils.platform = PLATFORM_ANDROID;
                PlatformUtils.program = program;
                PlatformUtils.platformUtils = (PlatformUtils) Class.forName("android.app.Activity").newInstance();
                Worker.init(numberOfWorkers);

                return;
            }
        } catch (Throwable t) {
        }

        throw new IllegalArgumentException(UNSUPPORTED_PLATFORM_MESSAGE);
    }

    protected PlatformUtils() {
        runOnUiThread(new Runnable() {
            public void run() {
                uiThread = Thread.currentThread();
            }
        });
    }

    /**
     * Return a reference to the main program object appropriate for this phone
     * platform (J2ME MIDlet, Android Activity, ...)
     *
     * @return
     */
    public static Object getProgram() {
        return program;
    }

    /**
     * Check if the current execution thread is the user interface thread
     *
     * @return
     */
    public static boolean isUIThread() {
        return Thread.currentThread() == uiThread;
    }

    /**
     * The application calls this when all Workers have completed shutdown to
     * inform the phone that the program has closed itself.
     *
     * Do not call this directly, call Worker.shutdown() to initiate a close
     *
     */
    public static void notifyDestroyed(final String reasonDestroyed) {
        L.i("Call to notifyDestroyed", reasonDestroyed);
        platformUtils.doNotifyDestroyed();
    }

    protected abstract void doNotifyDestroyed();

    /**
     * Get a persistent cache implementation appropriate for this phone platform
     *
     * @return
     */
    public static synchronized FlashCache getFlashCache() {
        if (flashCache == null) {
            try {
                switch (PlatformUtils.platform) {
                    case PLATFORM_J2ME:
                        flashCache = (FlashCache) Class.forName("org.tantalum.j2me.RMSCache").getClass().newInstance();
                        break;

                    case PLATFORM_ANDROID:
                        flashCache = (FlashCache) Class.forName("org.tantalum.android.AndroidCache").getClass().newInstance();
                        break;

                    default:
                        throw new IllegalArgumentException(UNSUPPORTED_PLATFORM_MESSAGE);
                }
            } catch (Exception e) {
                //#debug
                L.e("Can not getFlashCache", platformUtils.toString(), e);
            }
        }

        return flashCache;
    }
    
    public static void runOnUiThread(final Runnable action) {
        PlatformUtils.platformUtils.doRunOnUiThread(action);
    }
    
    protected abstract void doRunOnUiThread(Runnable action);

    /**
     * Create an HTTP GET connection appropriate for this phone platform
     *
     * @param url
     * @param requestPropertyKeys
     * @param requestPropertyValues
     * @return
     * @throws IOException
     */
    public static HttpConn getHttpGetConn(final String url, final Vector requestPropertyKeys, final Vector requestPropertyValues) throws IOException {
        return PlatformUtils.platformUtils.doGetHttpConn(url, requestPropertyKeys, requestPropertyValues, null, "GET");
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
    public static HttpConn getHttpPostConn(final String url, final Vector requestPropertyKeys, final Vector requestPropertyValues, final byte[] bytes) throws IOException {
        return PlatformUtils.platformUtils.doGetHttpConn(url, requestPropertyKeys, requestPropertyValues, bytes, "POST");
    }

    /**
     * Create an HTTP PUT connection appropriate for this phone platform
     *
     * @param url
     * @param requestPropertyKeys
     * @param requestPropertyValues
     * @param bytes
     * @param requestMethod
     * @return
     * @throws IOException
     */
    protected abstract HttpConn doGetHttpConn(String url, Vector requestPropertyKeys, Vector requestPropertyValues, byte[] bytes, String requestMethod) throws IOException;
    
    /**
     * Create an HTTP PUT connection appropriate for this phone platform
     *
     * @param url
     * @return
     * @throws IOException
     */
//    public static HttpConn getHttpPutConn(final String url, final Vector requestPropertyKeys, final Vector requestPropertyValues, final byte[] bytes) throws IOException {
//        return doGetHttpPostOrPutConn(url, requestPropertyKeys, requestPropertyValues, bytes, "PUT");
//    }
    
     /**
     * A convenience class abstracting HTTP connection operations between
     * different platforms.
     * 
     * This is a platform-specific HTTP network connection. It provides access
     * to response codes and the HTTP header with the corresponding key-value pairs
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

        public void getResponseHeaders(final Hashtable headers) throws IOException;

        public long getLength();

        public void close() throws IOException;
    }
}
