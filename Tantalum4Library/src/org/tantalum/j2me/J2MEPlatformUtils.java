/**
 * J2ME implementation of platform-specific libraries
 *
 */
package org.tantalum.j2me;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import org.tantalum.PlatformUtils;
import org.tantalum.util.L;
import org.tantalum.storage.FlashCache;

/**
 * Utilities for support of each specific platform (J2ME, Android, ..)
 *
 * The platform-specific local copy of this class replaces this base version for
 * other platforms.
 *
 * @author phou
 */
public final class J2MEPlatformUtils extends PlatformUtils {

    private static final int DEFAULT_NUMBER_OF_WORKERS = 4;
    final Display display = Display.getDisplay((MIDlet) program);

    protected J2MEPlatformUtils() {
        if (PlatformUtils.numberOfWorkers == 0) {
            PlatformUtils.setNumberOfWorkers(DEFAULT_NUMBER_OF_WORKERS);
        }
        runOnUiThread(new Runnable() {
            public void run() {
                uiThread = Thread.currentThread();
            }
        });
    }

    /**
     * Add an object to be executed in the foreground on the event dispatch
     * thread. All popular JavaME applications require UI and input events to be
     * called serially only from this one thread.
     *
     * Note that if you queue too many object on the EDT you risk out of memory
     * and (more commonly) a temporarily unresponsive user interface.
     *
     * @param action
     */
    protected void doRunOnUiThread(final Runnable action) {
        display.callSerially(action);
    }

    /**
     * The application calls this when all Workers have completed shutdown to
     * inform the phone that the program has closed itself.
     *
     * Do not call this directly, call Worker.shutdown() to initiate a close
     *
     */
    protected void doNotifyDestroyed() {
        ((MIDlet) program).notifyDestroyed();
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
    protected HttpConn doGetHttpConn(final String url, final Vector requestPropertyKeys, final Vector requestPropertyValues, final byte[] bytes, final String requestMethod) throws IOException {
        OutputStream out = null;

        try {
            final J2MEHttpConn httpConn = new J2MEHttpConn(url, requestPropertyKeys, requestPropertyValues);
            httpConn.httpConnection.setRequestMethod(requestMethod);
            if (bytes != null) {
                out = httpConn.httpConnection.openOutputStream();
                out.write(bytes);
            }

            return httpConn;
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * A convenience class abstracting HTTP connection operations between
     * different platforms
     */
    public static final class J2MEHttpConn implements PlatformUtils.HttpConn {

        final HttpConnection httpConnection;
        InputStream is = null;

        /**
         * Create a platform-specific HTTP network connection, and set the HTTP
         * header with the corresponding key-value pairs
         *
         * @param url
         * @param requestPropertyKeys - a list of header keys
         * @param requestPropertyValues - a list of associated header values
         * @throws IOException
         */
        public J2MEHttpConn(final String url, final Vector requestPropertyKeys, final Vector requestPropertyValues) throws IOException {
            httpConnection = (HttpConnection) Connector.open(url);
            for (int i = 0; i < requestPropertyKeys.size(); i++) {
                httpConnection.setRequestProperty((String) requestPropertyKeys.elementAt(i), (String) requestPropertyValues.elementAt(i));
            }
        }

        /**
         * Get the InputStream from the platform-specific HTTP connection
         *
         * @return
         * @throws IOException
         */
        public InputStream getInputStream() throws IOException {
            if (is == null) {
                is = httpConnection.openInputStream();
            }

            return is;
        }

        /**
         * Get the response code provided by the HTTP server after the
         * connection is made
         *
         * @return
         * @throws IOException
         */
        public int getResponseCode() throws IOException {
            return httpConnection.getResponseCode();
        }

        public void getResponseHeaders(final Hashtable headers) throws IOException {
            for (int i = 0; i < 10000; i++) {
                final String key = httpConnection.getHeaderFieldKey(i);
                if (key == null) {
                    break;
                }
                final String value = httpConnection.getHeaderField(i);
                headers.put(key, value);
            };
        }

        public long getLength() {
            return httpConnection.getLength();
        }

        public void close() throws IOException {
            if (is != null) {
                is.close();
                is = null;
            }
            httpConnection.close();
        }
    }
}
