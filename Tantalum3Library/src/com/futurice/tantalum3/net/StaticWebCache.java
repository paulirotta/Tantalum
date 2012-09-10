/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.net;

import com.futurice.tantalum3.DelegateTask;
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
     * @param priority - Default is Worker.NORMAL_PRIORITY
     */
    public void get(final String url, final DelegateTask r) {
        super.get(url, new DelegateTask() {

            /**
             * Local Cache get returned a result, no need to get it from the
             * network
             *
             */
            public void set(final Object o) {
                if (r != null) {
                    r.set(o);
                }
            }

            /**
             * Local Cache get failed to return a result- not cached
             *
             */
            public boolean cancel(final boolean mayInterruptIfNeeded) {
                //#debug
                L.i("No result from cache get, shift to HTTP", url);
                final HttpGetter httpGetter = new HttpGetter(url, HTTP_GET_RETRIES, new DelegateTask() {

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
                            //#debug
                            L.e("Can not set result", url, e);
                            cancel(false);
                        }
                    }

                    public boolean cancel(boolean mayInterruptIfNeeded) {
                        if (r != null) {
                            return r.cancel(false);
                        }
                        
                        return false;
                    }
                });

                // Continue the HTTP GET attempt immediately on the same Worker thread
                // This avoids possible fork delays
                httpGetter.exec();
  
                //TODO FIXME is this correct?
                return false;
//                return super.cancel(mayInterruptIfNeeded);
            }
        });
    }

    /**
     * Delete any existing version, then retrieve the object from WEB.
     *
     * @param url
     * @param result
     */
    public void update(final String url, final DelegateTask result) {
        Worker.fork(new Workable() {

            public void exec() {
                try {
                    remove(url);
                    get(url, result);
                } catch (Exception e) {
                    //#debug
                    L.e("Can not update", url, e);
                }
            }
        }, Worker.HIGH_PRIORITY);
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

                public void exec() {
                    try {
                        get(url, null);
                    } catch (Exception e) {
                        //#debug
                        L.e("Can not prefetch", url, e);
                    }
                }
            }, Worker.LOW_PRIORITY);
        }
    }
}