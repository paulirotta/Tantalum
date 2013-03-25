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
package org.tantalum.jme;

import android.graphics.BitmapFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;
import org.tantalum.PlatformUtils;
import org.tantalum.PlatformUtils.HttpConn;
import org.tantalum.PlatformAdapter;
import org.tantalum.storage.FlashCache;
import org.tantalum.storage.ImageTypeHandler;
import org.tantalum.util.L;
import org.tantalum.util.StringUtils;

/**
 * Utilities for support of each specific platform (JME, Android, ..)
 *
 * The platform-specific local copy of this class replaces this base version for
 * other platforms.
 *
 * @author phou
 */
public final class JMEPlatformAdapter implements PlatformAdapter {

    /**
     * There is only one Display per application
     *
     */
    public final Display display;
    private final L log;

    /**
     * Create a new instance of the JME platform port helper class.
     *
     * This is created for you when your MIDlet extends AndroidMIDlet and calls
     * the super() constructor.
     * 
     * @param routeDebugOutputToSerialPort - some platform implementation (Android)
     * may ignore this parameter because they have their own mechanism for
     * remote debugging.
     */
    public JMEPlatformAdapter(final boolean routeDebugOutputToSerialPort) {
        display = Display.getDisplay((MIDlet) PlatformUtils.getInstance().getProgram());
        log = new JMELog(routeDebugOutputToSerialPort);
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
    public void runOnUiThread(final Runnable action) {
        display.callSerially(action);
    }

    /**
     * The application calls this when all Workers have completed shutdown to
     * inform the phone that the program has closed itself.
     *
     * Do not call this directly, call Worker.shutdown() to initiate a close
     *
     */
    public void shutdownComplete() {
        ((MIDlet) PlatformUtils.getInstance().getProgram()).notifyDestroyed();
    }

    public ImageTypeHandler getImageTypeHandler() {
        return new JMEImageTypeHandler();
    }

    public FlashCache getFlashCache(final char priority) {
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
    public HttpConn getHttpConn(final String url, final Vector requestPropertyKeys, final Vector requestPropertyValues, final byte[] bytes, final String requestMethod) throws IOException {
        OutputStream out = null;

        try {
            final JMEHttpConn httpConn = new JMEHttpConn(url, requestPropertyKeys, requestPropertyValues);
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

    public L getLog() {
        return log;
    }
    
    public Object readImageFromJAR(final String jarPathAndFilename) throws IOException {
        final byte[] bytes = StringUtils.readBytesFromJAR(jarPathAndFilename);

        return Image.createImage(bytes, 0, bytes.length);
    }


    /**
     * A convenience class abstracting HTTP connection operations between
     * different platforms
     */
    public static final class JMEHttpConn implements PlatformUtils.HttpConn {

        final HttpConnection httpConnection;
        InputStream is = null;
        OutputStream os = null;

        /**
         * Create a platform-specific HTTP network connection, and set the HTTP
         * header with the corresponding key-value pairs
         *
         * @param url
         * @param requestPropertyKeys - a list of header keys
         * @param requestPropertyValues - a list of associated header values
         * @throws IOException
         */
        public JMEHttpConn(final String url, final Vector requestPropertyKeys, final Vector requestPropertyValues) throws IOException {
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
         * Get the OutputStream from the platform-specific HTTP connection
         *
         * @return
         * @throws IOException
         */
        public OutputStream getOutputStream() throws IOException {
            if (os == null) {
                os = httpConnection.openOutputStream();
            }

            return os;
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
			if (os != null) {
                os.close();
                os = null;
            }
            httpConnection.close();
        }
    }
}
