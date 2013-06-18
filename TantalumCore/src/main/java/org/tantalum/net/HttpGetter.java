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
import java.util.Hashtable;
import java.util.Vector;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.DigestException;
import java.util.TimerTask;

import org.tantalum.PlatformUtils;
import org.tantalum.Task;
import org.tantalum.util.CryptoUtils;
import org.tantalum.util.L;
import org.tantalum.util.RollingAverage;
import org.tantalum.util.WeakHashCache;
import org.tantalum.util.WeakReferenceListenerHandler;

/**
 * GET something from a URL on the Worker thread
 *
 * This Task will, when fork()ed, get the byte[] from a specified web service
 * URL.
 *
 * Be default, the client will automatically retry 3 times if the web service
 * does not respond on the first attempt (happens frequently with the mobile
 * web...). You can disable this by calling setRetriesRemaining(0).
 *
 * The input "key" is a url with optional additional lines of text which are
 * ignored from the URL but may be useful for distinguishing multiple HTTP
 * operations to the same URL (HttpPoster). You can optionally attach additional
 * information to the key after \n (newline) to create a unique hashcode for
 * cache management purposes. This is sometimes needed for example with HTTP
 * POST where the url does not alone indicate a unique cachable entity- the post
 * parameters do.
 *
 * @author pahought
 */
public class HttpGetter extends Task {

    private static final int READ_BUFFER_LENGTH = 8192; //8k read buffer if no Content-Length header from server
    private static final int OUTPUT_BUFFER_INITIAL_LENGTH = 8192; //8k read buffer if no Content-Length header from server
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
     * The HTTP server has not yet been contacted, so no response code is yet
     * available
     */
    public static final int HTTP_OPERATION_PENDING = -1;
    private static final int HTTP_GET_RETRIES = 1;
    private static final int HTTP_RETRY_DELAY = 5000; // 5 seconds
    /**
     * Connections slower than this drop into single file load with header
     * pre-wind to increase interface responsiveness to each HTTP action as seen
     * alone and decrease phone thread context switching.
     *
     * Note that due to measurement error this is not a real baud rate, but the
     * rate at which data can be pulled from the network buffers. If phone
     * network buffering associated with the first packets were removed from the
     * measure, the actual baud rate over the air would be slower than this.
     */
    public static final float THRESHOLD_BAUD = 128000f;
    /**
     * The rolling average of how long it takes the server to respond with the
     * first response body byte to an HTTP request. This will shift up and down
     * slowly based on the servers and data network you use. It is in some cases
     * useful as a performance tuning parameter.
     */
    public static final RollingAverage averageResponseDelayMillis = new RollingAverage(10, 700.0f);
    private static final WeakReferenceListenerHandler netActivityListenerDelegate = new WeakReferenceListenerHandler(NetActivityListener.class);
    /**
     * bits per second realized by a each connection. When multiple connections
     * are reading simultaneously this will be lower than the total bits per
     * second of the phone's downlink. It is used in conjunction with
     * THRESHOLD_BAUD to determine if we should switch to serial reading with
     * header pre-wind to help UX by decreasing the user's perceived response
     * time per HTTP GET.
     *
     * We start the app with the assumption we are on a slow connection by
     * quickly adapt if the data arrives quickly. Note that since the
     * measurement is continuous and realized we do not make assumptions based
     * on whether the user or phone think they are on a fast WIFI connection or
     * not. Changing network connections or network connection real speeds
     * should result in a change of mode within a few HTTP operations if
     * appropriate.
     */
    public static final RollingAverage averageBaud = new RollingAverage(10, THRESHOLD_BAUD / 2);
    /**
     * At what time earliest can the next HTTP request to a server can begin
     * when in slow connection mode
     */
    private static volatile long nextHeaderStartTime = 0;
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
    // Always access in a synchronized(HttpGetter.this) block
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
    // Always access in a synchronized(HttpGetter.this) block
    private int responseCode = HTTP_OPERATION_PENDING;
    private volatile long startTime = 0;

