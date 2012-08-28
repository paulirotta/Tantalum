/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3;

import com.futurice.tantalum3.log.Log;
import java.util.Vector;

/**
 * A worker thread. Long-running and background tasks are queued and executed
 * here to keep the user interface thread free to be responsive.
 *
 * @author pahought
 */
public class Worker implements Runnable {

    /*
     * Synchronize on the following object if your processing routine will
     * temporarily need a large amount of memory. Only one such activity can be
     * active at a time
     */
    public static final Object LARGE_MEMORY_MUTEX = new Object();
    /*
     * Genearal fork of tasks to be done by any Worker thread
     */
    private static final Vector q = new Vector();
    private static Worker[] workers;
    /*
     * Higher priority fork of tasks to be done only by this thread, in the
     * exact order they appear in the serialQ. Other threads which don't have
     * such dedicated compute to do will drop back to the more general q
     */
    private final Vector serialQ = new Vector();
    /*
     * serialQ jobs are assigned to Workers in a round-robin fashion using this
     * index. The user can store this index if they want to later add objects
     * to the same serialQ or manually manage this.
     */
    private static int nextSerialQWorkerIndex = 0;
    private static final Vector idleQ = new Vector();
    private static final Vector shutdownQueue = new Vector();
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
    public static void init(final int numberOfWorkers) {
        workers = new Worker[numberOfWorkers];
        createWorker(); // First worker
        Worker.fork(new Task() {
            /**
             * The first worker creates the others in the background
             */
            public boolean compute() {
                int i = 1;

                try {
                    for (; i < numberOfWorkers; i++) {
                        createWorker();
                    }
                } catch (Exception e) {
                    //#debug
                    Log.l.log("Can not create worker", "i=" + i, e);
                }

                return false;
            }
        });
    }

    private static void createWorker() {
        workers[workerCount] = new Worker();
        (new Thread(workers[workerCount], "Worker" + ++workerCount)).start();
    }

    public Worker() {
    }

    /**
     * Add an object to be executed in the background on the worker thread. This
     * well be executed FIFO (First In First Out), but some Worker threads may
     * be occupied with their own serialQueue() tasks which they prioritize over
     * main fork compute.
     *
     * Shutdown() will be delayed indefinitely until items in the fork complete
     * execution. If the shutdown signal comes from the phone (usually because
     * the user pressed the RED button to exit the application), then shutdown
     * will be delayed by a maximum of 3 seconds before forcing exit.
     *
     * @param workable
     */
    public static void fork(final Task workable) {
        synchronized (q) {
            q.addElement(workable);
            q.notify();
        }
    }

    /**
     * Queue compute to the Worker specified by serialQIndex. This compute will be
     * done after any previously serialQueue()d compute to this Worker. This Worker
     * will do only serialQueue() tasks until they are complete, then will
     * revert to doing general fork(), forkPriority() and forkLowPriority()
     *
     * @param workable
     * @param serialQIndex
     */
    public static void fork(final Task workable, final int serialQIndex) {
        if (serialQIndex >= workers.length) {
            throw new IndexOutOfBoundsException("serialQ to Worker " + serialQIndex + ", but there are only " + workers.length + " Workers");
        }
        workers[serialQIndex].serialQ.addElement(workable);
        synchronized (q) {
            /*
             * We must notifyAll to ensure the specified worker is notified. Low cost.
             */
            q.notifyAll();
        }
    }

    public static int nextSerialWorkerIndex() {
        synchronized (q) {
            final int i = nextSerialQWorkerIndex;
            nextSerialQWorkerIndex = ++nextSerialQWorkerIndex % workers.length;

            return i;
        }
    }

