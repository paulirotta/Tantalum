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
        final boolean doOutput = requestMethod.equals("POST");

        try {
            final AndroidHttpConn httpConn = new AndroidHttpConn(url, requestPropertyKeys, requestPropertyValues);
            httpConn.httpConnection.setDoOutput(doOutput);
            httpConn.httpConnection.setDoInput(true);
            httpConn.httpConnection.setRequestMethod(requestMethod);
            
            if (bytes != null) {
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
        OutputStream os = null;

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
         * Get the OutputStream associated with this HTTP connection
         *
         * @return
         * @throws IOException
         */
        public OutputStream getOutputStream() throws IOException {
            if (os == null) {
                os = httpConnection.getOutputStream();
            }

            return os;
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
            if (os != null) {
                os.close();
            }
            httpConnection.disconnect();
        }
    }
}
