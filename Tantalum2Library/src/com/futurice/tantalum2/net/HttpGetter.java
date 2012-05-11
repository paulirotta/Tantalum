/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2.net;

import com.futurice.tantalum2.Result;
import com.futurice.tantalum2.Workable;
import com.futurice.tantalum2.log.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class HttpGetter implements Workable {

    private final String url;
    private final Result result;
    private int retriesRemaining;

    /**
     * Get the contents of a URL and return that asynchronously as a Result
     *
     * @param url - where on the Internet to synchronousGet the data
     * @param retriesRemaining - how many time to attempt connection
     * @param result - optional object notified on the EDT with the result
     */
    public HttpGetter(final String url, final int retriesRemaining, final Result result) {
        if (url == null || url.indexOf(':') <= 0) {
            throw new IllegalArgumentException("HttpGetter was passed bad URL: " + url);
        }
        if (result == null) {
            throw new IllegalArgumentException("HttpGetter was null result handler- meaningless get operation: " + url);
        }
        this.url = url;
        this.retriesRemaining = retriesRemaining;
        this.result = result;
    }

    public String getUrl() {
        return this.url;
    }

    public boolean work() {
        //#debug
        Log.l.log("HttpGetter start", url);
        ByteArrayOutputStream bos = null;
        HttpConnection httpConnection = null;
        InputStream inputStream = null;
        byte[] bytes = null;
        boolean tryAgain = false;
        boolean success = false;

        try {
            httpConnection = (HttpConnection) Connector.open(url);
            httpConnection.setRequestMethod(HttpConnection.GET);
            // Give the underlying native C drivers and hardware settling time
            // This improves network realiability on some phones
//            Thread.sleep(200);
            inputStream = httpConnection.openInputStream();
            final long length = httpConnection.getLength();
            if (length > 0 && length < 1000000) {
                //#debug
                Log.l.log("Start fixed_length read", url + " content_length=" + length);
                int bytesRead = 0;
                bytes = new byte[(int) length];
                while (bytesRead < bytes.length) {
                    final int br = inputStream.read(bytes, bytesRead, bytes.length - bytesRead);
                    if (br > 0) {
                        bytesRead += br;
//                    } else if (br == 0) {
//                        Thread.sleep(50);
                    } else {
                        //#debug
                        Log.l.log("Recived EOF before content_length exceeded", url + ", content_length=" + length + " bytes_read=" + bytesRead);
                        break;
                    }
                }
            } else {
                //#debug
                Log.l.log("Start variable length read", url);
                bos = new ByteArrayOutputStream();
                byte[] readBuffer = new byte[16384];
                while (true) {
                    final int bytesRead = inputStream.read(readBuffer);
                    if (bytesRead > 0) {
                        bos.write(readBuffer, 0, bytesRead);
//                    } else if (bytesRead == 0) {
//                        Thread.sleep(50);
                    } else {
                        break;
                    }
                }
                bytes = bos.toByteArray();
                readBuffer = null;
            }

//            while ((bytesRead = inputStream.read(readBuffer)) != -1) {
//                bos.write(readBuffer, 0, bytesRead);
//            }
            if (bytes != null) {
                //#debug
                Log.l.log("HttpGetter complete", bytes.length + " bytes, " + url);
                result.setResult(bytes);
                success = true;
                bytes = null;
            } else {
                //#debug
                Log.l.log("HttpGetter null response", url);
            }
        } catch (IllegalArgumentException e) {
            //#debug
            Log.l.log("HttpGetter has a problem", url, e);
        } catch (IOException e) {
            //#debug
            Log.l.log("Retries remaining", url + ", retries=" + retriesRemaining, e);
            if (retriesRemaining > 0) {
                retriesRemaining--;
                tryAgain = true;
            } else {
                //#debug
                Log.l.log("HttpGetter no more retries", url);
            }
        } catch (Exception e) {
            //#debug
            Log.l.log("HttpGetter has a problem", url, e);
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
            try {
                httpConnection.close();
            } catch (Exception e) {
            }
            inputStream = null;
            bos = null;
            httpConnection = null;

            if (tryAgain) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                }

                return this.work();
            } else if (!success) {
                result.noResult();
            }
            //#debug
            Log.l.log("End HttpGet", url);
        }

        return success;
    }
}
