package com.futurice.tantalum3.net;

import com.futurice.tantalum3.Task;
import com.futurice.tantalum3.log.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * GET something from a URL on the Worker thread
 *
 * Implement Runnable if you want to automatically update the UI on the EDT
 * after the GET completes
 *
 * @author pahought
 */
public class HttpGetter extends Task {

    private final String url;
    private final Task task;
    private int retriesRemaining;

    /**
     * Get the contents of a URL and return that asynchronously as a DEPRICATED_Result
     *
     * @param url - where on the Internet to synchronousGet the data
     * @param retriesRemaining - how many time to attempt connection
     * @param task - optional object notified on the EDT with the task
     */
    public HttpGetter(final String url, final int retriesRemaining,
            final Task result) {
        if (url == null || url.indexOf(':') <= 0) {
            throw new IllegalArgumentException(
                    "HttpGetter was passed bad URL: " + url);
        }
        if (result == null) {
            throw new IllegalArgumentException(
                    "HttpGetter was null result handler- meaningless get operation: "
                    + url);
        }
        this.url = url;
        this.retriesRemaining = retriesRemaining;
        this.task = result;
    }

    public String getUrl() {
        return this.url;
    }

    @Override
    public void exec() {
        Log.l.log("HttpGetter start", url);
        ByteArrayOutputStream bos = null;
        HttpClient httpConnection;
        HttpResponse response;
        InputStream inputStream = null;
        byte[] bytes = null;
        boolean tryAgain = false;
        boolean success = false;

        try {
            httpConnection = new DefaultHttpClient();
            URI uri = new URI(url);
            HttpGet request = new HttpGet();
            request.setURI(uri);

            response = httpConnection.execute(request);

            inputStream = response.getEntity().getContent();
            final long length = response.getEntity().getContentLength();
            if (length > 0 && length < 1000000) {
                Log.l.log("Start fixed_length read", url + " content_length="
                        + length);
                int bytesRead = 0;
                bytes = new byte[(int) length];
                while (bytesRead < bytes.length) {
                    final int br = inputStream.read(bytes, bytesRead,
                            bytes.length - bytesRead);
                    if (br > 0) {
                        bytesRead += br;
                    } else {
                        Log.l.log("Recived EOF before content_length exceeded",
                                url + ", content_length=" + length
                                + " bytes_read=" + bytesRead);
                        break;
                    }
                }
            } else {
                Log.l.log("Start variable length read", url);
                bos = new ByteArrayOutputStream();
                byte[] readBuffer = new byte[16384];
                while (true) {
                    final int bytesRead = inputStream.read(readBuffer);
                    if (bytesRead > 0) {
                        bos.write(readBuffer, 0, bytesRead);
                    } else {
                        break;
                    }
                }
                bytes = bos.toByteArray();
                result = bytes;
                readBuffer = null;
            }
            Log.l.log("HttpGetter complete", bytes.length + " bytes, " + url);
            task.set(bytes);
            success = true;
            bytes = null;
        } catch (IllegalArgumentException e) {
            Log.l.log("HttpGetter has a problem", url, e);
            bytes = null;
        } catch (IOException e) {
            Log.l.log("Retries remaining", url + ", retries="
                    + retriesRemaining, e);
            bytes = null;
            if (retriesRemaining > 0) {
                retriesRemaining--;
                tryAgain = true;
            } else {
                //#debug
                Log.l.log("HttpGetter no more retries", url);
            }
        } catch (Exception e) {
            Log.l.log("HttpGetter has a problem", url, e);
            bytes = null;
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
            }
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {
            }
            inputStream = null;
            bos = null;
            httpConnection = null;

            if (tryAgain) {
                result = null;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                }
                exec();
            } else if (!success) {
                cancel(false);
                task.cancel(false);
            }
            Log.l.log("End HttpGet", url);
        }
    }
}
