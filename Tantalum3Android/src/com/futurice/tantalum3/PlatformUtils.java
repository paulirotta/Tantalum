/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3;

import android.app.Activity;
import com.futurice.tantalum3.rms.AndroidCache;
import com.futurice.tantalum3.rms.FlashCache;
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

    public static void setProgram(final Object program) {
        PlatformUtils.program = (Activity) program;
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
    public static void notifyDestroyed() {
        program.finish();
    }
    
    public static synchronized FlashCache getFlashCache() {
        if (flashDatabase == null) {
            flashDatabase = new AndroidCache();
        }
        
        return flashDatabase;
    }

    public static HttpConn getHttpPostConn(final String url) throws IOException {
        final HttpConn httpConn = new HttpConn(url);
        httpConn.con.setDoOutput(true);
        httpConn.con.setDoInput(true);
        httpConn.con.setRequestMethod("GET");

        return httpConn;
//        try {
//            final HttpClient client = new DefaultHttpClient();
//            final URI uri = new URI(url);
//            final HttpGet request = new HttpGet();
//            request.setURI(uri);
//            final HttpResponse response = client.execute(request);
//
//            return response.getEntity().getContent();
//        } catch (Exception ex) {
//            L.e("HttpGet error", url, ex);
//            throw new IOException("HttpGet error:" + url + ex);
//        }
    }

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

    /**
     * RMS Full or Database Full on the phone, need to clear space
     * 
     */
    public static final class FlashFullException extends Exception {
    }
}
