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
package org.tantalum.android;

import org.tantalum.PlatformAdapter;
import android.app.Activity;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;
import org.tantalum.PlatformUtils;
import org.tantalum.PlatformUtils.HttpConn;
import org.tantalum.storage.FlashCache;
import org.tantalum.storage.ImageTypeHandler;
import org.tantalum.util.L;

/**
 * Android-specific support routines for Tantalum
 *
 * @author phou
 */
public final class AndroidPlatformAdapter implements PlatformAdapter {

    private final L log;

    public AndroidPlatformAdapter() {
        log = new AndroidLog();
    }

    /**
     * Queue the action to the Android UI thread
     *
     * @param action
     */
    public void doRunOnUiThread(final Runnable action) {
        ((Activity) PlatformUtils.getInstance().getProgram()).runOnUiThread(action);
    }

    /**
     * End the Android Activity class. This will cause the phone to remove the
     * program from the display and free resources.
     *
     * Do not call this automatically. It is called for you when the user
     * navigates to another Activity, or asynchronously after a slight delay
     * when you call Worker.shutdown() to terminate the program.
     */
    public void doNotifyDestroyed() {
        ((Activity) PlatformUtils.getInstance().getProgram()).finish();
    }

    /**
     * Create an HTTP GET connection appropriate for this phone platform
     *
     * @param url
     * @param requestPropertyKeys header tags for the server
     * @param requestPropertyValues values associated with each header key
     * @return
     * @throws IOException
     */
    public static HttpConn getHttpGetConn(final String url, final Vector requestPropertyKeys, final Vector requestPropertyValues) throws IOException {
        final AndroidHttpConn httpConn = new AndroidHttpConn(url, requestPropertyKeys, requestPropertyValues);
        httpConn.httpConnection.setDoInput(true);
        httpConn.httpConnection.setRequestMethod("GET");

        return httpConn;
    }

    /**
     * Create an HTTP PUT connection appropriate for this phone platform
     *
     * @param url
     * @return
     * @throws IOException
     */
    public HttpConn doGetHttpConn(final String url, final Vector requestPropertyKeys, final Vector requestPropertyValues, final byte[] bytes, final String requestMethod) throws IOException {
        OutputStream out = null;
        final boolean doOutput = bytes != null;

        try {
            final AndroidHttpConn httpConn = new AndroidHttpConn(url, requestPropertyKeys, requestPropertyValues);
            httpConn.httpConnection.setDoOutput(doOutput);
            httpConn.httpConnection.setDoInput(true);
            httpConn.httpConnection.setRequestMethod(requestMethod);
            if (doOutput) {
                out = httpConn.httpConnection.getOutputStream();
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

    public FlashCache doGetFlashCache(final char priority) {
        return new AndroidCache(priority);
    }

    public ImageTypeHandler doGetImageTypeHandler() {
        return new AndroidImageTypeHandler();
    }

    /**
     * A convenience class abstracting HTTP connection operations between
     * different platforms
     */
    public static final class AndroidHttpConn implements PlatformUtils.HttpConn {

        final HttpURLConnection httpConnection;
        InputStream is = null;

        /**
         * Create an Android-specific handler for HttpConnecions
         *
         * This is done for you when you use the HttpGetter and similar utility
         * classes. You generally do not create this class directly.
         *
         * @param url
         * @param requestPropertyKeys
         * @param requestPropertyValues
         * @throws IOException
         */
        public AndroidHttpConn(final String url, final Vector requestPropertyKeys, final Vector requestPropertyValues) throws IOException {
            httpConnection = (HttpURLConnection) new URL(url).openConnection();
            for (int i = 0; i < requestPropertyKeys.size(); i++) {
                httpConnection.setRequestProperty((String) requestPropertyKeys.elementAt(i), (String) requestPropertyValues.elementAt(i));
            }
        }

        /**
         * Get the InputStream associated with this HTTP connection
         *
         * @return
         * @throws IOException
         */
        public InputStream getInputStream() throws IOException {
            if (is == null) {
                is = httpConnection.getInputStream();
            }

            return is;
        }

        /**
         * Get the HTTP response code returned by the server
         *
         * @return
         * @throws IOException
         */
        public int getResponseCode() throws IOException {
            return httpConnection.getResponseCode();
        }

        /**
         * Get the HTTP headers returned from the server
         *
         * @param headers
         * @throws IOException
         */
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

        /**
         * Add an HTTP header property which will be set to the server
         *
         * @param key
         * @param value
         * @throws IOException
         */
        public void setRequestProperty(final String key, final String value) throws IOException {
            httpConnection.setRequestProperty(key, value);
        }

        public long getLength() {
            final String s = httpConnection.getHeaderField("Content-Length");
            long length = 0;

            if (s != null && s.length() > 0) {
                length = Long.parseLong(s);
            }

            return length;
        }

        /**
         * Close the InputStream associated with this connection.
         *
         * The underlying Android HTTP 1.1 connection implementation probably
         * remains connected to the server for some time and will be re-used
         *
         * @throws IOException
         */
        public final void close() throws IOException {
            if (is != null) {
                is.close();
            }
            httpConnection.disconnect();
        }
    }
}
