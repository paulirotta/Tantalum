/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.net;

import com.futurice.tantalum3.AsyncResult;
import com.futurice.tantalum3.Task;
import com.futurice.tantalum3.log.L;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

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
    protected final AsyncResult task;
    protected int retriesRemaining;
    protected byte[] postMessage = null;
    protected String requestMethod = HttpConnection.GET;

    /**
     * Get the contents of a URL and return that asynchronously as a AsyncResult
     *
     * @param url - where on the Internet to synchronousGet the data
     * @param retriesRemaining - how many time to attempt connection
     * @param task - optional object notified on the EDT with the task
     */
    public HttpGetter(final String url, final int retriesRemaining, final AsyncResult task) {
        if (url == null || url.indexOf(':') <= 0) {
            throw new IllegalArgumentException("HttpGetter was passed bad URL: " + url);
        }
        if (task == null) {
            throw new IllegalArgumentException("HttpGetter was null result handler- meaningless get operation: " + url);
        }
        this.url = url;
        this.retriesRemaining = retriesRemaining;
        this.task = task;
    }

    public String getUrl() {
        return this.url;
    }

    public void exec() {
        //#debug
        L.i(this.getClass().getName() + " start", url);
        ByteArrayOutputStream bos = null;
        HttpConnection httpConnection = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        byte[] bytes = null;
        boolean tryAgain = false;
        boolean success = false;

        try {
            httpConnection = (HttpConnection) Connector.open(url);
            httpConnection.setRequestMethod(requestMethod);
            if (postMessage != null) {
                outputStream = httpConnection.openDataOutputStream();
                outputStream.write(postMessage);
            }
            inputStream = httpConnection.openInputStream();
            final long length = httpConnection.getLength();
            if (length > 0 && length < 1000000) {
                //#debug
                L.i(this.getClass().getName() + " start fixed_length read", url + " content_length=" + length);
                int bytesRead = 0;
                bytes = new byte[(int) length];
                while (bytesRead < bytes.length) {
                    final int br = inputStream.read(bytes, bytesRead, bytes.length - bytesRead);
                    if (br > 0) {
                        bytesRead += br;
                    } else {
                        //#debug
                        L.i(this.getClass().getName() + " recived EOF before content_length exceeded", url + ", content_length=" + length + " bytes_read=" + bytesRead);
                        break;
                    }
                }
            } else {
                //#debug
                L.i(this.getClass().getName() + " start variable length read", url);
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

            //#debug
            L.i(this.getClass().getName() + " complete", bytes.length + " bytes, " + url);
            task.set(bytes);
            success = true;
        } catch (IllegalArgumentException e) {
            //#debug
            L.e(this.getClass().getName() + " has a problem", url, e);
            bytes = null;
        } catch (IOException e) {
            //#debug
            L.e(this.getClass().getName() + " retries remaining", url + ", retries=" + retriesRemaining, e);
            bytes = null;
            if (retriesRemaining > 0) {
                retriesRemaining--;
                tryAgain = true;
            } else {
                //#debug
                L.i(this.getClass().getName() + " no more retries", url);
            }
        } catch (Exception e) {
            //#debug
            L.e(this.getClass().getName() + " has a problem", url, e);
            bytes = null;
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
            }
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {
            }
            try {
                httpConnection.close();
            } catch (Exception e) {
            }
            inputStream = null;
            outputStream = null;
            bos = null;
            httpConnection = null;

            if (tryAgain) {
                result = null;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                }
                exec();
            } else if (success) {
                onResult((byte[]) result);
            } else {
                cancel(false);
            }
            //#debug
            L.i("End " + this.getClass().getName(), url);
        }
    }
    
    protected void onResult(final byte[] bytes) {
        task.set(result);
    }
}
