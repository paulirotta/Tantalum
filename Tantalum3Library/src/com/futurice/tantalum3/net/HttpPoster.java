/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.futurice.tantalum3.net;

import com.futurice.tantalum3.Result;
import com.futurice.tantalum3.Workable;
import com.futurice.tantalum3.log.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 *
 * @author combes
 */
public class HttpPoster implements Workable {
    
    private final String url;
    private final Result result;
    private int retriesRemaining;
    private final String postMessage;

    
    public HttpPoster(final String url, final int retriesRemaining, final Result result, final String postMessage) {
        if (url == null || url.indexOf(':') <= 0) {
            throw new IllegalArgumentException("HttpPoster was passed bad URL: " + url);
        }
        if (postMessage == null) {
            throw new IllegalArgumentException("HttpPoster was null post message- meaningless post operation: " + url);
        }
        this.url = url;
        this.retriesRemaining = retriesRemaining;
        this.result = result;
        this.postMessage = postMessage;
    }        
    
    public String getUrl() {
        return url;
    }           
    
    public boolean work() {
        //#debug
        Log.l.log("HttpPoster start", url);
        ByteArrayOutputStream bos = null;
        HttpConnection httpConnection = null;
        InputStream inputStream = null; 
        OutputStream outputStream = null;
        int responseCode;
        byte[] bytes = null;
        boolean tryAgain = false;
        boolean success = false;

        try {
            httpConnection = (HttpConnection) Connector.open(url);
            httpConnection.setRequestMethod(HttpConnection.POST);            
            // Give the underlying native C drivers and hardware settling time
            // This improves network realiability on some phones
            outputStream = httpConnection.openDataOutputStream();
            outputStream.write(postMessage.getBytes());
            responseCode = httpConnection.getResponseCode();
            if(responseCode != HttpConnection.HTTP_OK) {
                throw new IOException("HTTP response code: " + responseCode);
            }
            
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
                    } else {
                        break;
                    }
                }
                bytes = bos.toByteArray();
                readBuffer = null;
            }

            //#debug
            Log.l.log("HttpPoster complete", bytes.length + " bytes, " + url);
            result.setResult(bytes);
            success = true;
            bytes = null;
        } catch (IllegalArgumentException e) {
            //#debug
            Log.l.log("HttpPoster has a problem", url, e);
        } catch (IOException e) {
            //#mdebug
            Log.l.log("Retries remaining", url + ", retries=" + retriesRemaining, e);
            //System.out.println(e.getMessage());
            //#enddebug
            if (retriesRemaining > 0) {
                retriesRemaining--;
                tryAgain = true;
            } else {
                //#debug
                Log.l.log("HttpPoster no more retries", url);
            }
        } catch (Exception e) {
            //#debug
            Log.l.log("HttpPoster has a problem", url, e);
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
            Log.l.log("End HttpPoster", url);
        }

        return success;
    }    
}
