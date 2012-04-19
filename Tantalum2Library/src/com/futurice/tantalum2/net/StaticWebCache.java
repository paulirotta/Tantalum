/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2.net;

import com.futurice.tantalum2.Result;
import com.futurice.tantalum2.Worker;
import com.futurice.tantalum2.rms.DataTypeHandler;
import com.futurice.tantalum2.rms.StaticCache;
import java.util.Hashtable;

/**
 * A cache of remote http contents backed by local flash memory storage
 *
 * @author tsaa
 */
public class StaticWebCache extends StaticCache {

    private static final int RETRIES = 3;
    private static final int PREFETCH_RETRIES = 0;
    private final Hashtable timestamps = new Hashtable();

    public StaticWebCache(final char priority, final DataTypeHandler handler) {
        super(priority, handler);
    }

    /**
     * Retrieve the object from 1. RAM if available 2. RMS if available 3. WEB
     *
     * @param url
     * @param getResult
     */
    public void get(final String url, final Result getResult) {
        super.get(url, new Result() {

            /**
             * Local Cache get returned a result, no need to get it from the
             * network
             *
             */
            public void setResult(Object o) {
                if (getResult != null) {
                    getResult.setResult(o);
                }
            }

            /**
             * Local Cache get failed to return a result- not cached
             *
             */
            public void noResult() {
                final HttpGetter httpGetter = new HttpGetter(url, RETRIES, new Result() {

                    public void setResult(final Object o) {
                        // Update the UI immediately
                        if (getResult != null) {
                            getResult.setResult(convertAndPutToHeapCache(url, (byte[]) o));
                        }
                        // Then store to RMS, which might take some time
                        updateTimestamp(url);
                        synchronousPut(url, (byte[]) o, getResult == null);
                    }

                    public void noResult() {
                        getResult.noResult();
                    }
                });

                // Continue the HTTP GET attempt immediately on the same Worker thread
                // This avoids possible queue delays
                httpGetter.work();
            }
        });
    }

    /**
     * Retrieve the object from WEB, replacing any existing version when the new
     * version arrives.
     *
     * @param url
     * @param result
     */
    public void update(final String url, final Result result) {
        Worker.queue(new HttpGetter(url, RETRIES, new Result() {

            public void setResult(Object o) {
                result.setResult(put(url, (byte[]) o));
            }

            public void noResult() {
                result.noResult();
            }
        }));
    }
    
    /**
     * Retrieve the object from WEB if it is not already cached locally.
     *
     * @param url
     * @param updateResult
     */
    public void prefetch(final String url, final boolean forceRefresh) {
        if (forceRefresh || synchronousRAMCacheGet(url) == null) {
            Worker.queueIdleWork(new HttpGetter(url, PREFETCH_RETRIES, new Result() {

                public void setResult(final Object o) {
                    // Then store to RMS, with converted form cached to RAM
                    updateTimestamp(url);
                    synchronousPut(url, (byte[]) o, true);
                }
            }));
        }
    }

    /**
     * Remove the object from the cache
     *
     * @param url
     */
    public synchronized void remove(String url) {
        super.remove(url);

        if (timestamps.contains(url)) {
            timestamps.remove(url);
        }
    }

    /**
     * Update the freshness timestamp associated with the cached object
     *
     * @param url
     */
    public synchronized void updateTimestamp(final String url) {
        timestamps.put(url, new Long(System.currentTimeMillis()));
    }
}