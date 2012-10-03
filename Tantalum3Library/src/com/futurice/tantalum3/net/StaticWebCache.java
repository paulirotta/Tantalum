/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.net;

import com.futurice.tantalum3.Task;
import com.futurice.tantalum3.Workable;
import com.futurice.tantalum3.Worker;
import com.futurice.tantalum3.log.L;
import com.futurice.tantalum3.rms.DataTypeHandler;
import com.futurice.tantalum3.rms.StaticCache;

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

    public Task get(final String url, final Task callback, final int getType, final int getPriority) {
        final Task task;

        //#debug
        L.i("StaticWebCache get type " + getType, url);
        switch (getType) {
            case GET_LOCAL:
                task = localGet(url, callback, getPriority);
                break;

            case GET_ANYWHERE:
                //#debug
                L.i("Get anywhere", url);
                task = get(url, callback, getPriority);
                break;

            case GET_WEB:
                task = netGet(url, callback, getPriority);
                break;

            default:
                throw new IllegalArgumentException("StaticWebCache get type not supported: " + getType);
        }

        return task;
    }

    /**
     * Retrieve the object from 1. RAM if available 2. RMS if available 3. WEB
     *
     * @param url
     * @param result
     * @param priority - Default is Worker.NORMAL_PRIORITY
     */
    public Task get(final String url, final Task task, final int getPriority) {
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
        super.get(url, onNotFoundInCacheTask, getPriority);

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