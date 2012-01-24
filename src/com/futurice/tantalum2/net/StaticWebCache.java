/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2.net;

import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.Workable;
import com.futurice.tantalum2.Worker;
import com.futurice.tantalum2.rms.CacheGetResult;
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
    private final int timeOutInMins;
    private final Hashtable timestamps = new Hashtable();

    public StaticWebCache(String name, int priority, int timeOutInMins, DataTypeHandler handler) {
        super(name, priority, handler);

        this.timeOutInMins = timeOutInMins;
    }

    public synchronized void put(String url, Object o) {
        super.put(url, o);
        updateTimestamp(url);
    }

    /**
     * Retrieve the object from 1. RAM if available 2. RMS if available 3. WEB
     *
     * @param url
     * @param cacheGetResult
     */
    public synchronized void get(final String url, final CacheGetResult cacheGetResult) {

        // RAM
        if (containsKey(url)) {
            Log.log("Hit in RAM");
            int index = this.accessOrder.indexOf(url);
            this.accessOrder.removeElementAt(index);
            this.accessOrder.addElement(url);
            cacheGetResult.setResult(this.cache.get(url));
            if (cacheGetResult.getResult() != null) {
                Worker.queueEDT(cacheGetResult);
                return;
            }
        }

        // RMS
        final byte[] bytes = getFromRMS(url);
        if (bytes != null) {
            Log.log("Hit in RMS");
            Worker.queue(new Workable() {

                public boolean work() {
                    Object converted = handler.convertToUseForm(bytes);
                    cacheGetResult.setResult(converted);
                    Worker.queueEDT(cacheGetResult);
                    put(url, converted);
                    return true;
                }
            });
            return;
        }

        // NET
        update(url, cacheGetResult);
    }

    /**
     * Retrieve the object from WEB, replacing any existing version when the new
     * version arrives.
     *
     * @param url
     * @param cacheGetResult
     */
    public synchronized void update(final String url, final CacheGetResult cacheGetResult) {
        Worker.queue(new HttpGetter(url, RETRIES, cacheGetResult, handler, this));
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
        String str;
        str = "StaticWebCache " + name + " --- priority: " + priority + " size: " + getSize() + " size (bytes): " + sizeAsBytes + " space: " + this.getSizeOfReservedSpace() + "timeOutInMins: " + timeOutInMins + "\n";
        for (int i = 0; i < accessOrder.size(); i++) {
            str += accessOrder.elementAt(i) + "\n";
        }
        return str;
    }
}