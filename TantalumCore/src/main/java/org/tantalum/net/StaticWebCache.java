/*
 Copyright (c) 2013 Nokia Corporation. All rights reserved.
 Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation.
 Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 affiliates. Other product and company names mentioned herein may be trademarks
 or trade names of their respective owners.

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

import java.io.UnsupportedEncodingException;
import java.security.DigestException;
import org.tantalum.CancellationException;
import org.tantalum.PlatformUtils;
import org.tantalum.Task;
import org.tantalum.TimeoutException;
import org.tantalum.storage.CacheView;
import org.tantalum.storage.FlashCache;
import org.tantalum.storage.FlashCache.StartupTask;
import org.tantalum.storage.FlashDatabaseException;
import org.tantalum.storage.StaticCache;
import org.tantalum.util.CryptoUtils;
import org.tantalum.util.L;
import org.tantalum.util.LOR;
import org.tantalum.util.RollingAverage;

/**
 * A cache of remote http contents backed by local flash memory storage
 *
 * @author tsaa
 */
public final class StaticWebCache extends StaticCache {

    /**
     * An object which creates HttpGetter or a subclass such as HttpPutter each
     * time a web cache needs to fill after a local cache miss.
     *
     * If you override this class, you can create your own replacement which
     * handles cookies, security, server response validation and other custom
     * issues for getting data from a specific HTTP server. For example, if you
     * need to check what you get from the server before deciding it is a valid
     * response and caching it locally, you should provide a custom
     * implementation.
     */
    public static final HttpTaskFactory DEFAULT_HTTP_GETTER_FACTORY = new HttpTaskFactory();
    private final HttpTaskFactory httpTaskFactory;
    /**
     * Get from the local heap or (on heap miss) flash memory cache only- do not
     * request from a web server
     *
     * If there is a hit the the WeakReference heap cache, this is returned
     * instead of re-fetching from flash memory.
     */
    public static final int GET_LOCAL = 0;
    /**
     * Get from the local cache if available, otherwise get from a web server
     */
    public static final int GET_ANYWHERE = 1;
    /**
     * GET from the web, overriding and replacing any value currently stored in
     * the local cache
     */
    public static final int GET_WEB = 2;
    //#debug
    private static final String[] GET_TYPES = {"GET_LOCAL", "GET_ANYWHERE", "GET_WEB"};

    /**
     * Return a cache of default type PlatformUtils.PHONE_DATABASE_CACHE. The
     * cache is created if it does not already exist.
     *
     * @param priority
     * @param defaultCacheView
     * @return
     */
    public static synchronized StaticWebCache getWebCache(final char priority, final CacheView cacheView) throws FlashDatabaseException {
        return getWebCache(priority, PlatformUtils.PHONE_DATABASE_CACHE, cacheView, DEFAULT_HTTP_GETTER_FACTORY, null);
    }

    /**
     * Get existing or create a new local cache of a web service.
     *
     * @param priority
     * @param cacheType
     * @param defaultCacheView - an object for converting from the byte[] format
     * returned by the web server or stored in local flash memory into Object
     * form. The most common handlers use cases return something like a String,
     * JSONModel, XMLModel or Image.
     * @param httpTaskFactory - A custom override of
     * <code>StaticWebCache.HttpTaskFactory</code>. A common need for this is
     * changing this default data validation behavior behavior if the HTTP
     * server you are caching does not return an HTTP header error code but
     * rather gives an unexpected response body that you need to watch for
     * before deciding to cache the response.
     * @param startupTask - an action to perform on each key found in the cache
     * as part of initialization
     * @return
     * @throws FlashDatabaseException
     */
    public static synchronized StaticWebCache getWebCache(final char priority, final int cacheType, final CacheView cacheView, final HttpTaskFactory httpTaskFactory, final FlashCache.StartupTask startupTask) throws FlashDatabaseException {
        StaticWebCache c = (StaticWebCache) getExistingCache(priority, cacheView, httpTaskFactory, StaticWebCache.class);

        if (c == null) {
            c = new StaticWebCache(priority, cacheType, cacheView, httpTaskFactory, startupTask);
            caches.addElement(c);
        }

        return c;
    }

