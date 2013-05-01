/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.blackberry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.HttpConnection;
import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.system.Alert;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.UiApplication;
import org.tantalum.PlatformAdapter;
import org.tantalum.PlatformUtils;
import org.tantalum.PlatformUtils.HttpConn;
import org.tantalum.jme.JMELog;
import org.tantalum.jme.RMSCache;
import org.tantalum.jme.RMSFastCache;
import org.tantalum.storage.FlashCache;
import org.tantalum.storage.FlashDatabaseException;
import org.tantalum.storage.ImageTypeHandler;
import org.tantalum.util.L;
import org.tantalum.util.StringUtils;

/**
 *
 * @author ADIKSONLINE
 */
public final class BBPlatformAdapter implements PlatformAdapter {

    private L log;
    private final UiApplication application;

    public BBPlatformAdapter() {
        application = UiApplication.getUiApplication();
    }

    public void init(int logMode) {
        log = new JMELog(logMode);
    }

    public void runOnUiThread(Runnable action) {
        application.invokeLater(action);
    }

    public void shutdownComplete() {
        System.exit(0);
    }

    public L getLog() {
        return log;
    }

    public ImageTypeHandler getImageTypeHandler() {
        return new BBImageTypeHandler();
    }

    public void vibrateAsync(int duration, Runnable timekeeperLambda) {
        if (Alert.isVibrateSupported()) {
            if (timekeeperLambda != null) {
                timekeeperLambda.run();
            }
            Alert.startVibrate(duration);
        }
    }

    public Object readImageFromJAR(String jarPathAndFilename) throws IOException {
        byte[] bytes = StringUtils.readBytesFromJAR(jarPathAndFilename);
        return Bitmap.createBitmapFromBytes(bytes, 0, bytes.length, 1);
    }

    public HttpConn getHttpConn(String url, Vector requestPropertyKeys, Vector requestPropertyValues, byte[] bytes, String requestMethod) throws IOException {
        OutputStream out = null;

        try {
            final BBHttpConn httpConn = new BBHttpConn(url, requestPropertyKeys, requestPropertyValues);
            httpConn.httpConnection.setRequestMethod(requestMethod);
            if (bytes != null) {
                out = httpConn.getOutputStream();
                out.write(bytes);
            }

            return httpConn;
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public FlashCache getFlashCache(char priority, int cacheType) throws FlashDatabaseException {
        switch (cacheType) {
            case PlatformUtils.PHONE_DATABASE_CACHE:
                try {
                    //                return new RMSCache(priority);
                    return new RMSFastCache(priority);
                } catch (Exception e) {
                    //#debug
                    L.e("Can not create flash cache", "" + priority, e);
                    throw new FlashDatabaseException("Can not create flash cache: " + e);
                }

            default:
                throw new IllegalArgumentException("Unsupported cache type " + cacheType + ": only PlatformAdapter.PHONE_DATABASE_CACHE is supported at this time");
        }
    }

    public static final class BBHttpConn implements PlatformUtils.HttpConn {

        final HttpConnection httpConnection;
        InputStream is = null;
        OutputStream os = null;

        public BBHttpConn(final String url, final Vector requestPropertyKeys, final Vector requestPropertyValues) throws IOException {
            ConnectionDescriptor cd = new ConnectionFactory().getConnection(url);
            if (cd != null) {
                httpConnection = (HttpConnection) cd.getConnection();
                for (int i = 0; i < requestPropertyKeys.size(); i++) {
                    httpConnection.setRequestProperty((String) requestPropertyKeys.elementAt(i), (String) requestPropertyValues.elementAt(i));
                }
            } else {
                throw new IOException("no connections were found");
            }
        }

        public InputStream getInputStream() throws IOException {
            if (is == null) {
                is = httpConnection.openInputStream();
            }
            return is;
        }

        public OutputStream getOutputStream() throws IOException {
            if (os == null) {
                os = httpConnection.openOutputStream();
            }
            return os;
        }

        public int getResponseCode() throws IOException {
            return httpConnection.getResponseCode();
        }

        public void getResponseHeaders(Hashtable headers) throws IOException {
            headers.clear();
            for (int i = 0; i < 10000; i++) {
                final String key = httpConnection.getHeaderFieldKey(i);
                if (key == null) {
                    break;
                }
                final String value = httpConnection.getHeaderField(i);
                final String[] values = (String[]) headers.get(key);
                final String[] newValues;
                
                if (values == null) {
                    newValues = new String[1];
                    newValues[0] = value;
                } else {
                    newValues = new String[values.length + 1];
                    System.arraycopy(values, 0, newValues, 0, values.length);
                    newValues[values.length] = value;
                }
                headers.put(key, newValues);
            }
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

        public long getMaxLengthSupportedAsBlockOperation() {
            return 1000000;
        }
    }
}
