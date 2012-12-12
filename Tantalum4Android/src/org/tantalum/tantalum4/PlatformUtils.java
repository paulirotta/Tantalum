/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.tantalum4;

import org.tantalum.tantalum4.PlatformUtils;
import android.app.Activity;
import org.tantalum.tantalum4.log.L;
import org.tantalum.tantalum4.storage.AndroidCache;
import org.tantalum.tantalum4.storage.FlashCache;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Android-specific support routines for Tantalum
 *
 * @author phou
 */
public final class PlatformUtils {

    private static Activity program;
    private static FlashCache flashDatabase;
    private static volatile Thread uiThread = null;

    /**
     * During initialization, the main program is set
     *
     * @param program
     */
    public static void setProgram(final Object program) {
        PlatformUtils.program = (Activity) program;
        runOnUiThread(new Runnable() {
            public void run() {
                uiThread = Thread.currentThread();
            }
        });
    }

    /**
     * Return a reference to the main program object appropriate for this phone
     * platform
     *
     * @return
     */
    public static Activity getProgram() {
        return PlatformUtils.program;
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
     * Add an object to be executed in the foreground on the event dispatch
     * thread. All popular JavaME applications require UI and input events to be
     * called serially only from this one thread.
     *
     * Note that if you queue too many object on the EDT you risk out of memory
     * and (more commonly) a temporarily unresponsive user interface.
     *
     * @param runnable
     */
    public static void runOnUiThread(final Runnable runnable) {
        program.runOnUiThread(runnable);
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
        program.finish();
    }

    /**
     * Get a persistent cache implementation appropriate for this phone platform
     *
     * @return
     */
    public static synchronized FlashCache getFlashCache() {
        if (flashDatabase == null) {
            flashDatabase = new AndroidCache();
        }

        return flashDatabase;
    }

    /**
     * Create an HTTP GET connection appropriate for this phone platform
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static HttpConn getHttpGetConn(final String url) throws IOException {
        final HttpConn httpConn = new HttpConn(url);
        httpConn.con.setDoInput(true);
        httpConn.con.setRequestMethod("GET");

        return httpConn;
    }

    /**
     * Create an HTTP POST connection appropriate for this phone platform
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static HttpConn getHttpPostConn(final String url, final byte[] bytes) throws IOException {
        OutputStream out = null;

        try {
            final HttpConn httpConn = new HttpConn(url);
            httpConn.con.setDoOutput(true);
            httpConn.con.setDoInput(true);
            httpConn.con.setRequestMethod("POST");
            out = httpConn.con.getOutputStream();
            out.write(bytes);

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
    public static final class HttpConn {

        final HttpURLConnection con;
        InputStream is = null;

        public HttpConn(final String url) throws IOException {
            con = (HttpURLConnection) new URL(url).openConnection();
        }

        public InputStream getInputStream() throws IOException {
            if (is == null) {
                is = con.getInputStream();
            }

            return is;
        }

        public long getLength() {
            final String s = con.getHeaderField("Content-Length");
            long length = 0;

            if (s != null && s.length() > 0) {
                length = Long.valueOf(s);
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
