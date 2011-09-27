/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2;

import java.util.Vector;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

/**
 *
 * @author pahought
 */
public class Worker implements Runnable {

    private static final Vector q = new Vector();
    private static MIDlet midlet;
    private static Display display;

    public static void init(MIDlet midlet, int numberOfWorkers) {
        Worker.midlet = midlet;
        Worker.display = Display.getDisplay(midlet);
        for (int i = 0; i < numberOfWorkers; i++) {
            new Worker(i);
        }
    }
    
    public static final MIDlet getMIDlet() {
        return midlet;
    }

    private Worker(int i) {
        new Thread(this, "Worker" + i).start();
    }

    public static void queue(final Workable workable) {
        synchronized (q) {
            q.addElement(workable);
            q.notifyAll();
        }
    }

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
            Log.logThrowable(t, "Worker error");
        }
    }
}