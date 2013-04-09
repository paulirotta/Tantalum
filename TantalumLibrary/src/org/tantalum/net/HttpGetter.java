/*
 Copyright (c) 2013, Paul Houghton and Futurice Oy
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
package org.tantalum.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.io.OutputStream;

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
    /*
     * HTTP Method constants
     */

    /**
     * HTTP GET is the default operation
     */
    public static final String HTTP_GET = "GET";
    /**
     * HTTP HEAD is not supported by JME
     */
    public static final String HTTP_HEAD = "HEAD";
    /**
     * HTTP POST is used if a byte[] to send is provided by the
     * <code>HttpPoster</code>
     */
    public static final String HTTP_POST = "POST";
    /**
     * HTTP PUT is not supported by JME
     */
    public static final String HTTP_PUT = "PUT";
    /**
     * HTTP DELETE is not supported by JME
     */
    public static final String HTTP_DELETE = "DELETE";
    /**
     * HTTP TRACE is not supported by JME
     */
    public static final String HTTP_TRACE = "TRACE";
    /**
     * HTTP CONNECT is not supported by JME
     */
    public static final String HTTP_CONNECT = "CONNECT";
    /*
     * HTTP Status constants
     */
    /**
     * HTTP response code value
     */
    public static final int HTTP_100_CONTINUE = 100;
    /**
     * HTTP response code value
     */
    public static final int HTTP_101_SWITCHING_PROTOCOLS = 101;
    /**
     * HTTP response code value
     */
    public static final int HTTP_102_PROCESSING = 102;
    /**
     * HTTP response code value
     */
    public static final int HTTP_200_OK = 200;
    /**
     * HTTP response code value
     */
    public static final int HTTP_201_CREATED = 201;
    /**
     * HTTP response code value
     */
    public static final int HTTP_202_ACCEPTED = 202;
    /**
     * HTTP response code value
     */
    public static final int HTTP_203_NON_AUTHORITATIVE_INFORMATION = 203;
    /**
     * HTTP response code value
     */
    public static final int HTTP_204_NO_CONTENT = 204;
    /**
     * HTTP response code value
     */
    public static final int HTTP_205_RESET_CONTENT = 205;
    /**
     * HTTP response code value
     */
    public static final int HTTP_206_PARTIAL_CONTENT = 206;
    /**
     * HTTP response code value
     */
    public static final int HTTP_207_MULTI_STATUS = 207;
    /**
     * HTTP response code value
     */
    public static final int HTTP_208_ALREADY_REPORTED = 208;
    /**
     * HTTP response code value
     */
    public static final int HTTP_250_LOW_ON_STORAGE_SPACE = 250;
    /**
     * HTTP response code value
     */
    public static final int HTTP_226_IM_USED = 226;
    /**
     * HTTP response code value
     */
    public static final int HTTP_300_MULTIPLE_CHOICES = 300;
    /**
     * HTTP response code value
     */
    public static final int HTTP_301_MOVED_PERMANENTLY = 301;
    /**
     * HTTP response code value
     */
    public static final int HTTP_302_FOUND = 302;
    /**
     * HTTP response code value
     */
    public static final int HTTP_303_SEE_OTHER = 303;
    /**
     * HTTP response code value
     */
    public static final int HTTP_304_NOT_MODIFIED = 304;
    /**
     * HTTP response code value
     */
    public static final int HTTP_305_USE_PROXY = 305;
    /**
     * HTTP response code value
     */
    public static final int HTTP_306_SWITCH_PROXY = 306;
    /**
     * HTTP response code value
     */
    public static final int HTTP_307_TEMPORARY_REDIRECT = 307;
    /**
     * HTTP response code value
     */
    public static final int HTTP_308_PERMANENT_REDIRECT = 308;
    /**
     * HTTP response code value
     */
    public static final int HTTP_400_BAD_REQUEST = 400;
    /**
     * HTTP response code value
     */
    public static final int HTTP_401_UNAUTHORIZED = 401;
    /**
     * HTTP response code value
     */
    public static final int HTTP_402_PAYMENT_REQUIRED = 402;
    /**
     * HTTP response code value
     */
    public static final int HTTP_403_FORBIDDEN = 403;
    /**
     * HTTP response code value
     */
    public static final int HTTP_404_NOT_FOUND = 404;
    /**
     * HTTP response code value
     */
    public static final int HTTP_405_METHOD_NOT_ALLOWED = 405;
    /**
     * HTTP response code value
     */
    public static final int HTTP_406_NOT_ACCEPTABLE = 406;
    /**
     * HTTP response code value
     */
    public static final int HTTP_407_PROXY_AUTHENTICATION_REQUIRED = 407;
    /**
     * HTTP response code value
     */
    public static final int HTTP_408_REQUEST_TIMEOUT = 408;
    /**
     * HTTP response code value
     */
    public static final int HTTP_409_CONFLICT = 409;
    /**
     * HTTP response code value
     */
    public static final int HTTP_410_GONE = 410;
    /**
     * HTTP response code value
     */
    public static final int HTTP_411_LENGTH_REQUIRED = 411;
    /**
     * HTTP response code value
     */
    public static final int HTTP_412_PRECONDITION_FAILED = 412;
    /**
     * HTTP response code value
     */
    public static final int HTTP_413_REQUEST_ENTITY_TOO_LARGE = 413;
    /**
     * HTTP response code value
     */
    public static final int HTTP_414_REQUEST_URI_TOO_LONG = 414;
    /**
     * HTTP response code value
     */
    public static final int HTTP_415_UNSUPPORTED_MEDIA_TYPE = 415;
    /**
     * HTTP response code value
     */
    public static final int HTTP_416_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    /**
     * HTTP response code value
     */
    /**
     * HTTP response code value
     */
    public static final int HTTP_417_EXPECTATION_FAILED = 417;
    /**
     * HTTP response code value
     */
    public static final int HTTP_418_IM_A_TEAPOT = 418;
    /**
     * HTTP response code value
     */
    public static final int HTTP_420_ENHANCE_YOUR_CALM = 420;
    /**
     * HTTP response code value
     */
    public static final int HTTP_422_UNPROCESSABLE_ENTITY = 422;
    /**
     * HTTP response code value
     */
    public static final int HTTP_423_LOCKED = 423;
    /**
     * HTTP response code value
     */
    public static final int HTTP_424_FAILED_DEPENDENCY = 424;
    /**
     * HTTP response code value
     */
    public static final int HTTP_424_METHOD_FAILURE = 424;
    /**
     * HTTP response code value
     */
    public static final int HTTP_425_UNORDERED_COLLECTION = 425;
    /**
     * HTTP response code value
     */
    public static final int HTTP_426_UPGRADE_REQUIRED = 426;
    /**
     * HTTP response code value
     */
    public static final int HTTP_428_PRECONDITION_REQUIRED = 428;
    /**
     * HTTP response code value
     */
    public static final int HTTP_429_TOO_MANY_REQUESTS = 429;
    /**
     * HTTP response code value
     */
    public static final int HTTP_431_REQUEST_HEADER_FIELDS_TOO_LARGE = 431;
    /**
     * HTTP response code value
     */
    public static final int HTTP_444_NO_RESPONSE = 444;
    /**
     * HTTP response code value
     */
    public static final int HTTP_449_RETRY_WITH = 449;
    /**
     * HTTP response code value
     */
    public static final int HTTP_450_BLOCKED_BY_WINDOWS_PARENTAL_CONTROLS = 450;
    /**
     * HTTP response code value
     */
    public static final int HTTP_451_PARAMETER_NOT_UNDERSTOOD = 451;
    /**
     * HTTP response code value
     */
    public static final int HTTP_451_UNAVAILABLE_FOR_LEGAL_REASONS = 451;
    /**
     * HTTP response code value
     */
    public static final int HTTP_451_REDIRECT = 451;
    /**
     * HTTP response code value
     */
    public static final int HTTP_452_CONFERENCE_NOT_FOUND = 452;
    /**
     * HTTP response code value
     */
    public static final int HTTP_453_NOT_ENOUGH_BANDWIDTH = 453;
    /**
     * HTTP response code value
     */
    public static final int HTTP_454_SESSION_NOT_FOUND = 454;
    /**
     * HTTP response code value
     */
    public static final int HTTP_455_METHOD_NOT_VALID_IN_THIS_STATE = 455;
    /**
     * HTTP response code value
     */
    public static final int HTTP_456_HEADER_FIELD_NOT_VALID_FOR_RESOURCE = 456;
    /**
     * HTTP response code value
     */
    public static final int HTTP_457_INVALID_RANGE = 457;
    /**
     * HTTP response code value
     */
    public static final int HTTP_458_PARAMETER_IS_READ_ONLY = 458;
    /**
     * HTTP response code value
     */
    public static final int HTTP_459_AGGREGATE_OPERATION_NOT_ALLOWED = 459;
    /**
     * HTTP response code value
     */
    public static final int HTTP_460_ONLY_AGGREGATE_OPERATION_ALLOWED = 460;
    /**
     * HTTP response code value
     */
    public static final int HTTP_461_UNSUPPORTED_TRANSPORT = 461;
    /**
     * HTTP response code value
     */
    public static final int HTTP_462_DESTINATION_UNREACHABLE = 462;
    /**
     * HTTP response code value
     */
    public static final int HTTP_494_REQUEST_HEADER_TOO_LARGE = 494;
    /**
     * HTTP response code value
     */
    public static final int HTTP_495_CERT_ERROR = 495;
    /**
     * HTTP response code value
     */
    public static final int HTTP_496_NO_CERT = 496;
    /**
     * HTTP response code value
     */
    public static final int HTTP_497_HTTP_TO_HTTPS = 497;
    /**
     * HTTP response code value
     */
    public static final int HTTP_499_CLIENT_CLOSED_REQUEST = 499;
    /**
     * HTTP response code value
     */
    public static final int HTTP_500_INTERNAL_SERVER_ERROR = 500;
    /**
     * HTTP response code value
     */
    public static final int HTTP_501_NOT_IMPLEMENTED = 501;
    /**
     * HTTP response code value
     */
    public static final int HTTP_502_BAD_GATEWAY = 502;
    /**
     * HTTP response code value
     */
    public static final int HTTP_503_SERVICE_UNAVAILABLE = 503;
    /**
     * HTTP response code value
     */
    public static final int HTTP_504_GATEWAY_TIMEOUT = 504;
    /**
     * HTTP response code value
     */
    public static final int HTTP_505_HTTP_VERSION_NOT_SUPPORTED = 505;
    /**
     * HTTP response code value
     */
    public static final int HTTP_506_VARIANT_ALSO_NEGOTIATES = 506;
    /**
     * HTTP response code value
     */
    public static final int HTTP_507_INSUFFICIENT_STORAGE = 507;
    /**
     * HTTP response code value
     */
    public static final int HTTP_508_LOOP_DETECTED = 508;
    /**
     * HTTP response code value
     */
    public static final int HTTP_509_BANDWIDTH_LIMIT_EXCEEDED = 509;
    /**
     * HTTP response code value
     */
    public static final int HTTP_510_NOT_EXTENDED = 510;
    /**
     * HTTP response code value
     */
    public static final int HTTP_511_NETWORK_AUTHENTICATION_REQUIRED = 511;
    /**
     * HTTP response code value
     */
    public static final int HTTP_550_PERMISSION_DENIED = 550;
    /**
     * HTTP response code value
     */
    public static final int HTTP_551_OPTION_NOT_SUPPORTED = 551;
    /**
     * HTTP response code value
     */
    public static final int HTTP_598_NETWORK_READ_TIMEOUT_ERROR = 598;
    /**
     * HTTP response code value
     */
    public static final int HTTP_599_NETWORK_CONNECT_TIMEOUT_ERROR = 599;
    /**
     * Prevent context switching to another HttpGetter during TCP buffer read
     *
     * You can also synchronized on this during a critical block in your UI
     * code. By doing so you are effectively freezing any temporary downstream
     * activity and thereby boosting the CPU cycles available for your activity.
     */
    public static final Object NET_MUTEX = new Object();
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
    /**
     * Counter, estimated downloaded bytes during the app session.
     *
     * Access only in static synchronized block
     */
    private static int downstreamDataCount = 0;
    /**
     * Counter, estimated uploaded bytes during the app session.
     *
     * Access only in static synchronized block
     */
    private static int upstreamDataCount = 0;
    private volatile StreamWriter streamWriter = null;
    private volatile StreamReader streamReader = null;

    /**
     * Set the StreamWriter which will provide data in the optional streaming
     * upload mode. Most HTTP activities are block-oriented in which case a
     * stream does not need to be set up.
     *
     * If you are tracking data usage, update addUpstreamDataCount() after or
     * while streaming
     *
     * @return
     */
    public StreamWriter getWriter() {
        return streamWriter;
    }

    /**
     * Set the StreamReader which will receive data in the optional streaming
     * download mode. Most HTTP activities are block-oriented in which case a
     * stream does not need to be set up.
     *
     * If you are tracking data usage, update addDownstreamDataCount() after or
     * while streaming
     *
     * @param writer
     */
    public void setWriter(StreamWriter writer) {
        this.streamWriter = writer;
    }

    /**
     * Get the current streaming download reader.
     *
     * Most HTTP use is block-oriented in which case the value is null.
     *
     * @return
     */
    public StreamReader getReader() {
        return streamReader;
    }

    /**
     * Get the current streaming upload reader.
     *
     * Most HTTP use is block-oriented in which case the value is null.
     *
     * @param reader
     */
    public void setReader(StreamReader reader) {
        this.streamReader = reader;
    }

    /**
     * Get the byte[] from the URL specified by the input argument when
     * exec(url) is called. This may be chained from a previous chain()ed
     * asynchronous task.
     */
    public HttpGetter() {
        super();
    }

    /**
     * Create a Task which which will, when fork()ed, get the byte[] from a
     * specified web service URL.
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

        if (!(url instanceof String) || url == null || ((String) url).indexOf(':') <= 0) {
            throw new IllegalArgumentException("HttpGetter was passed bad URL: " + url);
        }
        this.key = (String) url;

        //#debug
        L.i(this.getClass().getName() + " start", key);
        ByteArrayOutputStream bos = null;
        PlatformUtils.HttpConn httpConn = null;
        boolean tryAgain = false;
        boolean success = false;
        final String url2 = getUrl();

        addUpstreamDataCount(url2.length());

        try {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            if (this instanceof HttpPoster) {
                if (postMessage == null && streamWriter == null) {
                    throw new IllegalArgumentException("null HTTP POST- did you forget to call httpPoster.setMessage(byte[]) ? : " + key);
                }

                httpConn = PlatformUtils.getInstance().getHttpPostConn(url2, requestPropertyKeys, requestPropertyValues, postMessage);
                outputStream = httpConn.getOutputStream();
                final StreamWriter writer = this.streamWriter;
                if (writer != null) {
                    writer.writeReady(outputStream);
                    success = true;
                    out = null;
                }
                addUpstreamDataCount(postMessage.length);
            } else {
                httpConn = PlatformUtils.getInstance().getHttpGetConn(url2, requestPropertyKeys, requestPropertyValues);
                inputStream = httpConn.getInputStream();
                final StreamReader reader = streamReader;
                if (reader != null) {
                    reader.readReady(inputStream);
                    success = true;
                    out = null;
                }
            }

            // Estimate data length of the sent headers
            for (int i = 0; i < requestPropertyKeys.size(); i++) {
                addUpstreamDataCount(((String) requestPropertyKeys.elementAt(i)).length());
                addUpstreamDataCount(((String) requestPropertyValues.elementAt(i)).length());
            }

            final long length = httpConn.getLength();
            responseCode = httpConn.getResponseCode();
            httpConn.getResponseHeaders(responseHeaders);

            // Response headers length estimation
            addDownstreamDataCount(responseHeaders.toString().length());

            if (length == 0 || inputStream == null) {
                //#debug
                L.i(this.getClass().getName() + " exec", "No response. Stream is null, or length is 0");
            } else if (length > 0 && length < 1000000) {
                final byte[] bytes = new byte[(int) length];
                readBytesFixedLength(inputStream, bytes);
                out = bytes;
            } else {
                bos = new ByteArrayOutputStream(16384);
                readBytesVariableLength(inputStream, bos);
                out = bos.toByteArray();
            }

            addDownstreamDataCount(
                    ((byte[]) out).length);

            success = checkResponseCode(responseCode, responseHeaders);
        } catch (IllegalArgumentException e) {
            //#debug
            L.e(this.getClass().getName() + " HttpGetter has illegal argument", key, e);
            throw e;
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
            if (httpConn != null) {
                try {
                    httpConn.close();
                } catch (Exception e) {
                    //#debug
                    L.e("Closing Http InputStream error", key, e);
                } finally {
                    httpConn = null;
                }
            }
            try {
                if (bos != null) {
                    bos.close();
                    bos = null;
                }
            } catch (Exception e) {
            }

            if (tryAgain) {
                try {
                    Thread.sleep(HTTP_RETRY_DELAY);
                } catch (InterruptedException ex) {
                    cancel(false, "Interrupted HttpGetter while sleeping between retries: " + this);
                }
                out = exec(url);
            } else if (!success) {
                //#debug
                L.i("HTTP GET FAILED, cancel() of task and any chained Tasks", this.toString());
                cancel(false, "HttpGetter failed response code and header check: " + this.toString());
            }
            //#debug
            L.i("End " + this.getClass().getName(), key + " status=" + getStatus());

            return out;
        }
    }

    private void readBytesFixedLength(final InputStream inputStream, final byte[] bytes) throws IOException {
        int totalBytesRead = 0;

        //#debug
        L.i(this.getClass().getName() + " start Content-Length=" + bytes.length + " read", "" + this);
        try {
            while (totalBytesRead < bytes.length) {
                final int b = inputStream.read(); // Prime the read loop before mistakenly synchronizing on a net stream that has no data available yet
                if (b >= 0) {
                    bytes[totalBytesRead++] = (byte) b;
                    synchronized (NET_MUTEX) {
                        final int br = inputStream.read(bytes, totalBytesRead, bytes.length - totalBytesRead);
                        if (br >= 0) {
                            totalBytesRead += br;
                        } else {
                            prematureEOF(totalBytesRead, bytes.length);
                        }
                    }
                } else {
                    prematureEOF(totalBytesRead, bytes.length);
                }
            }
        } finally {
            //#debug
            L.i(this.getClass().getName() + " end Content-Length=" + bytes.length + " read", "" + this);
        }
    }

    private void readBytesVariableLength(final InputStream inputStream, final OutputStream bos) throws IOException {
        //#debug
        L.i(this.getClass().getName() + " start variable length read", key);
        try {
            final byte[] readBuffer = new byte[16384];
            while (true) {
                final int b = inputStream.read(); // Prime the read loop before mistakenly synchronizing on a net stream that has no data available yet
                if (b >= 0) {
                    bos.write(b);
                    synchronized (NET_MUTEX) {
                        final int bytesRead = inputStream.read(readBuffer);
                        if (bytesRead >= 0) {
                            bos.write(readBuffer, 0, bytesRead);
                        } else {
                            break;
                        }
                    }
                } else {
                    //#debug
                    L.i("Suspicious zero length result from a web service that does not set the Content-Length header: " + this.getClass().getName(), "" + this);
                }
            }
        } finally {
            //#debug
            L.i(this.getClass().getName() + " end variable length read", key);
        }
    }

    private void prematureEOF(final int bytesRead, final int length) throws IOException {
        final String s = this.getClass().getName() + " recieved EOF before content_length exceeded";
        //#debug
        L.i(s, key + ", content_length=" + length + " bytes_read=" + bytesRead);
        throw new IOException(s);
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

        if (responseCode < 400) {
            ok = true;
        } else if (responseCode < 500) {
            // We might be able to extract some useful information in case of a 400+ error code
            //#debug
            L.i("Bad response code (" + responseCode + ")", "");
            ok = false;
        } else {
            // 500+ error codes, which means that something went wrong on server side. 
            // Probably not recoverable, so should we throw an exception instead?
            //#debug
            L.i("Server side error. Bad response code (" + responseCode + ")", "");
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

    //#mdebug
    public String toString() {
        final StringBuffer sb = new StringBuffer();

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

        if (requestPropertyKeys.isEmpty()) {
            sb.append("\n(no HTTP request customer header)");
        } else {
            sb.append("\nHTTP REQUEST CUSTOM HEADERS");
            for (int i = 0; i < requestPropertyKeys.size(); i++) {
                final String key = (String) requestPropertyKeys.elementAt(i);
                sb.append("\n   ");
                sb.append(key);
                sb.append(": ");
                final String value = (String) requestPropertyValues.elementAt(i);
                sb.append(value);
            }
        }

        sb.append('\n');
        sb.append(super.toString());

        return sb.toString();
    }
    //#enddebug

    /**
     * Retrieves an estimated count of transfered bytes downstream. The counter
     * is valid during the application run.
     *
     * @return byte count
     */
    public synchronized static int getDownstreamDataCount() {
        return downstreamDataCount;
    }

    /**
     * Retrieves an estimated count of transfered bytes upstream. The counter is
     * valid during the application run.
     *
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
     *
     * @param byteCount
     */
    protected synchronized static void addDownstreamDataCount(final int byteCount) {
        downstreamDataCount += byteCount;
    }

    /**
     * Accumulates the upstream data counter.
     *
     * @param byteCount
     */
    protected synchronized static void addUpstreamDataCount(final int byteCount) {
        upstreamDataCount += byteCount;
    }
}
