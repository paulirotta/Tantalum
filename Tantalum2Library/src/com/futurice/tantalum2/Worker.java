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

    /*
     * Synchronize on the following object if your processing routine will
     * temporarily need a large amount of memory. Only one such activity
     * can be active at a time
     */
    public static final Object LARGE_MEMORY_MUTEX = new Object();

    private static final Vector q = new Vector();
    private static final Vector idleQ = new Vector();
    private static final Vector shutdownQueue = new Vector();
    private static MIDlet midlet;
    private static Display display;
    private static volatile int workerCount = 0;
    private static int currentlyIdleCount = 0;
    private static boolean shuttingDown = false;

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
     * Add an object to be executed in the background on the worker thread.
     *
     * Shutdown() will be delayed indefinitely until items in the queue complete
     * execution. If the shutdown signal comes from the phone (usually because
     * the user pressed the RED button to exit the application), then shutdown
     * will be delayed by only a maximum of 3 seconds.
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
     * Add an object to be executed at low priority in the background on the
     * worker thread. Execution will only begin when there are no foreground
     * tasks, and only if at least 1 Worker thread is left ready for immediate
     * execution of normal priority Workable tasks.
     *
     * Items in the idleQueue will not be executed if shutdown() is called
     * before they begin.
     *
     * @param workable
     */
    public static void queueIdleWork(final Workable workable) {
        synchronized (q) {
            idleQ.addElement(workable);
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

    /**
     * Call MIDlet.notifyDestroyed() after all current queued and shutdown
     * Workable tasks are completed. Resources held by the system will be closed
     * and queued work such as writing to the RMS or file system will complete.
     *
     * @param block Block the calling thread up to three seconds to allow
     * orderly shutdown. This is only needed in MIDlet.notifyDestroyed(true)
     * which is called for example by the user pressing the red HANGUP button.
     */
    public static void shutdown(final boolean block) {
        try {
            synchronized (q) {
                shuttingDown = true;
                q.notifyAll();
            }
            if (block) {
                final long shutdownTimeout = System.currentTimeMillis() + 3000;
                long timeRemaining;

                while (workerCount > 0) {
                    timeRemaining = shutdownTimeout - System.currentTimeMillis();
                    if (timeRemaining <= 0) {
                        Log.l.log("Blocked shutdown timeout", null);
                        break;
                    }
                    synchronized (q) {
                        q.wait(timeRemaining);
                    }
                }
            }
        } catch (InterruptedException ex) {
        }
        Log.l.log("Shutdown exit", "workers=" + workerCount);
    }

    /**
     * For unit testing
     *
     * @return
     */
    static int getNumberOfWorkers() {
        return workerCount;
    }

    /**
     * Main worker loop. Each Worker thread pulls tasks from the common queue.
     *
     * The worker thread exits on uncaught errors or after shutdown() has been
     * called and all pending tasks and shutdown tasks have completed.
     */
    public void run() {
        try {
            Workable workable = null;

            while (true) {
                synchronized (q) {
                    if (q.size() > 0) {
                         // Normal work
                        workable = (Workable) q.elementAt(0);
                        q.removeElementAt(0);
                    } else {
                        if (idleQ.size() > 0 && !shuttingDown && currentlyIdleCount > 0) {
                            // Idle work, at least 1 thread is left for new normal work
                            workable = (Workable) idleQ.elementAt(0);
                            idleQ.removeElementAt(0);
                        } else {
                            // Nothing to do
                            ++currentlyIdleCount;
                            if (!shuttingDown || currentlyIdleCount < workerCount) {
                                // Empty queue, or waiting for other Worker tasks to complete before shutdown tasks start
                                q.wait();
                            } else {
                                // Shutdown
                                while (!shutdownQueue.isEmpty()) {
                                    // PHASE 1: Start shutdown actions
                                    queue((Workable) shutdownQueue.elementAt(0));
                                    shutdownQueue.removeElementAt(0);
                                }
                                if (q.isEmpty() && currentlyIdleCount >= workerCount) {
                                    // PHASE 2: Shutdown actions are all complete
                                    Worker.queueEDT(new Runnable() {

                                        public void run() {
                                            midlet.notifyDestroyed();
                                        }
                                    });
                                    break;
                                }
                            }
                            --currentlyIdleCount;
                        }
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