    static {
        HttpGetter.averageBaud.setLowerBound(HttpGetter.THRESHOLD_BAUD / 10);
        HttpGetter.averageResponseDelayMillis.setUpperBound(5000.0f);
    }

    /**
     * Create a Task.NORMAL_PRIORITY getter
     *
     */
    public HttpGetter() {
        this(Task.NORMAL_PRIORITY);
    }

    /**
     * Get the byte[] from the URL specified by the input argument when
     * exec(url) is called. This may be chained from a previous chain()ed
     * asynchronous task.
     *
     * @param priority
     */
    public HttpGetter(final int priority) {
        super(priority);

        setShutdownBehaviour(Task.DEQUEUE_OR_CANCEL_ON_SHUTDOWN);
    }

    /**
     * Create a Task for the specified URL.
     *
     * @param priority
     * @param url
     */
    public HttpGetter(final int priority, final String url) {
        this(priority);

        if (url == null) {
            throw new IllegalArgumentException("Attempt to create an HttpGetter with null URL. Perhaps you want to use the alternate new HttpGetter() constructor and let the previous Task in a chain set the URL.");
        }
        set(url);
    }

    /**
     * Create a Task.NORMAL_PRIORITY getter
     *
     * @param url
     */
    public HttpGetter(final String url) {
        this(Task.NORMAL_PRIORITY, url);
    }

    /**
     * On a 2G-speed network, this method will block the calling thread up to
     * several seconds until the next HTTP operation can begin.
     *
     * On a fast network, this will not delay the calling thread.
     *
     * The HttpGetter will call this for you at the start of Task exec() to
     * reduce network contention. You may also want to call as part of your loop
     * that creates multiple HTTP GET operations such as fetching images. You
     * can in this way delay the decision to actually fetch a resource and not
     * do so if the data is not needed several seconds later. This is also
     * useful to reduce the number of worker threads being held in a header wait
     * state by HttpGetter Tasks.
     *
     * @throws InterruptedException
     */
    public static void staggerHeaderStartTime() throws InterruptedException {
        long t = System.currentTimeMillis();
        long t2;
        boolean staggerStartMode;
        while ((staggerStartMode = (HttpGetter.averageBaud.value() < THRESHOLD_BAUD)) && (t2 = nextHeaderStartTime) > t) {
            //#debug
            L.i("Header get stagger delay", (t2 - t) + "ms");
            Thread.sleep(t2 - t);
            t = System.currentTimeMillis();
        }
        if (staggerStartMode) {
            nextHeaderStartTime = t + (((int) HttpGetter.averageResponseDelayMillis.value()) * 7) / 8;
        }
    }

