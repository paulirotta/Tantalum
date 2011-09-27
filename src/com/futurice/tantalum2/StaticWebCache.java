/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2;

import com.futurice.tantalum2.net.HttpGetter;
import com.futurice.tantalum2.rms.CacheGetResult;
import com.futurice.tantalum2.rms.DataTypeHandler;
import com.futurice.tantalum2.rms.StaticCache;
import java.util.Hashtable;

/**
 *
 * @author tsaa
 */
public class StaticWebCache extends StaticCache {

    private static final int RETRIES = 3;
    private int timeOutInMins;
    private Hashtable timestamps = new Hashtable();

    public StaticWebCache(String name, int priority, int timeOutInMins, DataTypeHandler handler) {
        super(name, priority, handler);

        this.timeOutInMins = timeOutInMins;
    }

    public synchronized void put(String url, Object o) {
        super.put(url, o);
        updateTimestamp(url);
    }

    public synchronized void get(final String url, final CacheGetResult cacheGetResult) {
        
        // RAM
        if (containsKey(url)) {
            Log.log("Hit in RAM");
            int index = this.accessOrder.indexOf(url);
            this.accessOrder.removeElementAt(index);
            this.accessOrder.addElement(url);
            cacheGetResult.setResult(this.cache.get(url));
            Worker.queueEDT(cacheGetResult);
            return;
        }

        // RMS

        //FIXME Error, this is SYNCHRONOUS on the EDT, not on Worker
        // If RMS is busy writing on other thread, this will block
        // calling thread (EDT) possibly for a long time, resulting in
        // the stuttering we saw in the demo
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
        Worker.queue(new HttpGetter(url, RETRIES, cacheGetResult, handler, this));
    }

    public synchronized void update(final String url, final CacheGetResult cacheGetResult) {
        Worker.queue(new HttpGetter(url, RETRIES, cacheGetResult, handler, this));
    }

    public synchronized void remove(String url) {
        super.remove(url);
        if (timestamps.contains(url)) {
            timestamps.remove(url);
        }
    }

    public synchronized void updateTimestamp(String url) {
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