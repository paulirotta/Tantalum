/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package java.net;

import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author phou
 */
public abstract class HttpURLConnection extends URLConnection {

    public abstract void setDoInput(boolean b);

    public abstract void setDoOutput(boolean b);

    public abstract void setRequestMethod(String s);

    public abstract void setRequestProperty(String key, String value);

    public abstract int getResponseCode();

    public abstract String getHeaderFieldKey(int i);

    public abstract String getHeaderField(int i);

    public abstract String getHeaderField(String key);

    public abstract OutputStream getOutputStream();

    public abstract InputStream getInputStream();
    
    public abstract void disconnect();
}
