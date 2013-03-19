/*
 Copyright © 2012 Paul Houghton and Futurice on behalf of the Tantalum Project.
 All rights reserved.

 Tantalum software shall be used to make the world a better place for everyone.

 This software is licensed for use under the Apache 2 open source software license,
 http://www.apache.org/licenses/LICENSE-2.0.html

 You are kindly requested to return your improvements to this library to the
 open source community at http://projects.developer.nokia.com/Tantalum

 The above copyright and license notice notice shall be included in all copies
 or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package org.tantalum.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.ConnectionNotFoundException;
import org.tantalum.PlatformUtils;
import org.tantalum.Task;
import org.tantalum.util.L;

/**
 * GET something from a URL on the Worker thread
 *
 * Implement Runnable if you want to automatically update the UI on the EDT
 * after the GET completes
 *
 * @author pahought
 */
public class HttpGetter extends Task {

    private static final Hashtable inFlightGets = new Hashtable();
    /**
     * The HTTP server has not yet been contacted, so no response code is yet
     * available
     */
    public static final int HTTP_OPERATION_PENDING = -1;
    private static final int HTTP_GET_RETRIES = 3;
    private static final int HTTP_RETRY_DELAY = 5000; // 5 seconds
    /**
     * The url, with optional additional lines to create a unique hash code is
     * may be needed for HTTP POST.
     */
    protected String key = null;
    /**
     * How many more times will we try to re-connect after a 5 second delay
     * before giving up. This aids in working with low quality networks and
     * normal HTTP connection setup errors even on a "good" mobile network.
     */
    protected int retriesRemaining = HTTP_GET_RETRIES;
    /**
     * Data to be sent to the server as part of an HTTP POST operation
     */
    protected byte[] postMessage = null;
    private int responseCode = HTTP_OPERATION_PENDING;
    private final Hashtable responseHeaders = new Hashtable();
    private Vector requestPropertyKeys = new Vector();
    private Vector requestPropertyValues = new Vector();
    private HttpGetter duplicateTaskWeShouldJoinInsteadOfReGetting = null;

    /**
     * Counter, estimated downloaded bytes during the session. 
     */
    private static int downstreamDataCount = 0;
    /**
     * Counter, estimated uploaded bytes during the session. 
     */
    private static int upstreamDataCount = 0;

    /**
     * Get the byte[] from the URL specified by the input argument when
     * exec(url) is called. This may be chained from a previous chain()ed
     * asynchronous task.
     */
    public HttpGetter() {
        super();
    }

    /**
     * Get the byte[] from a specified web service URL.
     *
     * Be default, the client will automatically retry 3 times if the web
     * service does not respond on the first attempt (happens frequently with
     * the mobile web...). You can disable this by calling
     * setRetriesRemaining(0).
     *
     * The "key" is a url. You can optionally attach additional information to
     * the key after \n (newline) to create a unique hashcode for cache
     * management purposes. This is sometimes needed for example with HTTP POST
     * where the url does not alone indicate a unique cachable entity- the post
     * parameters do.
     *
     * @param key
     */
    public HttpGetter(final String key) {
        super(key);

        duplicateTaskWeShouldJoinInsteadOfReGetting = getInFlightNetworkTasks();
    }

    /**
     * HTTP POST the associated message
     *
     * @param key
     * @param postMessage
     */
    protected HttpGetter(final String key, final byte[] postMessage) {
        this(key);

        this.postMessage = postMessage;
    }

    private HttpGetter getInFlightNetworkTasks() {
        if (key == null) {
            return null;
        }

        return (HttpGetter) HttpGetter.inFlightGets.get(key);
    }

    /**
     * Specify how many more times the HttpGetter should re-attempt HTTP GET if
     * there is a network error.
     *
     * This will automatically count down to zero at which point the Task shifts
     * to the Task.EXCEPTION state and onCanceled() will be called from the UI
     * Thread.
     *
     * @param retries
     * @return
     */
    public Task setRetriesRemaining(final int retries) {
        this.retriesRemaining = retries;

        return this;
    }

    /**
     * Find the HTTP server's response code, or HTTP_OPERATION_PENDING if the
     * HTTP server has not yet been contacted.
     *
     * @return
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * Get a Hashtable of all HTTP headers recieved from the server
     *
     * @return
     */
    public Hashtable getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Add an HTTP header to the request sent to the server
     *
     * @param key
     * @param value
     */
    public void setRequestProperty(final String key, final String value) {
        if (this.responseCode != HTTP_OPERATION_PENDING) {
            throw new IllegalStateException("Can not set request property to HTTP operation already executed  (" + key + ": " + value + ")");
        }

        this.requestPropertyKeys.addElement(key);
        this.requestPropertyValues.addElement(value);
    }

