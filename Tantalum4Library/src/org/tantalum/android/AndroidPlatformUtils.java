/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.android;

import android.app.Activity;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;
import org.tantalum.PlatformUtils;

/**
 * Android-specific support routines for Tantalum
 *
 * @author phou
 */
public final class AndroidPlatformUtils extends PlatformUtils {

    private static final int DEFAULT_NUMBER_OF_WORKERS = 8;

    public AndroidPlatformUtils() {
        if (PlatformUtils.numberOfWorkers == 0) {
            PlatformUtils.setNumberOfWorkers(DEFAULT_NUMBER_OF_WORKERS);
        }
    }

    protected void doRunOnUiThread(final Runnable action) {
        ((Activity) program).runOnUiThread(action);
    }

    protected void doNotifyDestroyed() {
        ((Activity) program).finish();
    }

    /**
     * Create an HTTP GET connection appropriate for this phone platform
     *
     * @param url
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
    protected HttpConn doGetHttpConn(final String url, final Vector requestPropertyKeys, final Vector requestPropertyValues, final byte[] bytes, final String requestMethod) throws IOException {
        OutputStream out = null;
        final boolean doOutput = bytes != null;

        try {
            System.out.println("1" + url);
            final AndroidHttpConn httpConn = new AndroidHttpConn(url, requestPropertyKeys, requestPropertyValues);
            System.out.println("2" + url);
            httpConn.httpConnection.setDoOutput(doOutput);
            System.out.println("3" + url);
            httpConn.httpConnection.setDoInput(true);
            System.out.println("4" + url);
            httpConn.httpConnection.setRequestMethod(requestMethod);
            if (doOutput) {
                out = httpConn.httpConnection.getOutputStream();
                out.write(bytes);
            }
            System.out.println("5" + url);

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
    public static final class AndroidHttpConn implements PlatformUtils.HttpConn {

        final HttpURLConnection httpConnection;
        InputStream is = null;

        public AndroidHttpConn(final String url, final Vector requestPropertyKeys, final Vector requestPropertyValues) throws IOException {
            httpConnection = (HttpURLConnection) new URL(url).openConnection();
            for (int i = 0; i < requestPropertyKeys.size(); i++) {
                httpConnection.setRequestProperty((String) requestPropertyKeys.elementAt(i), (String) requestPropertyValues.elementAt(i));
            }
        }

        public InputStream getInputStream() throws IOException {
            if (is == null) {
                is = httpConnection.getInputStream();
            }

            return is;
        }

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

        public final void close() throws IOException {
            if (is != null) {
                is.close();
            }
        }
    }
}