    /**
     * Create a persistent flash memory cache, and provide your own class for
     * creating the HttpGetter or HttpPoster that will be used to getAsync
     * cache-miss items from the web.
     *
     * @param priority
     * @param cacheType
     * @param cacheView
     * @param httpTaskFactory
     * @param startupTask
     * @throws FlashDatabaseException
     */
    private StaticWebCache(final char priority, final int cacheType, final CacheView cacheView, final HttpTaskFactory httpTaskFactory, final StartupTask startupTask) throws FlashDatabaseException {
        super(priority, cacheType, cacheView, startupTask);

        this.httpTaskFactory = httpTaskFactory;
    }

//#mdebug
    /**
     * Read everything from the server, again, and make sure it is byte-for-byte
     * the same as what we get from asking the same thing from flash memory
     *
     * This is a sanity-check test method to confirm if your local copy of a web
     * service is identical to the current state of that web service. It runs
     * synchronously and slowly depending on your network speed and cache size,
     * so it is not recommended as part of your normal production runtime build.
     *
     * Rather slow, especially on a phone, but only done in -debug.jar builds so
     * does not affect production speed. Note also that you can call
     * StaticCache.setFlashCacheEnabled(false) for similar sanity checks which
     * run in a slow "always get from the server" mode.
     */
    public void validateEntireCacheAgainstWebServer() throws FlashDatabaseException {
        final Object[] keys = this.ramCache.getKeys();
        final Task[] tasks = new Task[keys.length];

        for (int i = 0; i < keys.length; i++) {
            final String url = this.flashCache.getKey(((Long) keys[i]).longValue());
            final HttpGetter getter = new HttpGetter(Task.NORMAL_PRIORITY, url);
            final Task localGetCacheValidationTask = new Task(Task.HIGH_PRIORITY) {
                protected Object exec(Object in) {
                    try {
                        final byte[] serverValue = (byte[]) in;
                        final byte[] localValue = flashCache.get(url);
                        final long localDigest = CryptoUtils.getInstance().toDigest(localValue);
                        final long serverDigest = CryptoUtils.getInstance().toDigest(serverValue);

                        if (localDigest == serverDigest) {
                            L.i(this, url, "Server and cache give same values");
                        } else {
                            throw new RuntimeException("Invalid cache entry for " + url + ": digest of server bytes(" + serverValue.length + " does not match digest of cache bytes(" + localValue.length);
                        }
                    } catch (FlashDatabaseException ex) {
                        L.e(this, "Can not validate", url, ex);
                    } catch (DigestException ex) {
                        L.e(this, "Can not validate", url, ex);
                    } catch (UnsupportedEncodingException ex) {
                        L.e(this, "Can not validate", url, ex);
                    }

                    return in;
                }
            }.setClassName("LocalGetCacheValidationTask");
            getter.chain(localGetCacheValidationTask);
            getter.fork();
            tasks[i] = localGetCacheValidationTask;
        }

        try {
            Task.joinAll(tasks);
        } catch (CancellationException ex) {
            L.e(this, "Can not validate entire cache", this.toString(), ex);
        } catch (TimeoutException ex) {
            L.e(this, "Can not validate entire cache", this.toString(), ex);
        }
    }
//#enddebug

    /**
     * Simple synchronous get.
     *
     * This will block until the result is returned, so only call from inside a
     * Task or other worker thread. Never call from the UI thread.
     *
     * Unlike getAsync(url, chainedTask), this may hold multiple threads for
     * some time depending on cache status. It is thus higher performance for
     * your app as a whole to use the async request and let chaining sequence
     * the load across threads.
     *
     * @param url
     * @return
     * @throws CancellationException
     * @throws TimeoutException
     */
    public Object get(final String url) throws CancellationException, TimeoutException {
        return getAsync(url, null).get();
    }

    /**
     * The simplest and most common way to get is for something that updates the
     * UI, so default Task.FASTLANE_PRIORITY and StaticWebCache.GET_ANYWHERE are
     * used.
     *
     * Note, do not get() a value from the Task returned from GET_ANYWHERE
     * operations. This method may need to fall back to an HTTP GET the value
     * returned by get() may be null. The value passed to the chainedTask will
     * always be non-null as it is provided after the HTTP GET in case of a
     * cache miss. You can use this "GET_ANYWHERE returns quickly, before any
     * optional HTTP operations" to your advantage as in the following example:
     *
     * <code>
     * // In painter loop, do a synchronous get from cache
     * Image img = imageCache.getAsync(url, new Task() {
     *     protected Object exec(Object in) {
     *         repaint(); //
     *     }
     * }).get();
     * if (img != null) // paint it if you got it from cache
     * </code>
     *
     * Note that the above example will paint more smoothly if you do not
     * <code>get()</code> and thus wait for the local cache read which may be
     * slow if there is an ongoing cache write operation already in progress.
     * For smoothest fast paint, omit the
     * <code>get()</code> and
     * <code>if (img != null) </code> and re-render when the cache results are
     * available.
     *
     * @param url
     * @param chainedTask
     * @return
     */
    public Task getAsync(final String url, final Task chainedTask) {
        return getAsync(url, Task.FASTLANE_PRIORITY, GET_ANYWHERE, chainedTask);
    }

