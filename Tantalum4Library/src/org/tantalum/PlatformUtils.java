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
package org.tantalum;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;
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
public abstract class PlatformUtils {

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
    private static int platform = PLATFORM_NOT_INITIALIZED;
    /**
     * How many Worker threads have been started
     */
    protected static int numberOfWorkers = 0;
    /**
     * Once PlatformUtils.setProgram() has been called at startup, you can
     * access the platform-specific platformUtils object here.
     */
    public static PlatformUtils platformUtils = null;
    /**
     * The base class for the application on this platform. On J2ME this is a
     * MIDlet, on Android this is an Activity
     */
    protected static Object program;
    /**
     * The user interface thread for this program. This is initialized shortly
     * after application startup.
     */
    protected static volatile Thread uiThread = null;
    /**
     * The platform-specific persistent memory handler
     */
    protected static FlashCache flashCache;

    /**
     * Call this at program startup before calling PlatformUtils.setProgram()
     *
     * If you do not explicitly call this, a default number of workers
     * appropriate for the platform will be used. This is currently 4 generic
     * worker threads on J2ME and 8 generic worker threads on Android.
     *
     * @param numberOfWorkers
     */
    public static synchronized void setNumberOfWorkers(final int numberOfWorkers) {
        PlatformUtils.numberOfWorkers = numberOfWorkers;
    }

    /**
     * During startup, this is automatically called when you extend a
     * platform-specific base class such as TantalumMIDlet or TantalumActivity.
     * This initializes the background Worker threads and provides hooks for
     * orderly program shutdown with time for background tasks such as write to
     * flash memory to complete.
     *
     * @param program
     */
    public static synchronized void setProgram(final Object program) {
        PlatformUtils.program = program;

        try {
            if (Class.forName("org.tantalum.android.TantalumActivity").isAssignableFrom(program.getClass())) {
                PlatformUtils.platform = PLATFORM_ANDROID;
                PlatformUtils.program = program;
                PlatformUtils.platformUtils = (PlatformUtils) Class.forName("org.tantalum.android.AndroidPlatformUtils").newInstance();
                Worker.init(numberOfWorkers);
                runOnUiThread(new Runnable() {
                    public void run() {
                        uiThread = Thread.currentThread();
                    }
                });

                return;
            }
        } catch (Throwable t) {
            System.out.println("Can not init Android in setProgram(" + program.getClass().getName() + ") : " + t);
        }
        try {
            if (Class.forName("org.tantalum.j2me.TantalumMIDlet").isAssignableFrom(program.getClass())) {
                PlatformUtils.platform = PLATFORM_J2ME;
                PlatformUtils.platformUtils = (PlatformUtils) Class.forName("org.tantalum.j2me.J2MEPlatformUtils").newInstance();
                Worker.init(numberOfWorkers);
                runOnUiThread(new Runnable() {
                    public void run() {
                        uiThread = Thread.currentThread();
                    }
                });

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
     * Indicate if this is probably a single core device and a hence parallel fork()
     * of CPU-intensive code is unlikely to lead to a speedup and may lead to temporarily
     * increased memory consumption.
     * 
     * @return 
     */
    public static boolean isSingleCore() {
        return PlatformUtils.platform == PLATFORM_J2ME;
    }

    /**
     * The application calls this when all Workers have completed shutdown to
     * inform the phone that the program has closed itself.
     *
     * Do not call this directly, call Worker.shutdown() to initiate a close
     *
     * @param reasonDestroyed
     */
    public static void notifyDestroyed(final String reasonDestroyed) {
        L.i("Call to notifyDestroyed", reasonDestroyed);
        platformUtils.doNotifyDestroyed();
    }

    /**
     * The platform-specific life cycle implementation of telling the phone the
     * application is ready to terminate.
     */
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
                        flashCache = (FlashCache) Class.forName("org.tantalum.j2me.RMSCache").newInstance();
                        break;

                    case PLATFORM_ANDROID:
                        flashCache = (FlashCache) Class.forName("org.tantalum.android.AndroidCache").newInstance();
                        break;

                    default:
                        throw new UnsupportedOperationException("GET FLASH CACHE: " + UNSUPPORTED_PLATFORM_MESSAGE);
                }
            } catch (Throwable t) {
                //#debug
                L.e("Can not getFlashCache", platformUtils.toString(), t);
            }
        }

        return flashCache;
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
    public static synchronized ImageTypeHandler getImageTypeHandler() {
        ImageTypeHandler imageTypeHandler = null;

        try {
            switch (PlatformUtils.platform) {
                case PLATFORM_J2ME:
                    imageTypeHandler = (ImageTypeHandler) Class.forName("org.tantalum.j2me.J2MEImageTypeHandler").newInstance();
                    break;

                case PLATFORM_ANDROID:
                    imageTypeHandler = (ImageTypeHandler) Class.forName("org.tantalum.android.AndroidImageTypeHandler").newInstance();
                    break;

                default:
                    throw new UnsupportedOperationException("GET IMAGE TYPE HANDLER: " + UNSUPPORTED_PLATFORM_MESSAGE);
            }
        } catch (Throwable t) {
            //#debug
            L.e("Can not getImageTypeHandler()", platformUtils.toString(), t);
        }

        return imageTypeHandler;
    }

    /**
     * This is used during application startup to get a platform-specific
     * logging helper class.
     *
     * Do not call this method directly. Call L.i() and L.e() instead.
     *
     * @return
     */
    public static synchronized L getLog() {
        L log = null;
        try {
            switch (PlatformUtils.platform) {
                case PLATFORM_J2ME:
                    log = (L) Class.forName("org.tantalum.j2me.J2MELog").newInstance();
                    break;

                case PLATFORM_ANDROID:
                    log = (L) Class.forName("org.tantalum.android.AndroidLog").newInstance();
                    break;

                default:
                    throw new UnsupportedOperationException("LOG: " + UNSUPPORTED_PLATFORM_MESSAGE);
            }
        } catch (Throwable t) {
            //#debug
            System.out.println("Can not init platform log " + platformUtils.toString() + " : " + t);
        }

        return log;
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
    public static void runOnUiThread(final Runnable action) {
        PlatformUtils.platformUtils.doRunOnUiThread(action);
    }

    /**
     * Platform-specific implementation of runOnUiThread
     *
     * @param action
     */
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
}
