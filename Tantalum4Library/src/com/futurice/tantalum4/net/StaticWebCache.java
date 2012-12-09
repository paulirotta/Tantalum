/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum4.net;

import com.futurice.tantalum4.Task;
import com.futurice.tantalum4.Workable;
import com.futurice.tantalum4.Worker;
import com.futurice.tantalum4.log.L;
import com.futurice.tantalum4.storage.DataTypeHandler;
import com.futurice.tantalum4.storage.StaticCache;

/**
 * A cache of remote http contents backed by local flash memory storage
 *
 * @author tsaa
 */
public class StaticWebCache extends StaticCache {

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

    public StaticWebCache(final char priority, final DataTypeHandler handler) {
        super(priority, handler);
    }

    /**
     * Retrieve one item from the cache using the method specified in the
     * getType parameter. The call returns immediately, is executed concurrently
     * on a worker thread, and code in the Task will be executed automatically
     * on a Worker and optionally also the UI thread after the get operation
     * completes.
     *
     * Normal use case is to specify the method StaticWebCache.GET_ANYWHERE.
     * This attempts get in the following order:
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
     * The get() call returns immediately, is executed concurrently on a worker
     * thread, and code in the Task will be executed automatically on a Worker
     * and optionally also the UI thread if UITask after the get operation
     * completes.
     *
     * @param url - The web service location to HTTP_GET the cacheable data
     * @param priority - Worker.HIGH_PRIORITY, Worker.NORMAL_PRIORITY, or
     * Worker.LOW_PRIORITY
     * @param getType - StaticWebCache.GET_ANYWHERE, StaticWebCache.GET_WEB, or
     * StaticWebCache.GET_LOCAL
     * @param chainedTask - your Task, UITask, or AsyncTask which is executed
     * after the get operation using the data retrieved.
     *
     * @return a new Task containing the result
     */
    public Task get(final String url, final int priority, final int getType, final Task chainedTask) {
        final Task t;

        //#debug
        L.i("StaticWebCache get:" + getType + " : " + chainedTask, url);
        switch (getType) {
            case GET_LOCAL:
                //#debug
                L.i("GET_LOCAL", url);
                t = new StaticCache.GetLocalTask(url);
                break;

            case GET_ANYWHERE:
                //#debug
                L.i("GET_ANYWHERE", url);
                t = new StaticWebCache.GetAnywhereTask(url);
                break;

            case GET_WEB:
                //#debug
                L.i("GET_WEB", url);
                t = new StaticWebCache.GetWebTask(url);
                break;

            default:
                throw new IllegalArgumentException("StaticWebCache get type not supported: " + getType);
        }
        t.chain(chainedTask);
        Worker.fork(t, priority);

        return t;
    }

    /**
     * Retrieve the object from WEB if it is not already cached locally.
     *
     * @param url
     * @param updateResult
     */
    public void prefetch(final String url) {
        if (synchronousRAMCacheGet(url) == null) {
            Worker.fork(new Workable() {
                public Object exec(final Object in) {
                    try {
                        get(url, Worker.LOW_PRIORITY, StaticWebCache.GET_ANYWHERE, null);
                    } catch (Exception e) {
                        //#debug
                        L.e("Can not prefetch", url, e);
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
     * For convenience, you normally call StaticWebCache.get(String url, int
     * priority, int getType, Task task) where getType is
     * StaticWebCache.GET_ANYWHERE and task is some operation you supply which
     * you would like to act on the result of the get(). You can, if you prefer,
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

        /**
         * Create a
         * <code>StaticWebCache.GET_ANYWHERE</code> actor which will execute on
         * the
         * <code>Worker</code> thread using as input the
         * <code>chain()</code>ed output from a previous
         * <code>Task</code>.
         *
         * For most purposes, it is easier to use the convenience method
         * <code>StaticWebCache.get()</code> to chain your
         * <code>Task</code> after the get operation before
         * <code>fork()</code>ing the Task for background execution.
         */
        public GetAnywhereTask() {
            super();
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
         * <code>StaticWebCache.get()</code> to chain your Task after the get
         * operation before
         * <code>fork()</code>ing the Task for background execution.
         *
         * @param url
         */
        public GetAnywhereTask(final String url) {
            super(url);
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
                        out = new GetWebTask().exec(in);
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
     * <code>StaticWebCache.get(String url,
     * int priority, int getType, Task task)</code> where
     * <code>getType</code> is
     * <code>StaticWebCache.GET_WEB</code> and
     * <code>task</code> is some operation you supply which you would like to
     * act on the result of the
     * <code>get()</code>. You can, if you prefer, invoke this class directly to
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

        /**
         * Create a
         * <code>StaticWebCache.GET_WEB</code> actor which will execute on the
         * Worker thread using as input the chained output from a previous Task.
         *
         * For most purposes, it is easier to use the convenience method
         * <code>StaticWebCache.get()</code> to chain your Task after the get
         * operation before
         * <code>fork()</code>ing the Task for background execution.
         */
        public GetWebTask() {
            super();
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
         * <code>StaticWebCache.get()</code> to chain your Task after the get
         * operation before
         * <code>fork()</code>ing the
         * <code>Task</code> for background execution.
         *
         * @param url
         */
        public GetWebTask(final String url) {
            super(url);
        }

        protected Object doInBackground(Object in) {
            //#debug
            L.i("Async StaticCache web get", (String) in);
            final String url = (String) in;
            try {
                Task t = new HttpGetter();
                in = t.exec(in);
                if (t.getStatus() == Task.CANCELED || t.getStatus() == Task.EXCEPTION) {
                    this.cancel(false);
                    return in;
                } else {
                    byte[] bytes = (byte[]) in;
                    if (bytes != null) {
                        try {
                            in = putAsync(url, bytes); // Convert to use form
                        } catch (Exception e) {
                            //#debug
                            L.e("Can not set result after staticwebcache http get", url, e);
                            setStatus(EXCEPTION);
                        }
                    }
                }
            } catch (Exception e) {
                //#debug
                L.e("Can not async StaticCache web get", in.toString(), e);
                this.cancel(false);
            }

            return in;
        }
    }
}