    /**
     * Return the byte[] results of an HTTP GET from the URL specified by key.
     *
     * Depending on the getType, the result may actually come from the local
     * cache in which case the service is much faster and works offline with any
     * previously requested results when no data connection is available.
     *
     * @param url
     * @param priority
     * @param getType
     * @param chainedTask
     * @return
     */
    public Task getAsync(final String url, final int priority, final int getType, final Task chainedTask) {
        return getAsync(url, null, priority, getType, chainedTask, httpTaskFactory, null);
    }

    /**
     * Retrieve one item from the cache using the method specified in the
     * getType parameter. The call returns immediately, is executed concurrently
     * on a worker thread, and code in the Task will be executed automatically
     * on a Worker and optionally also the UI thread after the getAsync
     * operation completes.
     *
     * Normal use case is to specify the method StaticWebCache.GET_ANYWHERE.
     * This attempts getAsync in the following order:
     *
     * 1. RAM if available: Task return is fast but still asynchronous
     *
     * 2. Local flash storage (RMS or database depending on the platform) if
     * available: may take several milliseconds, or longer if the Worker task
     * queue is long and you do not specify high cachePriorityChar.
     *
     * 3. web: may take several seconds for a HTTP response which is then cached
     * locally automatically.
     *
     * The getAsync() call returns immediately, is executed concurrently on a
     * worker thread, and code in the Task will be executed automatically on a
     * Worker and optionally also the UI thread if UITask after the getAsync
     * operation completes.
     *
     * @param url - The web service location to HTTP_GET the cacheable data
     * @param postMessage - HTTP POST will be used if this value is non-null,
     * otherwise HTTP GET is used
     * @param priority
     * @param      * getType <code>StaticWebCache.GET_ANYWHERE</code>, <code>StaticWebCache.GET_WEB</code>
     * or <code>StaticWebCache.GET_LOCAL</code>
     * @param nextTask - your <code>Task</code> which is given the data returned
     * and executed after the getAsync operation.
     * @param taskFactory - Specify a custom class for creating network
     * requests. If null, the default StaticWebCache.HttpTaskFactory specified
     * when you create this StaticWebCache will be used.
     * @param cacheView - usually null, which means use the default CacheView
     * specified when this cache was created. If you specify an alternate value,
     * all requests will bypass the RAM cache and be served from memory. If you
     * specify an alternate CacheView, results may be slightly slower since to
     * maintain cache integrity they must execute serially after any pending
     * write operations.
     * @return a new Task containing the result, or null if the
     * StaticWebCache.HttpTaskFactory decided not to honor the GET_WEB request
     * for application-specific reasons such as 'we don't need to do this
     * anymore'.
     */
    public Task getAsync(final String url, final byte[] postMessage, final int priority, final int getType, final Task nextTask, StaticWebCache.HttpTaskFactory taskFactory, final CacheView cacheView) {
        if (url == null) {
            throw new NullPointerException("Can not getAsync() with null key");
        }
        if (taskFactory == null) {
            taskFactory = this.httpTaskFactory;
        }

        final Task getTask;
        int getterPriority;

        //#debug
        L.i(this, "getAsync getType=" + GET_TYPES[getType] + " priority=" + priority + "key=" + url, "nextTask=" + nextTask);
        switch (getType) {
            case GET_LOCAL:
                getterPriority = boostHighPriorityToFastlane(priority);
                getterPriority = switchToSerialPriorityIfNotDefaultCacheView(getterPriority, cacheView);
                getTask = new StaticCache.GetLocalTask(getterPriority, url, cacheView);
                getTask.chain(nextTask);
                break;

            case GET_ANYWHERE:
                getterPriority = boostHighPriorityToFastlane(priority);
                getterPriority = switchToSerialPriorityIfNotDefaultCacheView(getterPriority, cacheView);
                getTask = new StaticWebCache.GetAnywhereTask(getterPriority, url, postMessage, nextTask, taskFactory, cacheView);
                break;

            case GET_WEB:
                getterPriority = preventWebTaskFromUsingFastLane(priority);
                getTask = getHttpGetter(getterPriority, url, postMessage, nextTask, taskFactory, cacheView);
                if (getTask == null) {
                    nextTask.cancel(false, "StaticWebCache was told by " + taskFactory.getClass().getName() + " not to complete the get operation (null returned): " + url);
                    return null;
                }
                break;

            default:
                throw new IllegalArgumentException("StaticWebCache get type not supported: " + getType);
        }

        return getTask.fork();
    }

