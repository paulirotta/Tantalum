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

import java.util.Hashtable;
import org.tantalum.Task;
import org.tantalum.Worker;
import org.tantalum.storage.DataTypeHandler;
import org.tantalum.storage.StaticCache;
import org.tantalum.util.L;

/**
 * A cache of remote http contents backed by local flash memory storage
 *
 * @author tsaa
 */
public class StaticWebCache extends StaticCache {

    private static final HttpTaskFactory defaultHttpGetterFactory = new HttpTaskFactory();
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
     * Create a persistent flash memory cache which acts like a Hashtable
     * 
     * Items not already in the cache will be requested from the web with HTTP GET
     * when you request them by url.
     * 
     * @param priority
     * @param handler 
     */
    public StaticWebCache(final char priority, final DataTypeHandler handler) {
        super(priority, handler);
        
        this.httpTaskFactory = StaticWebCache.defaultHttpGetterFactory;
    }

    /**
     * Create a persistent flash memory cache, and provide your own class for
     * creating the HttpGetter or HttpPoster that will be used to getAsync cache-miss
     * items from the web.
     * 
     * @param priority
     * @param handler
     * @param httpTaskFactory 
     */
    public StaticWebCache(final char priority, final DataTypeHandler handler, final HttpTaskFactory httpTaskFactory) {
        super(priority, handler);
        
        this.httpTaskFactory = httpTaskFactory;
    }
    
    /**
     * Get without the HTTP POST option (HTTP GET only)
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
     * on a Worker and optionally also the UI thread after the getAsync operation
     * completes.
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
     * The getAsync() call returns immediately, is executed concurrently on a worker
     * thread, and code in the Task will be executed automatically on a Worker
     * and optionally also the UI thread if UITask after the getAsync operation
     * completes.
     *
     * @param key - The web service location to HTTP_GET the cacheable data
     * @param postMessage - Optional HTTP POST body
     * @param priority - Worker.HIGH_PRIORITY, Worker.NORMAL_PRIORITY, or
     * Worker.LOW_PRIORITY
     * @param getType - StaticWebCache.GET_ANYWHERE, StaticWebCache.GET_WEB, or
     * StaticWebCache.GET_LOCAL
     * @param chainedTask - your Task, UITask, or AsyncTask which is executed
     * after the getAsync operation using the data retrieved.
     *
     * @return a new Task containing the result
     */
    public Task getAsync(final String key, final byte[] postMessage, final int priority, final int getType, final Task chainedTask) {
        final Task getTask;

        //#debug
        L.i("StaticWebCache get:" + getType + " : " + chainedTask, key);
        switch (getType) {
            case GET_LOCAL:
                //#debug
                L.i("GET_LOCAL", key);
                getTask = new StaticCache.GetLocalTask(key);
                break;

            case GET_ANYWHERE:
                //#debug
                L.i("GET_ANYWHERE", key);
                getTask = new StaticWebCache.GetAnywhereTask(key, postMessage);
                break;

            case GET_WEB:
                //#debug
                L.i("GET_WEB", key);
                getTask = new StaticWebCache.GetWebTask(key, postMessage);
                break;

            default:
                throw new IllegalArgumentException("StaticWebCache get type not supported: " + getType);
        }
        getTask.chain(chainedTask);
        Worker.fork(getTask, priority);

        return getTask;
    }

    /**
     * Retrieve the object from WEB if it is not already cached locally.
     *
     * @param key
     */
    public void prefetch(final String key) {
        if (synchronousRAMCacheGet(key) == null) {
            Worker.fork(new Task() {
                public Object doInBackground(final Object in) {
                    try {
                        getAsync(key, Worker.LOW_PRIORITY, StaticWebCache.GET_ANYWHERE, null);
                    } catch (Exception e) {
                        //#debug
                        L.e("Can not prefetch", key, e);
                    }

                    return in;
                }
            }, Worker.LOW_PRIORITY);
        }
    }

    /**
     * This Task performs a StaticWebCache.GET_ANYWHERE operation on a Worker
     * thread in the background.
     *
     * For convenience, you normally call StaticWebCache.getAsync(String url, int
     * priority, int getType, Task task) where getType is
     * StaticWebCache.GET_ANYWHERE and task is some operation you supply which
     * you would like to act on the result of the getAsync(). You can, if you prefer,
     * invoke this class directly to tailor how it operates or set parameters.
     *
     * The pattern for synchronous use when on a background Worker thread is:
     * <pre><code>
     * Object out = new GetAnywhereTask().exec(String url);
     * </code></pre>
     *
     * The pattern for asynchronous use from the UI thread is:
     * <pre><code>
     * UITask uiTask = new UITask() {
     *    protected Object doInBackground(final Object in) {
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
     *       // Do something on the UI Thread with the result from doInBackground() above
     *    }
     * };
     *
     * Task getTask = new GetAnywhereTask(String url);
     * getTask.chain(uiTask);
     * getTask.fork();
     * </code></pre>
     */
    final public class GetAnywhereTask extends Task {
        final byte[] postMessage;

