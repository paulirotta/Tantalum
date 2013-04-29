package java.net;

import java.io.InputStream;

public abstract class HttpURLConnection extends URLConnection {

    public void setRequestProperty(String field, String newValue) {
    }

    public InputStream getInputStream() {
        return null;
    }

    public int getResponseCode() {
        return 0;
    }

    public void setRequestMethod(String method) {
    }

    public abstract void disconnect();
}