    /**
     * HTTP operations are relatively slow and not allowed into the FASTLANE to
     * give local operations a way to keep the UI responsive even when there are
     * many pending HTTP operations.
     *
     * @param priority
     * @return
     */
    private int preventWebTaskFromUsingFastLane(final int priority) {
        if (priority == Task.FASTLANE_PRIORITY) {
            return Task.HIGH_PRIORITY;
        }

        return priority;
    }

    /**
     * Retrieve the object from WEB if it is not already cached locally.
     *
     * @param key
     * @throws FlashDatabaseException
     */
    public void prefetch(final String key) throws FlashDatabaseException {
        if (synchronousRAMCacheGet(key) == null) {
            new Task(Task.IDLE_PRIORITY) {
                public Object exec(final Object in) {
                    try {
                        getAsync(key, Task.IDLE_PRIORITY, StaticWebCache.GET_ANYWHERE, null);
                    } catch (Exception e) {
                        //#debug
                        L.e("Can not prefetch", key, e);
                    }

                    return in;
                }
            }.setClassName("Prefetch").fork();
        }
    }

    /**
     * This Task performs a StaticWebCache.GET_ANYWHERE operation on a Worker
     * thread in the background.
     *
     * For convenience, you normally call StaticWebCache.getAsync(String url,
     * int cachePriorityChar, int getType, Task task) where getType is
     * StaticWebCache.GET_ANYWHERE and task is some operation you supply which
     * you would like to act on the result of the getAsync(). You can, if you
     * prefer, invoke this class directly to tailor how it operates or set
     * parameters.
     *
     * The pattern for synchronous use when on a background Worker thread is:
     * <pre><code>
     * Object out = new GetAnywhereTask(url).get();
     * </code></pre>
     *
     * The pattern for asynchronous use from the UI thread is:
     * <pre><code>
     * UITask uiTask = new UITask() {
     *    protected Object exec(final Object in) {
     *       Object out = in;
     *       // Do something on the Worker thread with the fetched data
     *       return out;
     *    }
     *
     *    protected void onCanceled() {
     *        // Do something on the UI thread if the Task is cancel()ed or there is an error (usually HTTP error)
     *    }
     *
     *    protected void onPostExecute(Object result) {
     *       // Do something on the UI Thread with the result from exec() above
     *    }
     * };
     *
     * Task getTask = new GetAnywhereTask(String url);
     * getTask.chain(uiTask);
     * getTask.fork();
     * </code></pre>
     */
    private class GetAnywhereTask extends Task {

        final int priority;
        final byte[] postMessage;
        final Task nextTask;
        final StaticWebCache.HttpTaskFactory taskFactory;
        final CacheView cacheView;

        /**
         * Create a
         * <code>StaticWebCache.GET_ANYWHERE</code> actor which will execute on
         * the
         * <code>Worker</code> thread. This already knows the URL, so does not
         * need to be
         * <code>chain()</code>ed to another
         * <code>Task</code> to operate properly.
         *
         * For most purposes, it is easier to use the convenience method
         * <code>StaticWebCache.getAsync()</code> to chain your Task after the
         * getAsync operation before
         * <code>fork()</code>ing the Task for background execution.
         *
         * If you chain to a GetAnywhereTask, be aware that the output is either
         * the use form (POJO if the value was found in local cache) or a
         * <code>Task</code> that has been forked for you to get the result from
         * the web.
         *
         * @param key
         * @param cachePriorityChar
         * @param postMessage
         */
        private GetAnywhereTask(final int priority, final String key, final byte[] postMessage, final Task nextTask, final StaticWebCache.HttpTaskFactory taskFactory, final CacheView cacheView) {
            super(priority, key);

            this.postMessage = postMessage;
            this.priority = priority;
            this.nextTask = nextTask;
            this.taskFactory = taskFactory;
            this.cacheView = cacheView;
        }

