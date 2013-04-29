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

import org.tantalum.PlatformUtils;
import org.tantalum.Task;
import org.tantalum.storage.DataTypeHandler;
import org.tantalum.storage.FlashDatabaseException;
import org.tantalum.storage.StaticCache;
import org.tantalum.util.L;

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
     * Get from the local cache only- do not request from a web server
     */
    public static final int GET_LOCAL = 0;
    /**
     * Get from the local cache if available, otherwise GET from a web server
     */
    public static final int GET_ANYWHERE = 1;
    /**
     * GET from the web, overriding and replacing any value currently stored in
     * the local cache
     */
    public static final int GET_WEB = 2;

    /**
     * Get existing or create a new local cache of a web service.
     *
     * This uses the default
     * <code>StaticWebCache.HttpTaskFactory</code> is to perform HTTP GET or
     * HTTP PUT with no cookies and minimal validation of the server responses.
     * You can change this by providing your own custom override of this class.
     * A common need for this changing this default behavior is if the HTTP
     * server you are caching does not return an HTTP header error code but
     * rather gives an unexpected response body that you need to watch for
     * before deciding to cache the response.
     *
     * @param priority
     * @param cacheType
     * @param handler
     * @return
     */
    public static synchronized StaticWebCache getWebCache(final char priority, final int cacheType, final DataTypeHandler handler) {
        try {


            return (StaticWebCache) getWebCache(priority, cacheType, handler, DEFAULT_HTTP_GETTER_FACTORY);
        } catch (Exception e) {
            //#debug
            L.e("Can not create StaticWebCache", "" + priority, e);
            return null;
        }
    }

    /**
     * Return a cache of default type PlatformUtils.PHONE_DATABASE_CACHE.
     *
     * @param priority
     * @param handler
     * @return
     */
    public static synchronized StaticWebCache getWebCache(final char priority, final DataTypeHandler handler) {
        return getWebCache(priority, PlatformUtils.PHONE_DATABASE_CACHE, handler);
    }

    /**
     * Get existing or create a new local cache of a web service.
     *
     * @param priority
     * @param cacheType
     * @param handler - an object for converting from the byte[] format returned
     * by the web server or stored in local flash memory into Object form. The
     * most common handlers use cases return something like a String, JSONModel,
     * XMLModel or Image.
     * @param httpTaskFactory - A custom override of
     * <code>StaticWebCache.HttpTaskFactory</code>. A common need for this is
     * changing this default data validation behavior behavior if the HTTP
     * server you are caching does not return an HTTP header error code but
     * rather gives an unexpected response body that you need to watch for
     * before deciding to cache the response.
     *
     * @return
     * @throws FlashDatabaseException
     */
    public static synchronized StaticWebCache getWebCache(final char priority, final int cacheType, final DataTypeHandler handler, final HttpTaskFactory httpTaskFactory) throws FlashDatabaseException {
        StaticWebCache c = (StaticWebCache) getExistingCache(priority, handler, httpTaskFactory, StaticWebCache.class);

        if (c == null) {
            c = new StaticWebCache(priority, cacheType, handler, httpTaskFactory);
            caches.addElement(c);
        }

        return c;
    }

    /**
     * Create a persistent flash memory cache, and provide your own class for
     * creating the HttpGetter or HttpPoster that will be used to getAsync
     * cache-miss items from the web.
     *
     * @param cachePriorityChar
     * @param cacheType
     * @param handler
     * @param httpTaskFactory
     * @throws DigestException
     * @throws UnsupportedEncodingException
     */
    /**
     *
     * @param cachePriorityChar
     * @param cacheType
     * @param handler
     * @param httpTaskFactory
     * @throws FlashDatabaseException
     */
    private StaticWebCache(final char priority, final int cacheType, final DataTypeHandler handler, final HttpTaskFactory httpTaskFactory) throws FlashDatabaseException {
        super(priority, cacheType, handler);

        this.httpTaskFactory = httpTaskFactory;
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
        return getAsync(url, null, priority, getType, chainedTask);
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
     * @param
     * getType <code>StaticWebCache.GET_ANYWHERE</code>, <code>StaticWebCache.GET_WEB</code>
     * or <code>StaticWebCache.GET_LOCAL</code>
     * @param nextTask - your <code>Task</code> which is given the data returned
     * and executed after the getAsync operation.
     *
     * @return a new Task containing the result, or null if the
     * StaticWebCache.HttpTaskFactory decided not to honor the GET_WEB request
     * for application-specific reasons such as 'we don't need to do this anymore'.
     * If you
     */
    public Task getAsync(final String url, final byte[] postMessage, final int priority, final int getType, final Task nextTask) {
        if (url == null) {
            throw new IllegalArgumentException("Can not getAsync() with null key");
        }

        final Task getTask;
        final int getterPriority;

        //#debug
        L.i("StaticWebCache getType=" + getType + " : " + nextTask, "key=" + url);
        switch (getType) {
            case GET_LOCAL:
                //#debug
                L.i("Begin StaticWebCache(" + cachePriorityChar + ").getAsync(GET_LOCAL)", url);
                getterPriority = allowLocalCacheReadToUseFastlane(priority);
                getTask = new StaticCache.GetLocalTask(getterPriority, url);
                getTask.chain(nextTask);
                break;

            case GET_ANYWHERE:
                //#debug
                L.i("Begin StaticWebCache(" + cachePriorityChar + ").getAsync(GET_ANYWHERE)", url);
                getterPriority = allowLocalCacheReadToUseFastlane(priority);
                getTask = new StaticWebCache.GetAnywhereTask(getterPriority, url, postMessage, nextTask);
                break;

            case GET_WEB:
                //#debug
                L.i("Begin StaticWebCache(" + cachePriorityChar + ").getAsync(GET_WEB)", url);
                getterPriority = preventWebTaskFromUsingFastLane(priority);
                getTask = getHttpGetter(getterPriority, url, postMessage, nextTask);
                if (getTask == null) {
                    nextTask.cancel(false, "StaticWebCache was told by " + this.httpTaskFactory.getClass().getName() + " not to complete the get operation (null returned): " + url);
                    return null;
                }
                break;

            default:
                throw new IllegalArgumentException("StaticWebCache get type not supported: " + getType);
        }

        return getTask.fork();
    }

    /**
     * Local flash read should be fast- allow it into the FASTLANE so it can
     * bump past a (possible large number of blocking) HTTP operations.
     *
     * @param priority
     * @return
     */
    private int allowLocalCacheReadToUseFastlane(final int priority) {
        if (priority == Task.HIGH_PRIORITY) {
            return Task.FASTLANE_PRIORITY;
        }

        return priority;
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
            (new Task(Task.IDLE_PRIORITY) {
                public Object exec(final Object in) {
                    try {
                        getAsync(key, Task.IDLE_PRIORITY, StaticWebCache.GET_ANYWHERE, null);
                    } catch (Exception e) {
                        //#debug
                        L.e("Can not prefetch", key, e);
                    }

                    return in;
                }
            }.setClassName("Prefetch")).fork();
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
        private GetAnywhereTask(final int priority, final String key, final byte[] postMessage, final Task nextTask) {
            super(priority, key);

            this.postMessage = postMessage;
            this.priority = priority;
            this.nextTask = nextTask;
        }

        protected Object exec(final Object in) {
            Object out = null;

            if (in == null || !(in instanceof String)) {
                //#debug
                L.i(this, "ERROR", "Must receive a String url, but got " + (in == null ? "null" : in.toString()));
                cancel(false, this.getClassName() + " got bad input: " + in);
            } else {
                //#debug
                L.i(this, "get", (String) in);
                try {
                    final String url = (String) in;
                    out = synchronousGet(url);
                    if (out == null) {
                        //#debug
                        L.i(this, "Not found locally, get from the web", (String) in);
                        final Task httpGetter = getHttpGetter(preventWebTaskFromUsingFastLane(priority), url, postMessage, nextTask).fork();
                        if (httpGetter == null) {
                            cancel(false, getClassName() + " was told by " + StaticWebCache.this.httpTaskFactory.getClass().getName() + " not to complete the HTTP operation at this time by returning a null HttpGetter: " + url);
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
     * @param url
     * @param cachePriorityChar
     * @param postMessage
     * @return the HttpGetter task
     */
    private Task getHttpGetter(final int priority, final String url, final byte[] postMessage, final Task nextTask) {
        final HttpGetter httpGetter = httpTaskFactory.getHttpTask(priority, url, postMessage);

        if (httpGetter == null) {
            //#debug
            L.i(this, httpTaskFactory.getClass().getName() + " signaled the HttpGetter is no longer needed- aborting", url);
            return httpGetter;
        }

        //#debug
        L.i(this, "getHttpGetter", L.CRLF + httpGetter);

        final class ValidationTask extends Task {

            public ValidationTask(final int priority) {
                super(priority);

                setShutdownBehaviour(Task.EXECUTE_NORMALLY_ON_SHUTDOWN);
            }

            protected Object exec(final Object in) {
                Object out = null;

                //#debug
                L.i(this, "Validation start", "in=" + in + " - " + httpGetter.toString());
                if (!httpTaskFactory.validateHttpResponse(httpGetter, (byte[]) in)) {
                    //#debug
                    L.i(this, "Rejected server response", httpGetter.toString());
                    cancel(false, "StaticWebCache.GetWebTask failed HttpTaskFactory validation: " + url);
                } else {
                    try {
                        out = put(url, (byte[]) in); // Convert to use form
                    } catch (FlashDatabaseException e) {
                        //#debug
                        L.e("Can not set result after staticwebcache http get", httpGetter.toString(), e);
                        cancel(false, "Can not set result after staticwebcache http get: " + httpGetter.toString(), e);
                    }
                }

                return out;
            }
        }

        final ValidationTask validationTask = new ValidationTask(Task.FASTLANE_PRIORITY);
        httpGetter.chain(validationTask);
        validationTask.chain(nextTask);

        return httpGetter;
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
                throw new IllegalArgumentException("HttpTaskFactory was asked to make an HttpGetter for a null url");
            }
            if (postMessage == null) {
                //#debug
                L.i(this, "Generating a new HttpGetter", url);
                return new HttpGetter(priority, url);
            } else {
                //#debug
                L.i(this, "Generating a new HttpPoster", url + " postDataLength=" + postMessage.length + " priority=" + priority);
                return new HttpPoster(priority, url, postMessage);
            }
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
     * @param handler
     * @param taskFactory
     * @return
     */
    protected boolean equals(final char priority, final DataTypeHandler handler, final Object taskFactory) {
        return this.cachePriorityChar == priority && this.handler.equals(handler) && this.httpTaskFactory.equals(taskFactory);
    }
}