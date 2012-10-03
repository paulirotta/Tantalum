/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum4;

import com.futurice.tantalum4.storage.FlashCache;
import com.futurice.tantalum4.storage.RMSCache;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

/**
 * Utilities for support of each specific platform (J2ME, Android, ..)
 * 
 * The platform-specific local copy of this class replaces this base version
 * for other platforms.
 * 
 * @author phou
 */
public class PlatformUtils {

    private static MIDlet program;
    private static Display display;
    private static FlashCache flashDatabase;

    /**
     * During initialization, the main program is set
     * 
     * @param program 
     */
    public static void setProgram(final Object program) {
        PlatformUtils.program = (MIDlet) program;
        PlatformUtils.display = Display.getDisplay(PlatformUtils.program);
    }
    
    public static MIDlet getProgram() {
        return PlatformUtils.program;
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
        display.callSerially(runnable);
    }

    /**
     * The application calls this when all Workers have completed shutdown to
     * inform the phone that the program has closed itself.
     *
     * Do not call this directly, call Worker.shutdown() to initiate a close
     *
     */
    public static void notifyDestroyed() {
        program.notifyDestroyed();
    }
    public static synchronized FlashCache getFlashCache() {
        if (flashDatabase == null) {
            flashDatabase = new RMSCache();
        }
        
        return flashDatabase;
    }

    public static HttpConn getHttpGetConn(final String url) throws IOException {
        final HttpConn httpConn = new HttpConn(url);
        httpConn.httpConnection.setRequestMethod(HttpConnection.GET);

        return httpConn;
    }

    public static HttpConn getHttpPostConn(final String url, final byte[] bytes) throws IOException {
        OutputStream out = null;

        try {
            final HttpConn httpConn = new HttpConn(url);
            httpConn.httpConnection.setRequestMethod(HttpConnection.POST);
            out = httpConn.httpConnection.openOutputStream();
            out.write(bytes);

            return httpConn;
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static final class HttpConn {

        final HttpConnection httpConnection;
        InputStream is = null;

        public HttpConn(final String url) throws IOException {
            httpConnection = (HttpConnection) Connector.open(url);
        }

        public InputStream getInputStream() throws IOException {
            if (is == null) {
                is = httpConnection.openInputStream();
            }

            return is;
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
