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

    public static final int GET_LOCAL = 0;
    public static final int GET_ANYWHERE = 1;
    public static final int GET_WEB = 2;
    private static final int HTTP_GET_RETRIES = 3;

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
     * @param url - The web service location to HTTP_GET the cacheable data
     * @param task - your extension of Task, UICallbackTask, or AsyncTask
     * @param priority - Worker.HIGH_PRIORITY, Worker.NORMAL_PRIORITY, or Worker.LOW_PRIORITY
     * @param getType - StaticWebCache.GET_ANYWHERE, StaticWebCache.GET_WEB, or StaticWebCache.GET_LOCAL
     * @return a new Task containing the result
     */
    public Task get(final String url, final Task task, final int priority, final int getType) {
        final Task t;

        //#debug
        L.i("StaticWebCache get type " + getType, url);
        switch (getType) {
            case GET_LOCAL:
                t = localGet(url, task, priority);
                break;

            case GET_ANYWHERE:
                //#debug
                L.i("Get anywhere", url);
                t = get(url, task, priority);
                break;

            case GET_WEB:
                t = netGet(url, task, priority);
                break;

            default:
                throw new IllegalArgumentException("StaticWebCache get type not supported: " + getType);
        }

        return t;
    }

    /**
     * Retrieve the object from 1. RAM if available 2. RMS if available 3. WEB
     *
     * @param url
     * @param result
     * @param priority - Default is Worker.NORMAL_PRIORITY
     */
    /**
     * Retrieve one item from the cache with method StaticWebCache.GET_ANYWHERE.
     * This is done in the following order:
     * 1. RAM if available: Task completion is immediate but still asynchronous
     * 2. Local flash storage (RMS) if available: may take several milliseconds,
     * or longer if the Worker task queue is long and you do not specify high priority
     * 3. web:  may take several seconds
     * 
     * The call returns immediately, is executed concurrently
     * on a worker thread, and code in the Task will be executed automatically
     * on a Worker and optionally also the UI thread after the get operation
     * completes.
     * 
     * @param url - The web service location to HTTP_GET the cacheable data
     * @param task - your extension of Task, UICallbackTask, or AsyncTask
     * @param priority - Worker.HIGH_PRIORITY, Worker.NORMAL_PRIORITY, or Worker.LOW_PRIORITY
     * @return a new Task containing the result
     * @return 
     */
    public Task get(final String url, final Task task, final int priority) {
        if (url == null || url.length() == 0) {
            throw new IllegalArgumentException("Trivial StaticWebCache get");
        }

        final Task callback = getCallback(task);
        final Task onNotFoundInCacheTask = new Task() {
            /**
             * Local Cache get returned a result, no need to get it from the
             * network
             *
             */
            public Object doInBackground(final Object in) {
                callback.exec(in);

                return in;
            }

            /**
             * Local Cache get failed to return a result- not cached
             *
             */
            public final boolean cancel(final boolean mayInterruptIfNeeded) {
                //#debug
                L.i("No result from staticcache get, shift to HTTP", url);
                if (mayInterruptIfNeeded) {
                    return super.cancel(mayInterruptIfNeeded);
                }

                final HttpGetter httpGetter = new HttpGetter(url, HTTP_GET_RETRIES) {
                    public Object doInBackground(final Object in) {
                        //#debug
                        L.i("Start StaticWebCache httpGetter doInBackground", url);
                        final byte[] bytes = (byte[]) super.doInBackground(in);

                        if (bytes != null) {
                            try {
                                final Object useForm = put(url, bytes); // Convert to use form
                                callback.exec(useForm);

                                return useForm;
                            } catch (Exception e) {
                                //#debug
                                L.e("Can not set result after staticwebcache http get", url, e);
                                setStatus(EXCEPTION);
                            }
                        }

                        return bytes;
                    }

                    protected void onCancelled() {
                        //#debug
                        L.i("StaticWebCache httpgetter onCancelled()", url);
                        callback.cancel(false);
                    }
                };

                // Continue the HTTP GET attempt immediately on the same Worker thread
                // This avoids possible fork delays
                setResult(httpGetter.exec(url));
                if (httpGetter.getStatus() == EXEC_FINISHED) {
                    setStatus(EXEC_FINISHED);
                } else {
                    super.cancel(false);
                    callback.cancel(false);
                }

                return false;
            }
        };

        //#debug
        L.i("Calling staticcache get with new callback", url);
        super.get(url, onNotFoundInCacheTask, priority);

        return onNotFoundInCacheTask;
    }

    /**
     * Delete any existing version, then retrieve the object from WEB.
     *
     * @param url
     * @param result
     */
    private Task netGet(final String url, final Task task, final int getPriority) {
        if (url == null || url.length() == 0) {
            throw new IllegalArgumentException("Trivial StaticWebCache netGet");
        }

        final Task callback = getCallback(task);
        Worker.fork(new Workable() {
            public Object exec(final Object in) {
                try {
                    remove(url);
                    StaticWebCache.this.get(url, callback, getPriority);
                } catch (Exception e) {
                    //#debug
                    L.e("Can not update", url, e);
                }

                return in;
            }
        }, getPriority);

        return task;
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
                        get(url, null, Worker.LOW_PRIORITY);
                    } catch (Exception e) {
                        //#debug
                        L.e("Can not prefetch", url, e);
                    }

                    return in;
                }
            }, Worker.LOW_PRIORITY);
        }
    }
}