    /**
     * Jump an object to the beginning of the fork (LIFO - Last In First Out).
     *
     * Note that this is best used for ensuring that operations holding a lot of
     * memory are finished as soon as possible. If you are relying on this for
     * performance, be warned that multiple calls to this method may still bog
     * the system down.
     *
     * Note also that under the rare circumstance that all Workers are busy with
     * serialQueue() tasks, forkPriority() compute may be delayed. The recommended
     * solution then is to either increase the number of Workers. You may also
     * want to decrease reliance on serialQueue() elsewhere in you your program
     * and make your application logic more parallel.
     *
     * @param workable
     */
    public static void forkPriority(final Task workable) {
        synchronized (q) {
            q.insertElementAt(workable, 0);
            q.notifyAll();
        }
    }

    /**
     * Add an object to be executed at low priority in the background on the
     * worker thread. Execution will only begin when there are no foreground
     * tasks, and only if at least 1 Worker thread is left ready for immediate
     * execution of normal priority Task tasks.
     *
     * Items in the idleQueue will not be executed if shutdown() is called
     * before they begin.
     *
     * @param workable
     */
    public static void forkLowPriority(final Task workable) {
        synchronized (q) {
            idleQ.addElement(workable);
            q.notify();
        }
    }

    /**
     * Add an object to be executed in the background on the worker thread
     *
     * @param workable
     */
    public static void queueShutdownTask(final Task workable) {
        synchronized (q) {
            shutdownQueue.addElement(workable);
            q.notify();
        }
    }

    /**
     * Call MIDlet.notifyDestroyed() after all current queued and shutdown
     * Task tasks are completed. Resources held by the system will be closed
     * and queued compute such as writing to the RMS or file system will complete.
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
                        //#debug
                        Log.l.log("Worker blocked shutdown timeout", "");
                        break;
                    }
                    synchronized (q) {
                        q.wait(timeRemaining);
                    }
                }
            }
        } catch (InterruptedException ex) {
        }
        //#debug
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
     * Main worker loop. Each Worker thread pulls tasks from the common fork.
     *
     * The worker thread exits on uncaught errors or after shutdown() has been
     * called and all pending tasks and shutdown tasks have completed.
     */
    public void run() {
        try {
            Task workable = null;

            while (true) {
                if (serialQ.isEmpty()) {
                    synchronized (q) {
                        if (q.size() > 0) {
                            // Normal compute
                            workable = (Task) q.firstElement();
                            q.removeElementAt(0);
                        } else {
                            if (idleQ.size() > 0 && !shuttingDown && currentlyIdleCount > 0) {
                                // Idle compute, at least 1 thread is left for new normal compute
                                workable = (Task) idleQ.firstElement();
                                idleQ.removeElementAt(0);
                            } else {
                                // Nothing to do
                                ++currentlyIdleCount;
                                if (!shuttingDown || currentlyIdleCount < workerCount) {
                                    // Empty fork, or waiting for other Worker tasks to complete before shutdown tasks start
                                    q.wait();
                                } else if (!shutdownQueue.isEmpty()) {
                                    // PHASE 1: Execute shutdown actions
                                    workable = (Task) shutdownQueue.firstElement();
                                    shutdownQueue.removeElementAt(0);
                                } else if (currentlyIdleCount >= workerCount) {
                                    // PHASE 2: Shutdown actions are all complete
                                    //#mdebug
                                    Log.l.log("notifyDestroyed", "");
                                    Log.l.shutdown();
                                    //#enddebug
                                    PlatformUtils.notifyDestroyed();
                                    break;
                                }
                                --currentlyIdleCount;
                            }
                        }
                    }
                } else {
                    workable = (Task) serialQ.firstElement();
                    serialQ.removeElementAt(0);
                }
                try {
                    if (workable != null && workable.compute() && workable instanceof Runnable) {
                        PlatformUtils.runOnUiThread((Runnable) workable);
                    }
                } catch (Exception e) {
                    //#debug
                    Log.l.log("Uncaught worker error", "workers=" + workerCount, e);
                }
                workable = null;
            }
        } catch (Throwable t) {
            //#debug
            Log.l.log("Worker error", "", t);
        } finally {
            --workerCount;
            //#debug
            Log.l.log("Worker stop", "workerCount=" + workerCount);
        }
    }
}