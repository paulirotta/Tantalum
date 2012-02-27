/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2;

import com.futurice.tantalum2.log.Log;
import java.util.Vector;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

/**
 * A worker thread. Long-running and background tasks are queued and executed
 * here to keep the user interface thread free to be responsive.
 * 
 * @author pahought
 */
public final class Worker implements Runnable {
    private static final Vector q = new Vector();
    private static MIDlet midlet;
    private static Display display;

    /**
     * Initialize the Worker class at the start of your MIDlet.
     * 
     * Generally numberOfWorkers=2 is suggested, but you can increase this
     * later when tuning your application's performance.
     * 
     * @param midlet
     * @param numberOfWorkers 
     */
    public static void init(MIDlet midlet, int numberOfWorkers) {
        Worker.midlet = midlet;
        Worker.display = Display.getDisplay(midlet);
        for (int i = 0; i < numberOfWorkers; i++) {
            new Thread(new Worker(), "Worker" + i).start();
        }
    }

    /**
     * Access the MIDlet associated with this application
     * 
     * @return 
     */
    public static MIDlet getMIDlet() {
        return midlet;
    }

    private Worker() {
    }

    /**
     * Add an object to be executed in the background on the worker thread
     * 
     * @param workable 
     */
    public static void queue(final Workable workable) {
        synchronized (q) {
            q.addElement(workable);
            q.notifyAll();
        }
    }

    /**
     * Add an object to be executed in the foreground on the event dispatch
     * thread. All popular JavaME applications require UI and input events to
     * be called serially only from this one thread.
     * 
     * Note that if you queue too many object on the EDT you risk out of memory
     * and (more commonly) a temporarily unresponsive user interface.
     * 
     * @param runnable 
     */
    public static void queueEDT(final Object runnable) {
        if (runnable instanceof Runnable) {
            Worker.display.callSerially((Runnable) runnable);
        }
    }

    public void run() {
        try {
            Workable workable = null;

            while (true) {
                synchronized (q) {
                    if (q.size() > 0) {
                        workable = (Workable) q.elementAt(0);
                        q.removeElementAt(0);
                    } else {
                        q.wait();
                    }
                }
                if (workable != null && workable.work() && workable instanceof Runnable) {
                    Worker.queueEDT((Runnable) workable);
                }
                workable = null;
            }
        } catch (Throwable t) {
            Log.l.log("Worker error", "", t);
        }
    }
}