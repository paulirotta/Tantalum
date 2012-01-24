/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2.net;

import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.Workable;
import com.futurice.tantalum2.Worker;
import com.futurice.tantalum2.rms.CacheGetResult;
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
    private int retriesRemaining;
    private CacheGetResult cacheGetResult;
    private DataTypeHandler handler;
    private StaticCache cache;

    public HttpGetter(String url, int retriesRemaining, CacheGetResult cacheGetResult, DataTypeHandler handler, StaticCache cache) {
        this.url = url;
        this.retriesRemaining = retriesRemaining;
        this.cacheGetResult = cacheGetResult;
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
            final byte[] bytes = bos.toByteArray();
            final Object converted = handler.convertToUseForm(bytes);

            if (converted != null) {
                cacheGetResult.setResult(converted);
                Worker.queueEDT(cacheGetResult);
//                Worker.queueEDT(new Runnable() {

//                    public void run() {
                cache.put(url, converted);
                cache.storeToRMS(url, bytes);
//                    }
//                });
                success = true;
                Log.log("HttpGetter complete (" + bytes.length + ") bytes, " + url);
            } else {
                success = false;
            }
        } catch (IOException e) {
            Log.logNonfatalThrowable(e, url + "(retries = " + retriesRemaining);
            if (retriesRemaining > 0) {
                retriesRemaining--;
                tryAgain = true;
            } else {
                //this.exception(new Exception("No more http retries: " + e));
            }
        } catch (Exception e) {
            Log.logThrowable(e, "HttpGetter has a problem: " + url);
            //this.exception(e);
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                Log.logThrowable(e, "HttpGetter close error: " + url);
            }
            try {
                bos.close();
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