    /**
     * Get the contents of a URL and return that asynchronously as a AsyncResult
     *
     * Note that your web service should set the HTTP Header field
     * content_length as this makes the phone run slightly faster when we can
     * predict how many bytes to expect.
     *
     * @param url - The url we will HTTP GET from
     *
     * @return - a JSONModel of the data provided by the HTTP server
     */
    public Object exec(final Object url) {
        Object out = url;

        this.key = (String) url;
        if (key == null || key.indexOf(':') <= 0) {
            throw new IllegalArgumentException("HttpGetter was passed bad URL: " + key);
        }

        //#debug
        L.i(this.getClass().getName() + " start", key);
        if (duplicateTaskWeShouldJoinInsteadOfReGetting == null) {
            duplicateTaskWeShouldJoinInsteadOfReGetting = getInFlightNetworkTasks();
        }
        if (duplicateTaskWeShouldJoinInsteadOfReGetting != null) {
            //#debug
            L.i("Noticed a duplicate HttpGetter Task: join()ing that result rather than re-getting from web server", key);
            try {
                return duplicateTaskWeShouldJoinInsteadOfReGetting.get();
            } catch (Exception e) {
                //#debug
                L.e("Can not join duplicate HttpGetter Task", key, e);
            }
        }
        ByteArrayOutputStream bos = null;
        PlatformUtils.HttpConn httpConn = null;
        boolean tryAgain = false;
        boolean success = false;
        final String url2 = getUrl();

        addUpstreamDataCount(url2.length());
        
        try {
            if (this instanceof HttpPoster) {
                if (postMessage == null) {
                    throw new IllegalArgumentException("null HTTP POST- did you forget to call httpPoster.setMessage(byte[]) ? : " + key);
                }
                httpConn = PlatformUtils.getInstance().getHttpPostConn(url2, requestPropertyKeys, requestPropertyValues, postMessage);
                addUpstreamDataCount(postMessage.length);
            } else {
                httpConn = PlatformUtils.getInstance().getHttpGetConn(url2, requestPropertyKeys, requestPropertyValues);
            }

	    // Estimate data length of the sent headers
	    for (int i = 0; i < requestPropertyKeys.size(); i++) {
		addUpstreamDataCount(((String)requestPropertyKeys.elementAt(i)).length());
		addUpstreamDataCount(((String)requestPropertyValues.elementAt(i)).length());
	    }
	    
            final InputStream inputStream = httpConn.getInputStream();
            final long length = httpConn.getLength();
            responseCode = httpConn.getResponseCode();
            httpConn.getResponseHeaders(responseHeaders);
            
            // Response headers length estimation
            addDownstreamDataCount(responseHeaders.toString().length());

            if (length > 0 && length < 1000000) {
                //#debug
                L.i(this.getClass().getName() + " start fixed_length read", key + " content_length=" + length);
                int bytesRead = 0;
                final byte[] bytes = new byte[(int) length];
                while (bytesRead < bytes.length) {
                    final int br = inputStream.read(bytes, bytesRead, bytes.length - bytesRead);
                    if (br > 0) {
                        bytesRead += br;
                    } else {
                        //#debug
                        L.i(this.getClass().getName() + " recieved EOF before content_length exceeded", key + ", content_length=" + length + " bytes_read=" + bytesRead);
                        break;
                    }
                }
                out = bytes;
                //#debug
                L.i(this.getClass().getName() + " end fixed_length read", key + " content_length=" + length);
            } else {
                //#debug
                L.i(this.getClass().getName() + " start variable length read", key);
                bos = new ByteArrayOutputStream();
                final byte[] readBuffer = new byte[16384];

                while (true) {
                    final int bytesRead = inputStream.read(readBuffer);
                    if (bytesRead > 0) {
                        bos.write(readBuffer, 0, bytesRead);
                    } else {
                        break;
                    }
                }
                out = bos.toByteArray();
                //#debug
                L.i(this.getClass().getName() + " end variable length read (" + ((byte[]) out).length + " bytes)", key);
            }

            addDownstreamDataCount(((byte[]) out).length);
				   
            success = checkResponseCode(responseCode, responseHeaders);
        } catch (IllegalArgumentException e) {
            //#debug
            L.e(this.getClass().getName() + " HttpGetter has illegal argument", key, e);
            throw e;
        } catch (ConnectionNotFoundException e) {
            //#debug
            L.e(this.getClass().getName() + " HttpGetter can not open a connection right now", key, e);
            cancel(false, "HttpGetter received ConnectionNotFound: " + key);
        } catch (IOException e) {
            //#debug
            L.e(this.getClass().getName() + " retries remaining", key + ", retries=" + retriesRemaining, e);
            if (retriesRemaining > 0) {
                retriesRemaining--;
                tryAgain = true;
            } else {
                //#debug
                L.i(this.getClass().getName() + " no more retries", key);
            }
        } finally {
            try {
                httpConn.close();
            } catch (Exception e) {
                //#debug
                L.e("Closing Http InputStream error", key, e);
            }
            httpConn = null;
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {
            }
            bos = null;

            if (tryAgain) {
                try {
                    Thread.sleep(HTTP_RETRY_DELAY);
                } catch (InterruptedException ex) {
                }
                out = exec(url);
            } else if (!success) {
                //#debug
                L.i("HTTP GET FAILED, cancel() of task and any chained Tasks", this.toString());
                cancel(false, "HttpGetter failed response code and header check: " + this.toString());
            }
            HttpGetter.inFlightGets.remove(key);
            //#debug
            L.i("End " + this.getClass().getName(), key + " status=" + getStatus());

            return out;
        }
    }

