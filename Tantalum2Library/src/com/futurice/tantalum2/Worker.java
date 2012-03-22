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
    private static final Vector shutdownQueue = new Vector();
    private static MIDlet midlet;
    private static Display display;
    private static volatile int workerCount = 0;
    private static int currentlyIdleCount = 0;
    private static boolean shuttingDown = false;
    private static Runnable shutdownCompleteRunnable = null;

    /**
     * Initialize the Worker class at the start of your MIDlet.
     *
     * Generally numberOfWorkers=2 is suggested, but you can increase this later
     * when tuning your application's performance.
     *
     * @param midlet
     * @param numberOfWorkers
     */
    public static void init(final MIDlet midlet, final int numberOfWorkers) {
        Worker.midlet = midlet;
        Worker.display = Display.getDisplay(midlet);
        createWorker(); // First worker
        Worker.queue(new Workable() {

            /**
             * The first worker creates the others in the background
             */
            public boolean work() {
                for (int i = 1; i < numberOfWorkers; i++) {
                    createWorker();
                }

                return false;
            }
        });
    }

    private static void createWorker() {
        new Thread(new Worker(), "Worker" + ++workerCount).start();
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
     * Add an object to be executed in the background on the worker thread
     *
     * @param workable
     */
    public static void queueShutdownTask(final Workable workable) {
        synchronized (q) {
            shutdownQueue.addElement(workable);
            q.notifyAll();
        }
    }

    /**
     * Add an object to be executed in the foreground on the event dispatch
     * thread. All popular JavaME applications require UI and input events to be
     * called serially only from this one thread.
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

    public static void shutdown(final Runnable shutdownCompleteRunnable) {
        synchronized (q) {
            shuttingDown = true;
            Worker.shutdownCompleteRunnable = shutdownCompleteRunnable;
            q.notifyAll();
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
                        ++currentlyIdleCount;
                        if (!shuttingDown || currentlyIdleCount < workerCount) {
                            // Empty queue, or waiting for other Worker tasks to complete before shutdown tasks start
                            q.wait();
                        } else {
                            while (!shutdownQueue.isEmpty()) {
                                // Start shutdown actions
                                queue((Workable) shutdownQueue.elementAt(0));
                                shutdownQueue.removeElementAt(0);
                            }
                            if (q.isEmpty() && currentlyIdleCount == workerCount) {
                                // Shutdown actions all complete
                                --currentlyIdleCount;
                                Worker.queueEDT(shutdownCompleteRunnable);
                                break;
                            }
                        }
                        --currentlyIdleCount;
                    }
                }
                if (workable != null && workable.work() && workable instanceof Runnable) {
                    Worker.queueEDT((Runnable) workable);
                }
                workable = null;
            }
        } catch (Throwable t) {
            Log.l.log("Worker error", "", t);
        } finally {
            --workerCount;
            Log.l.log("Worker stop", "workerCount=" + workerCount);
        }
    }
}