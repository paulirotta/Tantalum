/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2.net;

import com.futurice.tantalum2.DefaultResult;
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
    private final Hashtable timestamps = new Hashtable();

    public StaticWebCache(final String name, final char priority, final DataTypeHandler handler) {
        super(name, priority, handler);
    }

    /**
     * Retrieve the object from 1. RAM if available 2. RMS if available 3. WEB
     *
     * @param url
     * @param result
     */
    public void get(final String url, final Result result) {
        super.get(url, result.prepend(new DefaultResult() {

            public void noResult() {
                super.noResult();

                update(url, result);
            }
        }));
//        Worker.queue(new Workable() {
//
//            public boolean work() {
//                final Object o = StaticWebCache.super.synchronousRAMCacheGet(url);
//
//                if (o != null) {
//                    result.setResult(o, true);
//                    return true;
//                }
//
//                // NET
//                update(url, result);
//
//                return false;
//            }
//            
//        });
    }

    /**
     * Retrieve the object from WEB, replacing any existing version when the new
     * version arrives.
     *
     * @param url
     * @param result
     */
    public void update(final String url, final Result result) {
        Worker.queue(new HttpGetter(url, RETRIES, result.prepend(new DefaultResult() {

            public void setResult(Object o, final boolean queueToEDTnow) {
                // Convert the result to use form immediately
                o = put(url, (byte[]) o);

                super.setResult(o, queueToEDTnow);
            }
        })));
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

    public synchronized String toString() {
        String str = "StaticWebCache " + name + " --- priority: " + priority + " size: " + getSize() + " size (bytes): " + sizeAsBytes + " space: " + this.getSizeOfReservedSpace() + "\n";
        for (int i = 0; i < accessOrder.size(); i++) {
            str += accessOrder.elementAt(i) + "\n";
        }

        return str;
    }
}