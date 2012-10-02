/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3;

import com.futurice.tantalum3.rms.FlashCache;
import com.futurice.tantalum3.rms.RMSCache;
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
    public static void setProgram(final MIDlet program) {
        PlatformUtils.program = program;
        PlatformUtils.display = Display.getDisplay(program);
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
        httpConn.con.setRequestMethod(HttpConnection.GET);

        return httpConn;
    }

    public static HttpConn getHttpPostConn(final String url, final byte[] bytes) throws IOException {
        OutputStream out = null;

        try {
            final HttpConn httpConn = new HttpConn(url);
            httpConn.con.setRequestMethod(HttpConnection.POST);
            out = httpConn.con.openOutputStream();
            out.write(bytes);

            return httpConn;
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static final class HttpConn {

        final HttpConnection con;
        InputStream is = null;

        public HttpConn(final String url) throws IOException {
            con = (HttpConnection) Connector.open(url);
        }

        public InputStream getInputStream() throws IOException {
            if (is == null) {
                is = con.openInputStream();
            }

            return is;
        }

        public long getLength() {
            return con.getLength();
        }

        public void close() throws IOException {
            if (is != null) {
                is.close();
                is = null;
            }
            con.close();
        }
    }

    /**
     * RMS Full or Database Full on the phone, need to clear space
     * 
     */
    public static final class FlashFullException extends Exception {
    }
}
