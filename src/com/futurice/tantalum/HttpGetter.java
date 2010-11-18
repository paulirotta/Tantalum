/*
 * Tantalum Mobile Toolset
 * https://projects.forum.nokia.com/Tantalum
 *
 * Special thanks to http://www.futurice.com for support of this project
 * Project lead: paul.houghton@futurice.com
 *
 * Copyright 2010 Paul Eugene Houghton
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.futurice.tantalum;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 *
 * @author Paul Eugene Houghton
 */
public class HttpGetter implements Runnable {
    public static final int HTTP_RETRIES = 3;

    final String url;
    final Result result;
    int retriesRemaining;

    public HttpGetter(String url, Result result, int retriesRemaining) {
        this.url = url;
        this.result = result;
        this.retriesRemaining = retriesRemaining;
    }

    public void run() {
        final byte[] readBuffer = new byte[8192];
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        HttpConnection httpConnection = null;
        boolean tryAgain = false;

        try {
            httpConnection = (HttpConnection) Connector.open(url);
            httpConnection.setRequestMethod(HttpConnection.GET);
            final InputStream inputStream = httpConnection.openInputStream();

            int bytesRead;
            while ((bytesRead = inputStream.read(readBuffer)) != -1) {
                bos.write(readBuffer, 0, bytesRead);
            }
            this.result.response(new String(bos.toByteArray()));
        } catch (IOException e) {
            Log.logNonfatalThrowable(e, url + "(retries = " + retriesRemaining);
            if (retriesRemaining > 0) {
                retriesRemaining--;
                tryAgain = true;
            } else {
                this.result.exception(new Exception("No more http retries: " + e));
            }
        } catch (Exception e) {
            Log.log("HttpGetter has a problem: " + e);
            e.printStackTrace();
            this.result.exception(e);
        } finally {
            try {
                if (httpConnection != null) {
                    httpConnection.close();
                }
            } catch (Exception e) {
                Log.logThrowable(e, "HttpGetter close error: " + url);
            }
            if (tryAgain) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                }
                this.run();
            }
        }
    }
}
