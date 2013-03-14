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
import org.tantalum.PlatformUtils.HttpConn;
import org.tantalum.PlatformAdapter;
import org.tantalum.storage.FlashCache;
import org.tantalum.storage.ImageTypeHandler;
import org.tantalum.util.L;

/**
 * Utilities for support of each specific platform (J2ME, Android, ..)
 *
 * The platform-specific local copy of this class replaces this base version for
 * other platforms.
 *
 * @author phou
 */
public final class J2MEPlatformAdapter implements PlatformAdapter {

    /**
     * There is only one Display per application
     *
     */
    public final Display display;
    private final L log;

    /**
     * Create a new instance of the J2ME platform port helper class.
     *
     * This is created for you when your MIDlet extends AndroidMIDlet and calls
     * the super() constructor.
     * 
     * @param routeDebugOutputToSerialPort - some platform implementation (Android)
     * may ignore this parameter because they have their own mechanism for
     * remote debugging.
     */
    public J2MEPlatformAdapter(final boolean routeDebugOutputToSerialPort) {
        display = Display.getDisplay((MIDlet) PlatformUtils.getInstance().getProgram());
        log = new J2MELog(routeDebugOutputToSerialPort);
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
    public void doRunOnUiThread(final Runnable action) {
        display.callSerially(action);
    }

    /**
     * The application calls this when all Workers have completed shutdown to
     * inform the phone that the program has closed itself.
     *
     * Do not call this directly, call Worker.shutdown() to initiate a close
     *
     */
    public void doNotifyDestroyed() {
        ((MIDlet) PlatformUtils.getInstance().getProgram()).notifyDestroyed();
    }

    public ImageTypeHandler doGetImageTypeHandler() {
        return new J2MEImageTypeHandler();
    }

    public FlashCache doGetFlashCache(final char priority) {
        return new RMSCache(priority);
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
    public HttpConn doGetHttpConn(final String url, final Vector requestPropertyKeys, final Vector requestPropertyValues, final byte[] bytes, final String requestMethod) throws IOException {
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

    public L doGetLog() {
        return log;
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