    /**
     * Check headers and HTTP response code as needed for your web service to
     * see if this is a valid response. Override if needed.
     *
     * @param responseCode
     * @param headers
     * @return
     */
    protected boolean checkResponseCode(final int responseCode, final Hashtable headers) {
        boolean ok = true;

        if (responseCode >= 400) {
            //#debug
            L.i("Bad response code (" + responseCode + ")", "" + getValue());
            ok = false;
        }

        return ok;
    }

    /**
     * Strip additional (optional) lines from the key to create a URL. The key
     * may contain this data to create several unique hashcodes for cache
     * management purposes.
     *
     * @return
     */
    private String getUrl() {
        final int i = key.indexOf('\n');

        if (i < 0) {
            return key;
        }

        return key.substring(0, i);
    }
    
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        
        //#mdebug
        sb.append("HttpGetter: key=");
        sb.append(key);

        sb.append(" retriesRemaining=");
        sb.append(retriesRemaining);

        sb.append(" postMessageLength=");
        if (postMessage == null) {
            sb.append("<null>");
        } else {
            sb.append(postMessage.length);
        }
        
        sb.append(" responseCode=");
        sb.append(responseCode);
        
        sb.append("\nHTTP RESPONSE HEADERS");
        final Enumeration enu = responseHeaders.keys();
        while (enu.hasMoreElements()) {
            final String k = (String) enu.nextElement();
            sb.append("\n   ");
            sb.append(k);
            sb.append(": ");
            final String value = (String) responseHeaders.get(k);
            sb.append(value);
        }

        sb.append("\nHTTP REQUEST CUSTOM HEADERS");
        for (int i = 0; i < requestPropertyKeys.size(); i++) {
            final String key = (String) requestPropertyKeys.elementAt(i);
            sb.append("\n   ");
            sb.append(key);
            sb.append(": ");
            final String value = (String) requestPropertyValues.elementAt(i);
            sb.append(value);
        }

        sb.append("\n duplicateTaskWeShouldJoinInsteadOfReGetting=");
        sb.append(duplicateTaskWeShouldJoinInsteadOfReGetting);
        //#enddebug
        sb.append(super.toString());
        
        return sb.toString();
    }
    
    /**
     * Retrieves an estimated count of transfered bytes downstream.
     * The counter is valid during the application run.
     * @return byte count
     */
    public synchronized static int getDownstreamDataCount() {
	return downstreamDataCount;
    }
    
    /**
     * Retrieves an estimated count of transfered bytes upstream.
     * The counter is valid during the application run.
     * @return byte count
     */
    public synchronized static int getUpstreamDataCount() {
	return upstreamDataCount;
    }
    
    /**
     * Clears the downstream data counter.
     */
    public synchronized static void clearDownstreamDataCount() {
	downstreamDataCount = 0;
    }

    /**
     * Clears the upstream data counter. 
     */
    public synchronized static void clearUpstreamDataCount() {
	upstreamDataCount = 0;
    }
    
    /**
     * Accumulates the downstream data counter.
     * @param count 
     */
    private synchronized static void addDownstreamDataCount(final int count) {
	downstreamDataCount += count;
    }
       
    /**
     * Accumulates the upstream data counter.
     * @param count 
     */
    private synchronized static void addUpstreamDataCount(final int count) {
	upstreamDataCount += count;
    }

}