        protected Object exec(final Object in) {
            Object out = null;

            if (in == null || !(in instanceof String)) {
                //#debug
                L.i(this, "ERROR", "Must receive a String url, but got " + (in == null ? "null" : in.toString()));
                cancel(false, getClassName() + " got bad url: " + in);
            } else {
                try {
                    final String url = (String) in;
                    //#debug
                    L.i(this, "StaticWebCache.HttpGetterTask.exec: GetAnywhereTask get locally", url);
                    out = synchronousGet(url, cacheView);
                    if (out == null) {
                        //#debug
                        L.i(this, "StaticWebCache.HttpGetterTask.exec: GetAnywhereTask did not found locally, get from the web", url);
                        final Task httpGetter = getHttpGetter(preventWebTaskFromUsingFastLane(priority), url, postMessage, nextTask, taskFactory, cacheView);
                        if (httpGetter == null) {
                            //#debug
                            L.i(this, getClassName() + " was told by " + StaticWebCache.this.httpTaskFactory.getClass().getName() + " not to complete the HTTP operation at this time by returning a null HttpGetter", url);
                            cancel(false, getClassName() + " was told by " + StaticWebCache.this.httpTaskFactory.getClass().getName() + " not to complete the HTTP operation at this time by returning a null HttpGetter: " + url);
                        } else {
                            httpGetter.fork();
                        }
                    } else {
                        chain(nextTask);
                    }
                } catch (FlashDatabaseException e) {
                    //#debug
                    L.e(this, "Can not get", (String) in, e);
                    chain(nextTask);
                    cancel(false, "Can not GET_ANYWHERE, in=" + (String) in, e);
                }
            }
            //#mdebug
            String inDebug = null;
            String outDebug = null;

            if (in != null) {
                inDebug = in.toString();
            }
            if (out != null) {
                outDebug = out.toString();
            }
            L.i(this, "End", "in=" + inDebug + " out=" + outDebug);
            //#enddebug

            return out;
        }
    }

    /**
     * Fetch from the url and validate the response.
     *
     * If the taskFactory decides not to fetch at this time (no longer needed,
     * etc) then this method may return null
     *
     * @param priority
     * @param url
     * @param postMessage
     * @param nextTask
     * @param taskFactory
     * @param skipHeap
     * @return
     */
    private Task getHttpGetter(final int priority, final String url, final byte[] postMessage, final Task nextTask, final StaticWebCache.HttpTaskFactory taskFactory, final CacheView cacheView) {
        final HttpGetter httpGetter = taskFactory.getHttpTask(priority, url, postMessage);

        if (httpGetter == null) {
            //#debug
            L.i(this, taskFactory.getClass().getName() + " signaled the HttpGetter is no longer needed- aborting", url);
            return null;
        }

        //#debug
        L.i(this, "getHttpGetter(" + url + ")", L.CRLF + httpGetter);

        final Task validationTask = new Task(Task.FASTLANE_PRIORITY) {
            protected Object exec(final Object in) {
                if (!(in instanceof LOR)) {
                    throw new IllegalArgumentException("StaticWebCache validationTask was passed bad argument: " + in);
                }
                Object out = null;
                final LOR bytesReference = (LOR) in;

                //#debug
                L.i(this, "Validation start", "in=" + in + " - " + httpGetter.toString());
                if (!taskFactory.validateHttpResponse(httpGetter, bytesReference.getBytes())) {
                    //#debug
                    L.i(this, "Rejected server response", httpGetter.toString());
                    cancel(false, "StaticWebCache.GetWebTask failed HttpTaskFactory validation: " + url);
                } else {
                    try {
                        out = put(url, bytesReference, cacheView, null);
                    } catch (FlashDatabaseException ex) {
                        //#debug
                        L.e(this, "Can not put web service response to heap cache", url, ex);
                        cancel(false, "Can not put web service response to heap cache: " + url + " : " + ex);
                    }
                }

                return out;
            }
        }.setClassName("ValidateAndWriteWebServiceReponse").chain(nextTask);

        return httpGetter.chain(validationTask);
    }

    /**
     * If you override this default implementation, you can add custom header
     * parameters to the HTTP request and act on custom header fields such as
     * cookies in the HTTP response.
     *
     * If your override returns null, that is an indication that the HTTP GET is
     * obsolete and no longer desired by the application.
     */
    public static class HttpTaskFactory {

