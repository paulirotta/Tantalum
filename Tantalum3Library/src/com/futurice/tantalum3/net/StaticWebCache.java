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
     * @param priority - Default is Worker.NORMAL_PRIORITY
     */
    public void get(final String url, final Task task) {
        super.get(url, new Task() {

            /**
             * Local Cache get returned a result, no need to get it from the
             * network
             *
             */

            public Object doInBackground(final Object in) {
                if (task != null) {
                    task.exec(in);
                }
                
                return in;
            }

            /**
             * Local Cache get failed to return a result- not cached
             *
             */
            public boolean cancel(final boolean mayInterruptIfNeeded) {
                //#debug
                L.i("No result from cache get, shift to HTTP", url);
                if (mayInterruptIfNeeded) {
                    return super.cancel(mayInterruptIfNeeded);
                }
                
                final HttpGetter httpGetter = new HttpGetter(url, HTTP_GET_RETRIES) {

                    public Object doInBackground(final Object in) {
                        final byte[] bytes = (byte[]) super.doInBackground(in);
                        try {
                            final Object useForm = put(url, bytes);
                            return setResult(useForm); // Convert to use form
                        } catch (Exception e) {
                            //#debug
                            L.e("Can not set result", url, e);
                            super.cancel(false);
                        }

                        return bytes;
                    }
                };

                // Continue the HTTP GET attempt immediately on the same Worker thread
                // This avoids possible fork delays
                httpGetter.doInBackground(url);
                if (httpGetter.getStatus() == EXEC_FINISHED) {
                    setStatus(EXEC_FINISHED);
                } else {
                    super.cancel(mayInterruptIfNeeded);
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

            public Object exec(final Object in) {
                try {
                    remove(url);
                    get(url, result);
                } catch (Exception e) {
                    //#debug
                    L.e("Can not update", url, e);
                }
                
                return in;
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

                public Object exec(final Object in) {
                    try {
                        get(url, null);
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