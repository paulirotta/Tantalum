/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2.net;

import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.Workable;
import com.futurice.tantalum2.Worker;
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
        Log.log("Start HttpGetter " + url);
        final byte[] readBuffer = new byte[8192];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        HttpConnection httpConnection = null;
        InputStream inputStream = null;
        boolean tryAgain = false;
        boolean success = false;

        try {
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
                getResult.setResult(converted);
                Worker.queueEDT(getResult);
                if (cache != null) {
                    cache.put(url, converted);
                    cache.storeToRMS(url, bytes);
                }
                success = true;
                Log.log("HttpGetter complete (" + bytes.length + ") bytes, " + url);
            } else {
                success = false;
            }
            bytes = null;
        } catch (IOException e) {
            Log.logNonfatalThrowable(e, url + "(retries = " + retriesRemaining);
            if (retriesRemaining > 0) {
                retriesRemaining--;
                tryAgain = true;
            } else {
                Log.log("HttpGetter no more retries: " + url);
            }
        } catch (Exception e) {
            Log.logThrowable(e, "HttpGetter has a problem: " + url);
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                Log.logThrowable(e, "HttpGetter close error: " + url);
            }
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {
                Log.logThrowable(e, "HttpGetter close error: " + url);
            }
            try {
                httpConnection.close();
            } catch (Exception e) {
                Log.logThrowable(e, "HttpGetter close error: " + url);
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
        }

        return success;
    }
}
