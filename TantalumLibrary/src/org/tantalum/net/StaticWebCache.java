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

import java.util.Hashtable;
import org.tantalum.Task;
import org.tantalum.storage.DataTypeHandler;
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
     * @param handler
     * @return
     */
    public static synchronized StaticWebCache getWebCache(final char priority, final DataTypeHandler handler) {
        return (StaticWebCache) getWebCache(priority, handler, DEFAULT_HTTP_GETTER_FACTORY);
    }

    /**
     * Get existing or create a new local cache of a web service.
     *
     * @param priority - must be unique in your application. Lower numbers or
     * letters are garbage collected first when flash storage runs out.
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
     * @return
     */
    public static synchronized StaticWebCache getWebCache(final char priority, final DataTypeHandler handler, final HttpTaskFactory httpTaskFactory) {
        StaticWebCache c = (StaticWebCache) getExistingCache(priority, handler, httpTaskFactory, StaticWebCache.class);

        if (c == null) {
            c = new StaticWebCache(priority, handler, httpTaskFactory);
        }

        return c;
    }

    /**
     * Create a persistent flash memory cache, and provide your own class for
     * creating the HttpGetter or HttpPoster that will be used to getAsync
     * cache-miss items from the web.
     *
     * @param priority
     * @param handler
     * @param httpTaskFactory
     */
    private StaticWebCache(final char priority, final DataTypeHandler handler, final HttpTaskFactory httpTaskFactory) {
        super(priority, handler);

        this.httpTaskFactory = httpTaskFactory;
    }

    /**
     * HTTP GET from the URL specified by key
     *
     * @param key
     * @param priority
     * @param getType
     * @param chainedTask
     * @return
     */
    public Task getAsync(final String key, final int priority, final int getType, final Task chainedTask) {
        return getAsync(key, null, priority, getType, chainedTask);
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
     * queue is long and you do not specify high priority.
     *
     * 3. web: may take several seconds for a HTTP response which is then cached
     * locally automatically.
     *
     * The getAsync() call returns immediately, is executed concurrently on a
     * worker thread, and code in the Task will be executed automatically on a
     * Worker and optionally also the UI thread if UITask after the getAsync
     * operation completes.
     *
     * @param key - The web service location to HTTP_GET the cacheable data
     * @param postMessage - HTTP POST will be used if this value is non-null,
     * otherwise HTTP GET is used
     * @param priority - note that HIGH_PRIORITY requests will be automatically
     * bumped into FASTLINE_PRIORITY unless it is a GET_WEB operation.
     * @param getType *      * - <code>StaticWebCache.GET_ANYWHERE</code>, <code>StaticWebCache.GET_WEB</code>
     * or <code>StaticWebCache.GET_LOCAL</code>
     * @param chainedTask - your <code>Task</code> which is given the data
     * returned and executed after the getAsync operation.
     *
     * @return a new Task containing the result
     */
    public Task getAsync(final String key, final byte[] postMessage, int priority, final int getType, final Task chainedTask) {
        if (key == null) {
            throw new IllegalArgumentException("Can not getAsync() with null key");
        }

        final Task getTask;

        //#debug
        L.i("StaticWebCache getType=" + getType + " : " + chainedTask, "key=" + key);
        switch (getType) {
            case GET_LOCAL:
                //#debug
                L.i("GET_LOCAL", key);
                getTask = new StaticCache.GetLocalTask(key, priority);
                if (priority == Task.HIGH_PRIORITY) {
                    priority = Task.FASTLANE_PRIORITY;
                }
                break;

            case GET_ANYWHERE:
                //#debug
                L.i("GET_ANYWHERE", key);
                getTask = new StaticWebCache.GetAnywhereTask(key, priority, postMessage);
                if (priority == Task.HIGH_PRIORITY) {
                    priority = Task.FASTLANE_PRIORITY;
                }
                break;

            case GET_WEB:
                //#debug
                L.i("GET_WEB", key);
                getTask = new StaticWebCache.GetWebTask(key, priority, postMessage);
                if (priority == Task.FASTLANE_PRIORITY) {
                    priority = Task.HIGH_PRIORITY;
                }
                break;

            default:
                throw new IllegalArgumentException("StaticWebCache get type not supported: " + getType);
        }
        getTask.chain(chainedTask);
        getTask.fork(priority);

        return getTask;
    }

    /**
     * Retrieve the object from WEB if it is not already cached locally.
     *
     * @param key
     */
    public void prefetch(final String key) {
        if (synchronousRAMCacheGet(key) == null) {
            (new Task() {
                public Object exec(final Object in) {
                    try {
                        getAsync(key, Task.IDLE_PRIORITY, StaticWebCache.GET_ANYWHERE, null);
                    } catch (Exception e) {
                        //#debug
                        L.e("Can not prefetch", key, e);
                    }

                    return in;
                }
            }).fork(Task.IDLE_PRIORITY);
        }
    }

    /**
     * This Task performs a StaticWebCache.GET_ANYWHERE operation on a Worker
     * thread in the background.
     *
     * For convenience, you normally call StaticWebCache.getAsync(String url,
     * int priority, int getType, Task task) where getType is
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
    final public class GetAnywhereTask extends Task {

        final int priority;
        final byte[] postMessage;

        /**
         * Create a chainable task where key will be provided as the output from
         * the previous step in the chain.
         *
         * @param priority
         * @param postMessage
         */
        public GetAnywhereTask(final int priority, final byte[] postMessage) {
            super();

            this.postMessage = postMessage;
            this.priority = priority;
        }

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
         * the use form (if found locally) or a Task to get the result from the
         * web.
         * 
         * @param key
         * @param priority
         * @param postMessage
         */
        public GetAnywhereTask(final String key, final int priority, final byte[] postMessage) {
            super(key);

            this.postMessage = postMessage;
            this.priority = priority;
        }

        protected Object exec(final Object in) {
            Object out = in;

            if (in == null || !(in instanceof String)) {
                //#debug
                L.i("ERROR", "StaticWebCache.GetAnywhereTask must receive a String url, but got " + in == null ? "null" : in.toString());
                cancel(false, "StaticWebCache.getAnywhereTask got bad input: " + in);
            } else {
                //#debug
                L.i("Async StaticCache.GET_ANYWHERE get", (String) in);
                try {
                    out = synchronousGet((String) in);
                    if (out == null) {
                        //#debug
                        L.i("StaticWebCache: not found locally, get from the web", (String) in);
                        out = new GetWebTask((String) in, priority, postMessage).fork(Task.HIGH_PRIORITY).get();
                    }
                } catch (Exception e) {
                    //#debug
                    L.e("Can not get from StaticWebCache", (String) in, e);
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
            L.i("End GetAnywhereTask " + inDebug, outDebug);
            //#enddebug
            return out;
        }
    }

    /**
     * This
     * <code>Task</code> performs a
     * <code>StaticWebCache.GET_WEB</code> operation on a
     * <code>Worker</code> thread in the background.
     *
     * For convenience, you normally call
     * <code>StaticWebCache.getAsync(String url,
     * int priority, int getType, Task task)</code> where
     * <code>getType</code> is
     * <code>StaticWebCache.GET_WEB</code> and
     * <code>task</code> is some operation you supply which you would like to
     * act on the result of the
     * <code>getAsync()</code>. You can, if you prefer, invoke this class
     * directly to tailor how it operates or set parameters.
     *
     * The pattern for synchronous use when on a background Worker thread is:
     * <pre><code>
     * Object out = new GetWebTask(url).get();
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
     * Task getTask = new GetWebTask(String url);
     * getTask.chain(uiTask);
     * getTask.fork();
     */
    final public class GetWebTask extends Task {

        final int priority;
        final byte[] postMessage;

        /**
         * Create a
         * <code>StaticWebCache.GET_WEB</code> actor which will execute on the
         * Worker thread using as input the chained output from a previous Task.
         *
         * For most purposes, it is easier to use the convenience method
         * <code>StaticWebCache.getAsync()</code> to chain your Task after the
         * getAsync operation before
         * <code>fork()</code>ing the Task for background execution.
         *
         * @param postMessage
         */
        public GetWebTask(final int priority, final byte[] postMessage) {
            super();

            this.priority = priority;
            this.postMessage = postMessage;
        }

        /**
         * Create a
         * <code>StaticWebCache.GET_WEB</code> actor which will execute on the
         * <code>Worker</code> thread. This already knows the URL, so does not
         * need to be
         * <code>chain()</code>ed to another
         * <code>Task</code> to operate properly.
         *
         * For most purposes, it is easier to use the convenience method
         * <code>StaticWebCache.getAsync()</code> to chain your Task after the
         * getAsync operation before
         * <code>fork()</code>ing the
         * <code>Task</code> for background execution.
         *
         * @param key
         * @param postMessage
         */
        public GetWebTask(final String key, final int priority, final byte[] postMessage) {
            super(key);

            this.priority = priority;
            this.postMessage = postMessage;
        }

        protected Object exec(final Object in) {
            //#debug
            L.i("Async StaticCache web get", (String) in);
            Object out = in;

            try {
                final HttpGetter httpGetter = httpTaskFactory.getHttpTask((String) in, postMessage);

                if (httpGetter == null) {
                    //#debug
                    L.i("StaticWebCache.HttpTaskFactory returned null", "This is a normal signal that 'we don't need this queued HTTP operation anymore, so cancel");
                    cancel(false, "StaticWebCache.GetWebTask received null HttpGetter from HttpTaskFactory which is a normal signal for 'do not get anymore': " + in);
                } else {
                    //#debug
                    L.i("StaticWebCache.HttpTaskFactory returned", httpGetter.toString());
                    out = httpGetter.fork().get();
                    if (httpGetter.getStatus() == Task.CANCELED) {
                        cancel(false, "StaticWebCache.GetWebTask HttpGetter was canceled or error: " + httpGetter);
                        return out;
                    } else {
                        if (!httpTaskFactory.validateHttpResponse(httpGetter.getResponseCode(), httpGetter.getResponseHeaders(), (byte[]) out)) {
                            //#debug
                            L.i("staticwebcache task factory rejected server response", httpGetter.toString());
                            httpGetter.cancel(false, "StaticWebCache.GetWebTask failed HttpTaskFactory validation");
                        }
                        if (out != null) {
                            try {
                                out = putAsync((String) in, (byte[]) out); // Convert to use form
                            } catch (Exception e) {
                                //#debug
                                L.e("Can not set result after staticwebcache http get", httpGetter.toString(), e);
                                cancel(false, "Can not set result after staticwebcache http get: " + httpGetter.toString() + " : " + e);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                //#debug
                L.e("Can not async StaticCache web get", "" + in, e);
                cancel(false, "Exception in StaticWebCache.GetWebTask - " + in + " : " + e);
            }

            return out;
        }
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
         * @param key
         * @param postMessage
         * @return
         */
        public HttpGetter getHttpTask(final String key, final byte[] postMessage) {
            if (postMessage == null) {
                //#debug
                L.i("StaticWebCache.HttpTaskFactory is generating a new HttpGetter", key);
                return new HttpGetter(key);
            } else {
                final HttpPoster poster = new HttpPoster(key);
                poster.setMessage(postMessage);

                return poster;
            }
        }

        /**
         * Validate the server response.
         *
         * This is also where you can check, store and otherwise process cookies
         * and custom security tags from the server.
         *
         * @param responseCode
         * @param headers
         * @param bytesReceived
         * @return false to cancel() the HTTP operation
         */
        public boolean validateHttpResponse(final int responseCode, final Hashtable headers, final byte[] bytesReceived) {
            if (responseCode >= HttpGetter.HTTP_500_INTERNAL_SERVER_ERROR) {
                //#debug
                L.i(this.getClass().getName(), "Invalid response code " + responseCode + " received");
                return false; // TODO: Should we throw an exception?
            } else if (responseCode >= HttpGetter.HTTP_400_BAD_REQUEST) {
                return false;
            } else if (responseCode >= HttpGetter.HTTP_300_MULTIPLE_CHOICES) {
                // Redirect. Still valid?
                return true;
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
        return this.priority == priority && this.handler.equals(handler) && this.httpTaskFactory.equals(taskFactory);
    }
}