        /**
         * Create a
         * <code>StaticWebCache.GET_ANYWHERE</code> actor which will execute on
         * the
         * <code>Worker</code> thread using as input the
         * <code>chain()</code>ed output from a previous
         * <code>Task</code>.
         *
         * For most purposes, it is easier to use the convenience method
         * <code>StaticWebCache.getAsync()</code> to chain your
         * <code>Task</code> after the getAsync operation before
         * <code>fork()</code>ing the Task for background execution.
         */
        public GetAnywhereTask() {
            super();
            this.postMessage = null;
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
         * <code>StaticWebCache.getAsync()</code> to chain your Task after the getAsync
         * operation before
         * <code>fork()</code>ing the Task for background execution.
         *
         * @param key
         * @param postMessage 
         */
        public GetAnywhereTask(final String key, final byte[] postMessage) {
            super(key);
            this.postMessage = postMessage;
        }

        protected Object doInBackground(final Object in) {
            Object out = in;

            if (in == null || !(in instanceof String)) {
                //#debug
                L.i("ERROR", "StaticWebCache.GetAnywhereTask must receive a String url, but got " + in == null ? "null" : in.toString());
                cancel(false);
            } else {
                //#debug
                L.i("Async StaticCache.GET_ANYWHERE get", (String) in);
                try {
                    out = synchronousGet((String) in);
                    if (out == null) {
                        //#debug
                        L.i("StaticWebCache: not found locally, get from the web", (String) in);
                        out = new GetWebTask((String) in, postMessage).exec(in);
                    }
                } catch (Exception e) {
                    //#debug
                    L.e("Can not get from StaticWebCache", (String) in, e);
                }
            }
            //#debug
            L.i("End GetAnywhereTask " + in.toString(), out.toString());

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
     * <code>getAsync()</code>. You can, if you prefer, invoke this class directly to
     * tailor how it operates or set parameters.
     *
     * The pattern for synchronous use when on a background Worker thread is:
     * <pre><code>
     * Object out = new GetWebTask().exec(String url);
     * </code></pre>
     *
     * The pattern for asynchronous use from the UI thread is:
     * <pre><code>
     * UITask uiTask = new UITask() {
     *    protected Object doInBackground(final Object in) {
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
     *       // Do something on the UI Thread with the result from doInBackground() above
     *    }
     * };
     *
     * Task getTask = new GetWebTask(String url);
     * getTask.chain(uiTask);
     * getTask.fork();
     */
    final public class GetWebTask extends Task {
        final byte[] postMessage;
        
        /**
         * Create a
         * <code>StaticWebCache.GET_WEB</code> actor which will execute on the
         * Worker thread using as input the chained output from a previous Task.
         *
         * For most purposes, it is easier to use the convenience method
         * <code>StaticWebCache.getAsync()</code> to chain your Task after the getAsync
         * operation before
         * <code>fork()</code>ing the Task for background execution.
         * 
         * @param postMessage 
         */
        public GetWebTask(final byte[] postMessage) {
            super();
            
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
         * <code>StaticWebCache.getAsync()</code> to chain your Task after the getAsync
         * operation before
         * <code>fork()</code>ing the
         * <code>Task</code> for background execution.
         *
         * @param key
         * @param postMessage 
         */
        public GetWebTask(final String key, final byte[] postMessage) {
            super(key);
            
            this.postMessage = postMessage;
        }

        protected Object doInBackground(final Object in) {
            //#debug
            L.i("Async StaticCache web get", (String) in);
            Object out = in;

            try {
                final HttpGetter httpGetter = httpTaskFactory.getHttpTask((String) in, postMessage);
                out = httpGetter.exec(null);
                if (!httpTaskFactory.checkHttpResponse(httpGetter.getResponseCode(), httpGetter.getResponseHeaders())) {
                    //#debug
                    L.i("staticwebcache task factory rejected server response", httpGetter.toString());
                    httpGetter.cancel(false);
                }
                if (httpGetter.getStatus() == Task.CANCELED || httpGetter.getStatus() == Task.EXCEPTION) {
                    this.cancel(false);
                    return out;
                } else {
                    if (out != null) {
                        try {
                            out = putAsync((String) in, (byte[]) out); // Convert to use form
                        } catch (Exception e) {
                            //#debug
                            L.e("Can not set result after staticwebcache http get", httpGetter.toString(), e);
                            setStatus(EXCEPTION);
                        }
                    }
                }
            } catch (Exception e) {
                //#debug
                L.e("Can not async StaticCache web get", in.toString(), e);
                this.cancel(false);
            }

            return out;
        }
    }

    /**
     * If you override this default implementation, you can add custom header
     * parameters to the HTTP request and act on custom header fields such as
     * cookies in the HTTP response.
     *
     */
    public static class HttpTaskFactory {

        /**
         * Get an HTTP task, possibly with modified headers. Although this is
         * usually an HttpGetter, in could be an HttpPoster.
         *
         * @param key
         * @param postMessage
         * @return 
         */
        public HttpGetter getHttpTask(final String key, final byte[] postMessage) {
            if (postMessage == null) {
                return new HttpGetter(key);
            } else {
                final HttpPoster poster = new HttpPoster(key);
                poster.setMessage(postMessage);
                
                return poster;
            }
        }

        /**
         * Check the servers response.
         * 
         * This is also where you can check, store and otherwise process cookies
         * and custom security tags from the server.
         *
         * @param responseCode
         * @param headers
         * @return false to cancel() the HTTP operation
         */
        public boolean checkHttpResponse(final int responseCode, final Hashtable headers) {
            return true;
        }
    }
}