    /**
     * Get the time at which the HTTP network connection started
     *
     * @return
     */
    public long getStartTime() {
        return startTime;
    }

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
    public void setWriter(final StreamWriter writer) {
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
    public void setReader(final StreamReader reader) {
        this.streamReader = reader;
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
    public synchronized int getResponseCode() {
        return responseCode;
    }

    /**
     * Get a Hashtable of all HTTP headers recieved from the server
     *
     * @return
     */
    public synchronized Hashtable getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Add an HTTP header to the request sent to the server
     *
     * @param key
     * @param value
     */
    public synchronized void setRequestProperty(final String key, final String value) {
        if (responseCode != HTTP_OPERATION_PENDING) {
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
     * @param in
     * @return
     */
    public Object exec(final Object in) throws InterruptedException {
        staggerHeaderStartTime();
        byte[] out = null;

        startTime = System.currentTimeMillis();
        if (!(in instanceof String) || ((String) in).indexOf(':') <= 0) {
            final String s = "HTTP operation was passed a bad url=" + in + ". Check calling method or previous chained task: " + this;
            //#debug
            L.e("HttpGetter with non-String input", s, new IllegalArgumentException());
            cancel(false, s);
            return out;
        }

        final String url = keyIncludingPostDataHashtoUrl((String) in);
        final Integer netActivityKey = new Integer(url.hashCode());
        HttpGetter.networkActivity(netActivityKey); // Notify listeners, net is in use

        //#debug
        L.i(this, "Start", url);
        ByteArrayOutputStream bos = null;
        PlatformUtils.HttpConn httpConn = null;
        boolean tryAgain = false;
        boolean success = false;

        addUpstreamDataCount(url.length());

        try {
            final OutputStream outputStream;
            if (this instanceof HttpPoster) {
                if (postMessage == null && streamWriter == null) {
                    throw new IllegalArgumentException("null HTTP POST- did you forget to call httpPoster.setMessage(byte[]) ? : " + url);
                }

                httpConn = PlatformUtils.getInstance().getHttpPostConn(url, requestPropertyKeys, requestPropertyValues, postMessage);
                outputStream = httpConn.getOutputStream();
                final StreamWriter writer = this.streamWriter;
                if (writer != null) {
                    writer.writeReady(outputStream);
                    success = true;
                }
                addUpstreamDataCount(postMessage.length);
            } else {
                httpConn = PlatformUtils.getInstance().getHttpGetConn(url, requestPropertyKeys, requestPropertyValues);
            }
            final InputStream inputStream = httpConn.getInputStream();
            final StreamReader reader = streamReader;
            if (reader != null) {
                reader.readReady(inputStream);
                success = true;
            }

            // Estimate data length of the sent headers
            for (int i = 0; i < requestPropertyKeys.size(); i++) {
                addUpstreamDataCount(((String) requestPropertyKeys.elementAt(i)).length());
                addUpstreamDataCount(((String) requestPropertyValues.elementAt(i)).length());
            }

            final int length = (int) httpConn.getLength();
            final int downstreamDataHeaderLength;
            synchronized (this) {
                responseCode = httpConn.getResponseCode();
                httpConn.getResponseHeaders(responseHeaders);
                downstreamDataHeaderLength = PlatformUtils.responseHeadersToString(responseHeaders).length();
            }

            // Response headers length estimation
            addDownstreamDataCount(downstreamDataHeaderLength);

            long firstByteTime = Long.MAX_VALUE;
            if (length == 0) {
                //#debug
                L.i(this, "Exec", "No response. Stream is null, or length is 0");
            } else if (length > httpConn.getMaxLengthSupportedAsBlockOperation()) {
                cancel(false, "Http server sent Content-Length > " + httpConn.getMaxLengthSupportedAsBlockOperation() + " which might cause out-of-memory on this platform");
            } else if (length > 0 && HttpGetter.netActivityListenerDelegate.isEmpty()) {
                final byte[] bytes = new byte[length];
                firstByteTime = readBytesFixedLength(url, inputStream, bytes);
                out = bytes;
            } else {
                bos = new ByteArrayOutputStream(OUTPUT_BUFFER_INITIAL_LENGTH);
                firstByteTime = readBytesVariableLength(inputStream, bos, netActivityKey);
                out = bos.toByteArray();
            }
            if (firstByteTime != Long.MAX_VALUE) {
                final long responseTime = firstByteTime - startTime;
                HttpGetter.averageResponseDelayMillis.update(responseTime);
                //#debug
                L.i(this, "Average HTTP header response time", HttpGetter.averageResponseDelayMillis.value() + " current=" + responseTime);
            }
            final long lastByteTime = System.currentTimeMillis();
            final float baud;
            int dataLength = 0;
            if (out != null) {
                dataLength = out.length;
            }
            if (dataLength > 0 && lastByteTime > firstByteTime) {
                baud = (dataLength * 8 * 1000) / ((int) (lastByteTime - firstByteTime));
            } else {
                baud = THRESHOLD_BAUD * 2;
            }
            HttpGetter.averageBaud.update(baud);
            //#debug
            L.i(this, "Average HTTP body read baud", HttpGetter.averageBaud.value() + " current=" + baud);

            if (out != null) {
                addDownstreamDataCount(dataLength);
                //#debug
                L.i(this, "End read", "url=" + url + " bytes=" + ((byte[]) out).length);
            }
            synchronized (this) {
                success = checkResponseCode(url, responseCode, responseHeaders);
            }
            //#debug
            L.i(this, "Response", "HTTP response code indicates success=" + success);
        } catch (IllegalArgumentException e) {
            //#debug
            L.e(this, "HttpGetter has illegal argument", url, e);
            throw e;
        } catch (IOException e) {
            //#debug
            L.e(this, "Retries remaining", url + ", retries=" + retriesRemaining, e);
            if (retriesRemaining > 0) {
                retriesRemaining--;
                tryAgain = true;
            } else {
                //#debug
                L.i(this, "No more retries", url);
                cancel(false, "No more retries");
            }
        } finally {
            if (httpConn != null) {
                try {
                    httpConn.close();
                } catch (Exception e) {
                    //#debug
                    L.e(this, "Closing Http InputStream error", url, e);
                } finally {
                    httpConn = null;
                }
            }
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {
                //#debug
                L.e(this, "HttpGetter byteArrayOutputStream close error", url, e);
            } finally {
                bos = null;
            }

            if (tryAgain) {
                try {
                    Thread.sleep(HTTP_RETRY_DELAY);
                } catch (InterruptedException ex) {
                    cancel(false, "Interrupted HttpGetter while sleeping between retries: " + this);
                }
                out = (byte[]) exec(url);
            } else if (!success) {
                //#debug
                L.i("HTTP GET FAILED, cancel() of task and any chained Tasks", (this != null ? this.toString() : "null"));
                cancel(false, "HttpGetter failed response code and header check: " + this);
            }
            //#debug
            L.i(this, "End", url + " status=" + getStatus() + " out=" + out);
            HttpGetter.endNetworkActivity(netActivityKey); // Notify listeners, net is in use
        }

        if (!success) {
            return null;
        }

        return out;
    }

    /**
     * Read an exact number of bytes specified in the header Content-Length
     * field
     *
     * @param url
     * @param inputStream
     * @param bytes
     * @return time of first byte received
     * @throws IOException
     */
    private long readBytesFixedLength(final String url, final InputStream inputStream, final byte[] bytes) throws IOException {
        long firstByteReceivedTime = Long.MAX_VALUE;

        if (bytes.length != 0) {
            int totalBytesRead = 0;

            final int b = inputStream.read(); // Prime the read loop before mistakenly synchronizing on a net stream that has no data available yet
            if (b >= 0) {
                bytes[totalBytesRead++] = (byte) b;
            } else {
                prematureEOF(url, totalBytesRead, bytes.length);
            }
            firstByteReceivedTime = System.currentTimeMillis();
            while (totalBytesRead < bytes.length) {
                final int br = inputStream.read(bytes, totalBytesRead, bytes.length - totalBytesRead);
                if (br >= 0) {
                    totalBytesRead += br;
                    if (totalBytesRead < bytes.length) {
                        Thread.currentThread().yield();
                    }
                } else {
                    prematureEOF(url, totalBytesRead, bytes.length);
                }
            }
        }

        return firstByteReceivedTime;
    }

    private void prematureEOF(final String url, final int bytesRead, final int length) throws IOException {
        //#debug
        L.i(this, "EOF before Content-Length sent by server", url + ", Content-Length=" + length + " bytesRead=" + bytesRead);
        throw new IOException(getClassName() + " recieved EOF before content_length exceeded");
    }

    /**
     * Read an unknown length field because the server did not specify how long
     * the result is
     *
     * @param inputStream
     * @param bos
     * @return time of first byte received
     * @throws IOException
     */
    private long readBytesVariableLength(final InputStream inputStream, final OutputStream bos, final Integer netActivityKey) throws IOException {
        final byte[] readBuffer = new byte[READ_BUFFER_LENGTH];

        final int b = inputStream.read(); // Prime the read loop before mistakenly synchronizing on a net stream that has no data available yet
        if (b < 0) {
            return Long.MAX_VALUE;
        }
        bos.write(b);
        final long firstByteReceivedTime = System.currentTimeMillis();
        HttpGetter.networkActivity(netActivityKey);
        while (true) {
            final int bytesRead = inputStream.read(readBuffer);
            HttpGetter.networkActivity(netActivityKey);
            if (bytesRead < 0) {
                break;
            }
            bos.write(readBuffer, 0, bytesRead);
        }

        return firstByteReceivedTime;
    }

    /**
     * Check headers and HTTP response code as needed for your web service to
     * see if this is a valid response. Override if needed.
     *
     * @param url
     * @param responseCode
     * @param headers
     * @return
     * @throws IOException
     */
    protected boolean checkResponseCode(final String url, final int responseCode, final Hashtable headers) throws IOException {
        if (responseCode < 300) {
            return true;
        } else if (responseCode < 500) {
            // We might be able to extract some useful information in case of a 400+ error code
            //#debug
            L.i("Bad response code (" + responseCode + ")", "url=" + url);
            return false;
        }
        /*
         * 500+ error codes, which means that something went wrong on server side. 
         * Probably not recoverable, so should we throw an exception instead?
         */
        //#debug
        L.i(this, "Server error. Unrecoverable HTTP response code (" + responseCode + ")", "url=" + url);
        throw new IOException("Server error. Unrecoverable HTTP response code (" + responseCode + ") url=" + url);
    }

    /**
     * Strip additional (optional) lines from the key to create a URL. The key
     * may contain this data to create several unique hashcodes for cache
     * management purposes.
     *
     * @return
     */
    private String keyIncludingPostDataHashtoUrl(final String key) {
        final int i = key.indexOf('\n');

        if (i < 0) {
            return key;
        }

        return key.substring(0, i);
    }

    /**
     *
     * @param url
     * @return
     * @throws DigestException
     * @throws UnsupportedEncodingException
     */
    protected String urlToKeyIncludingPostDataHash(final String url) throws DigestException, UnsupportedEncodingException {
        if (this.postMessage == null) {
            throw new IllegalStateException("Attempt to get post-style crypto digest, but postData==null");
        }
        final long digest = CryptoUtils.getInstance().toDigest(this.postMessage);
        final String digestAsHex = Long.toString(digest, 16);

        return url + '\n' + digestAsHex;
    }

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

    //#mdebug
    public synchronized String toString() {
        final StringBuffer sb = new StringBuffer();

        sb.append(super.toString());
        sb.append("   retriesRemaining=");
        sb.append(retriesRemaining);
        sb.append("   postMessageLength=");
        if (postMessage == null) {
            sb.append("<null>");
        } else {
            sb.append(postMessage.length);
        }

        if (requestPropertyKeys.isEmpty()) {
            sb.append(L.CRLF + "(default HTTP request, no customer header params)");
        } else {
            sb.append(L.CRLF + "HTTP REQUEST CUSTOM HEADERS");
            for (int i = 0; i < requestPropertyKeys.size(); i++) {
                final String key = (String) requestPropertyKeys.elementAt(i);
                sb.append(L.CRLF + "   ");
                sb.append(key);
                sb.append(": ");
                final String value = (String) requestPropertyValues.elementAt(i);
                sb.append(value);
            }
        }

        if (responseCode == HTTP_OPERATION_PENDING) {
            sb.append(L.CRLF + "(http operation pending, no server response yet)");
        } else {
            sb.append(L.CRLF + "serverHTTPResponseCode=");
            sb.append(responseCode);

            sb.append(L.CRLF + "HTTP RESPONSE HEADERS" + L.CRLF);
            sb.append(PlatformUtils.responseHeadersToString(responseHeaders));
        }
        sb.append(L.CRLF);

        return sb.toString();
    }
    //#enddebug

    /**
     * Register to start receiving notifications of network activity
     *
     * @param listener
     */
    public static void registerNetActivityListener(final NetActivityListener listener) {
        HttpGetter.netActivityListenerDelegate.registerListener(listener);
    }

    /**
     * Unregister to stop receiving notifications of network activity
     *
     * @param listener
     */
    public static void unregisterNetActivityListener(final NetActivityListener listener) {
        HttpGetter.netActivityListenerDelegate.unregisterListener(listener);
    }
    private static volatile int netActivityState = NetActivityListener.INACTIVE; // Compare to the last notification to see if state is new
    private static volatile int netActivityListenerStallTimeout = 10000; // ms
    private static volatile int netActivityListenerInactiveTimeout = 30000; // ms
    private static volatile long nextNetStallTimeout = 0; // ms, when should we transition to NetActivityListener.STALLED state unless something changes in the meantime
    private static volatile long nextNetInactiveTimeout = 0; // ms, when should we transition to idle state unless something changes in the meantime
    private static volatile TimerTask netActivityStallTimerTask = null;
    private static volatile TimerTask netActivityInactiveTimerTask = null;
    private static final WeakHashCache networkActivityActorsHash = new WeakHashCache();
    private static final Runnable uiThreadNetworkStateChange = new Runnable() {
        public void run() {
            // Detect current state. If changed, notify listeners
            final int newNetActivityState = getCurrentNetActivityState();

            if (netActivityState != newNetActivityState) {
                final Object[] listeners = netActivityListenerDelegate.getAllListeners();

                for (int i = 0; i < listeners.length; i++) {
                    ((NetActivityListener) listeners[i]).netActivityStateChanged(netActivityState, newNetActivityState);
                }

                netActivityState = newNetActivityState;
            }
        }
    };

    private static int getCurrentNetActivityState() {
        networkActivityActorsHash.purgeExpiredWeakReferences();
        final long t;

        if (networkActivityActorsHash.size() == 0 || (t = System.currentTimeMillis()) >= nextNetInactiveTimeout) {
            return NetActivityListener.INACTIVE;
        }

        if (t >= nextNetStallTimeout) {
            return NetActivityListener.STALLED;
        }

        return NetActivityListener.ACTIVE;
    }

    /**
     * Check in 5 sec if the net state has changed
     *
     * @param deltaT
     */
    private static void conditionalStartStallTimer(final long deltaT) {
        if (netActivityStallTimerTask == null) {
            final TimerTask tt = new TimerTask() {
                public void run() {
                    netActivityStallTimerTask = null;
                    final long t = System.currentTimeMillis();
                    final long t2 = nextNetStallTimeout;

                    if (t >= t2) {
                        // Update possible state change to stalled
                        PlatformUtils.getInstance().runOnUiThread(uiThreadNetworkStateChange);
                    } else {
                        // Net was active. Test again at the revised stall time
                        conditionalStartStallTimer(t2 - t);
                    }
                }
            };
            netActivityStallTimerTask = tt;
            Task.getTimer().schedule(tt, deltaT);
        }
    }

    /**
     * Check in 30 sec if the net state has changed
     *
     * @param deltaT
     */
    private static void conditionalStartInactiveTimer(final long deltaT) {
        if (netActivityInactiveTimerTask == null) {
            final TimerTask tt = new TimerTask() {
                public void run() {
                    netActivityInactiveTimerTask = null;
                    final long t = System.currentTimeMillis();
                    final long t2 = nextNetInactiveTimeout;

                    if (t >= t2) {
                        // Update possible state change to inactive
                        PlatformUtils.getInstance().runOnUiThread(uiThreadNetworkStateChange);
                    } else {
                        // Net was active. Test again at the revised inactive time
                        conditionalStartInactiveTimer(t2 - t);
                    }
                }
            };
            netActivityInactiveTimerTask = tt;
            Task.getTimer().schedule(tt, deltaT);
        }
    }

    /**
     * Indicate that the network is active at this moment. Tantalum code will
     * automatically call this for you, but if you also use own network code you
     * can call this periodically in your network loop to keep the listeners
     * informed.
     *
     * If the network was previously STALLED or INACTIVE,
     * <code>NetActivityListener</code>s will be notified the network is
     * entering
     * <code>NetActivityListener.ACTIVE</code> state.
     *
     * If no calls to this made before the 5 second timeout,
     * <code>NetActivityListener</code>s will be notified the network is
     * entering
     * <code>NetActivityListener.STALLED</code> state.
     *
     * From the STALLED state, if no calls to this made before the 30 second
     * timeout,
     * <code>NetActivityListener</code>s will be notified the network is
     * entering
     * <code>NetActivityListener.INACTIVE</code> state.
     *
     * @param key identifies this source of network activity and should be
     * highly likely to be unique such as <code>new
     * Integer(this.hashCode()</code>
     */
    public static void networkActivity(final Integer key) {
        if (!networkActivityActorsHash.containsKey(key)) {
            networkActivityActorsHash.put(key, key);
        }
        final long t = System.currentTimeMillis();
        nextNetStallTimeout = t + netActivityListenerStallTimeout;
        conditionalStartStallTimer(netActivityListenerStallTimeout);
        nextNetInactiveTimeout = t + netActivityListenerInactiveTimeout;
        conditionalStartInactiveTimer(netActivityListenerInactiveTimeout);

        if (netActivityState != NetActivityListener.ACTIVE) {
            // Update possible state change to inactive
            PlatformUtils.getInstance().runOnUiThread(uiThreadNetworkStateChange);
        }
    }

    /**
     * Indicate that the network activity is finished.
     *
     * If this is the last current network activity, or if for some other reason
     * you fail to call this method before the timeout period is reached, all
     * <code>NetActivityListener</code>s will be notified
     *
     * @param key identifies this source of network activity and should be
     * highly likely to be unique such as <code>new
     * Integer(this.hashCode()</code>
     */
    public static void endNetworkActivity(final Integer key) {
        networkActivityActorsHash.remove(key);

        if (networkActivityActorsHash.size() == 0) {
            // Update possible state change to inactive
            PlatformUtils.getInstance().runOnUiThread(uiThreadNetworkStateChange);
        }
    }

    /**
     * Override the default 10 second stall and 30 second INACTIVE no net
     * activity timeouts. This alters how quickly all
     * <code>NetActivityListener</code>s are notified that a network is not
     * receiving expected data.
     *
     * @param stallTimeoutInMilliseconds - time without net activity before
     * entering STALLED state. The default is 10000.
     * @param inactiveTimeoutInMilliseconds - time without net activity before
     * entering INACTIVE state. The default is 30000.
     */
    public static void setNetActivityListenerTimeouts(final int stallTimeoutInMilliseconds, final int inactiveTimeoutInMilliseconds) {
        netActivityListenerStallTimeout = stallTimeoutInMilliseconds;
        netActivityListenerInactiveTimeout = inactiveTimeoutInMilliseconds;
    }

    /**
     * Implement this the NetActivityListener to be notified about the network
     * state. This is useful for adding user notification such as a spinner
     * icon.
     *
     * These notifications will always arrive on the User Interface thread.
     */
    public static interface NetActivityListener {

        /**
         * The data network is not in use
         */
        public static final int INACTIVE = 0;
        /**
         * New network data has been requested or received within the last 5
         * seconds
         */
        public static final int ACTIVE = 1;
        /**
         * One or more requests have been made, but no network data has been
         * received during the last 5 seconds
         */
        public static final int STALLED = 2;

        /**
         * An update received on the UI thread indicating changes in network
         * activity level.
         *
         * <pre>Example use cases (previousState -> newState):
         *    INACTIVE -> ACTIVE  : show net spinner
         *    -> INACTIVE : hide net spinner
         *    -> STALLED : pause net spinner (stop moving to show net is slow)
         *    STALLED -> ACTIVE : resume moving net spinner</pre>
         *
         * This allows you to animate your network activity display (often a
         * pinner) on screen using your own timing loop.
         *
         * @param previousState
         * @param newState
         */
        public void netActivityStateChanged(final int previousState, final int newState);
    }
}