        /**
         * Tell the task factory if it should delay concurrent downloads such
         * that the data comes in to this cache over the network one item at a
         * time. This may increase throughput, but more importantly it may
         * improve user experience (UX) on slow network. Delays are self-tuning
         * based on current network conditions.
         */
        public volatile boolean getSequencerEnabled = false;
        /**
         * A measure of how long on average the last 10 HTTP operations to fetch
         * from the server have taken. If the get sequencer is enabled, this is
         * used internally to sequence HTTP operations such that both network
         * contention is minimized. If the get sequencer is disabled, this
         * network speed measurement is not taken. This is most useful on 2G
         * networks but may also increase response time in a heavily loaded or
         * otherwise poorly performing 3G network.
         */
        public final RollingAverage averageGetTimeMillis = new RollingAverage(10, 700.0f);

        private final class TimerEndTask extends Task {

            final HttpGetter httpGetter;

            TimerEndTask(final HttpGetter httpGetter) {
                super(Task.FASTLANE_PRIORITY);

                this.httpGetter = httpGetter;
            }

            protected Object exec(final Object in) {
                averageGetTimeMillis.update((float) (System.currentTimeMillis() - httpGetter.getStartTime()));
                //#debug
                L.i(this, "Average cache HTTP response time", "" + averageGetTimeMillis.value());

                return in;
            }
        }

        /**
         * Get an HTTP task, possibly with modified headers. Although this is
         * usually an HttpGetter, in could be an HttpPoster.
         *
         * You can override this method to customize it. If you return null,
         * then that will trigger the StaticWebCache async get Task and any
         * chained operations to cancel.
         *
         * @param priority
         * @param url
         * @param postMessage
         * @return
         */
        public HttpGetter getHttpTask(final int priority, final String url, final byte[] postMessage) {
            if (url == null) {
                throw new NullPointerException("HttpTaskFactory was asked to make an HttpGetter for a null url");
            }
            final HttpGetter httpGetter;
            if (postMessage == null) {
                //#debug
                L.i(this, "Generating a new HttpGetter", url);
                httpGetter = new HttpGetter(priority, url);
            } else {
                //#debug
                L.i(this, "Generating a new HttpPoster", url + " postDataLength=" + postMessage.length + " priority=" + priority);
                httpGetter = new HttpPoster(priority, url, postMessage);
            }
            if (getSequencerEnabled) {
                httpGetter.chain(new TimerEndTask(httpGetter));
            }

            return httpGetter;
        }

        /**
         * Validate the server response.
         *
         * This is also where you can check, store and otherwise process cookies
         * and custom security tags from the server.
         *
         * @param httpGetter
         * @param bytesReceived
         * @return false to cancel() the HTTP operation
         */
        public boolean validateHttpResponse(final HttpGetter httpGetter, final byte[] bytesReceived) {
            final int responseCode = httpGetter.getResponseCode();

            if (responseCode >= HttpGetter.HTTP_500_INTERNAL_SERVER_ERROR) {
                //#debug
                L.i(this, "Invalid response code", responseCode + " received");
                return false;
            }
            if (responseCode >= HttpGetter.HTTP_400_BAD_REQUEST) {
                //#debug
                L.i(this, "Invalid response code", responseCode + " received");
                return false;
            }
            if (responseCode >= HttpGetter.HTTP_300_MULTIPLE_CHOICES) {
                /*
                 * Redirect. Not valid since we don't support this at the
                 * Tantalum level. If you replace this validator or fix this in
                 * Tantalum to follow the redirect then change this.
                 */
                //#debug
                L.i(this, "Redirect not supported", responseCode + " received");
                return false;
            }
            if (bytesReceived == null || bytesReceived.length == 0) {
                //#debug
                L.i(this, "Trivial response", "0 bytes received");
                return false;
            }

            return true;
        }
    }

    /**
     * Analyze if this cache is the same one that would be returned by a call to
     * StaticWebCache.getCache() with the same parameters
     *
     * @param priority
     * @param defaultCacheView
     * @param taskFactory
     * @return
     */
    protected boolean equals(final char priority, final CacheView cacheView, final Object taskFactory) {
        return this.cachePriorityChar == priority && this.defaultCacheView.equals(cacheView) && this.httpTaskFactory.equals(taskFactory);
    }
}