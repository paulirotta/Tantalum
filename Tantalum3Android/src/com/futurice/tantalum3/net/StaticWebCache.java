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

    private static final int HTTP_GET_RETRIES = 3;

    public StaticWebCache(final char priority, final DataTypeHandler handler) {
        super(priority, handler);
    }

    /**
     * Retrieve the object from 1. RAM if available 2. RMS if available 3. WEB
     *
     * @param url
     * @param result
     */
    @Override
    public void get(final String url, final Task r) {
        super.get(url, new Task() {
            /**
             * Local Cache get returned a result, no need to get it from the
             * network
             *
             */
            @Override
            public void set(final Object o) {
                if (r != null) {
                    r.set(o);
                }
            }

            /**
             * Local Cache get failed to return a result- not cached
             *
             */
            @Override
            public boolean cancel(final boolean mayInterruptIfRunning) {
                L.i("No result from cache get, shift to HTTP", url);
                final HttpGetter httpGetter = new HttpGetter(url, HTTP_GET_RETRIES, new Task() {
                    @Override
                    public void set(Object o) {
                        try {
                            o = put(url, (byte[]) o); // Convert to use form
                            if (r != null) {
                                if (o != null) {
                                    r.set(o);
                                    //#debug
                                    L.i("END SAVE: After no result from cache get, shift to HTTP", url);
                                } else {
                                    r.cancel(false);
                                }
                            }
                        } catch (Exception e) {
                            L.e("Can not set result", url, e);
                            cancel(false);
                        }
                    }

//                    @Override
//                    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
//                        return super.cancel(mayInterruptIfRunning);
//                    }
                    
//                    @Override
//                    public void cancel(final boolean mayInterruptIfRunning) {
//                        if (r != null) {
//                            r.cancel(false);
//                        }
//                        super.cancel(mayInterruptIfRunning);
//                    }
                    //TODO FIXME once HttpGetter is a Task
                });
                // super.cancel(mayInterruptIfRunning);

                // Continue the HTTP GET attempt immediately on the same Worker thread
                // This avoids possible forkSerial delays
                //httpGetter.exec();
                httpGetter.exec();
                try {
                    //Worker.fork(httpGetter, RMS_WORKER_INDEX);
                    if (httpGetter.get() == null) {
                        //TODO FIXME Is this correct when re-getting multiple times?
                        return super.cancel(mayInterruptIfRunning);
                    }
                } catch (Exception ex) {
                    L.e("Cache can not re-get from net", url, ex);
                }
                
                return false;
            }
        });

    }

    /**
     * Delete any existing version, then retrieve the object from WEB.
     *
     * @param url
     * @param result
     */
    public void update(final String url, final Task result) {
        Worker.fork(new Workable() {
            @Override
            public void exec() {
                try {
                    remove(url);
                    get(url, result);
                } catch (Exception e) {
                    L.e("Can not update", url, e);
                }
            }
        });
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
                @Override
                public void exec() {
                    try {
                        get(url, null);
                    } catch (Exception e) {
                        L.e("Can not prefetch", url, e);
                    }
                }
            }, Worker.LOW_PRIORITY);
        }
    }
}