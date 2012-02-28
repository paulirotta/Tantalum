/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2.net;

import com.futurice.tantalum2.Workable;
import com.futurice.tantalum2.Worker;
import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.rms.GetResult;
import com.futurice.tantalum2.rms.DataTypeHandler;
import com.futurice.tantalum2.rms.StaticCache;
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
    private final GetResult getResult;
    private final DataTypeHandler handler;
    private final StaticCache cache;
    private int retriesRemaining;

    /**
     * Get the contents of a URL and return that asynchronously as a GetResult
     * 
     * @param url - where on the Internet to get the data
     * @param retriesRemaining - how many time to attempt connection
     * @param getResult - optional object notified on the EDT with the result
     * @param handler - optional converter to change byte[] into a usable form
     * @param cache - optional cache which will store the result
     */
    public HttpGetter(String url, int retriesRemaining, GetResult getResult, DataTypeHandler handler, StaticCache cache) {
        this.url = url;
        this.retriesRemaining = retriesRemaining;
        this.getResult = getResult;
        this.handler = handler;
        this.cache = cache;
    }

    public String getUrl() {
        return this.url;
    }

    public boolean work() {
        Log.l.log("Start HttpGet", url);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        HttpConnection httpConnection = null;
        InputStream inputStream = null;
        boolean tryAgain = false;
        boolean success = false;

        try {
            final byte[] readBuffer = new byte[8192];
            
            httpConnection = (HttpConnection) Connector.open(url);
            httpConnection.setRequestMethod(HttpConnection.GET);
            inputStream = httpConnection.openInputStream();

            int bytesRead;
            while ((bytesRead = inputStream.read(readBuffer)) != -1) {
                bos.write(readBuffer, 0, bytesRead);
            }
            final Object converted;
            byte[] bytes = bos.toByteArray();
            bos.close();
            bos = null;
            if (handler != null) {
                converted = handler.convertToUseForm(bytes);
            } else {
                converted = bytes;
            }

            if (converted != null) {
                if (getResult != null) {
                    getResult.setResult(converted);
                    Worker.queueEDT(getResult);
                }
                if (cache != null) {
                    cache.put(url, converted);
                    cache.storeToRMS(url, bytes);
                }
                success = true;
                Log.l.log("HttpGetter complete", bytes.length + " bytes, " + url);
            } else {
                success = false;
            }
            bytes = null;
        } catch (IOException e) {
            Log.l.log("Retries remaining", url + ", retries=" + retriesRemaining, e);
            if (retriesRemaining > 0) {
                retriesRemaining--;
                tryAgain = true;
            } else {
                Log.l.log("HttpGetter no more retries", url);
            }
        } catch (Exception e) {
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
            }
            Log.l.log("End HttpGet", url);
        }

        return success;
    }
}
