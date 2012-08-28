/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.net;

import com.futurice.tantalum3.Result;
import com.futurice.tantalum3.Task;
import com.futurice.tantalum3.Worker;
import com.futurice.tantalum3.log.Log;
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
    public void get(final String url, final Result result, final boolean highPriority) {
        super.get(url, new Result() {

            /**
             * Local Cache get returned a result, no need to get it from the
             * network
             *
             */
            @Override
            public void setResult(final Object o) {
                if (result != null) {
                    result.setResult(o);
                }
            }

            /**
             * Local Cache get failed to return a result- not cached
             *
             */
            @Override
            public void noResult() {
                Log.l.log("No result from cache get, shift to HTTP", url);
                final HttpGetter httpGetter = new HttpGetter(url, HTTP_GET_RETRIES, new Result() {

                    @Override
                    public void setResult(Object o) {
                        try {
                            o = put(url, (byte[]) o); // Convert to use form
                            if (result != null) {
                                if (o != null) {
                                    result.setResult(o);
                                    //#debug
                                    Log.l.log("END SAVE: After no result from cache get, shift to HTTP", url);
                                } else {
                                    result.noResult();
                                }
                            }
                        } catch (Exception e) {
                            Log.l.log("Can not set result", url, e);
                            noResult();
                        }
                    }

                    @Override
                    public void noResult() {
                        if (result != null) {
                            result.noResult();
                        }
                    }
                });

                // Continue the HTTP GET attempt immediately on the same Worker thread
                // This avoids possible queue delays
                //httpGetter.compute();
                
                Worker.queue(httpGetter);
            }
        }, highPriority);
        
    }

    /**
     * Delete any existing version, then retrieve the object from WEB.
     *
     * @param url
     * @param result
     */
    public void update(final String url, final Result result) {
        Worker.queuePriority(new Task() {

            @Override
            public boolean compute() {
                try {
                    remove(url);
                    get(url, result, true);
                } catch (Exception e) {
                    Log.l.log("Can not update", url, e);
                }

                return false;
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
            Worker.queueIdleWork(new Task() {

                @Override
                public boolean compute() {
                    try {
                        get(url, null, false);
                    } catch (Exception e) {
                        Log.l.log("Can not prefetch", url, e);
                    }

                    return false;
                }
            });
        }
    }
}