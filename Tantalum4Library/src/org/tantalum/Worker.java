/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum;

import org.tantalum.j2me.J2MEPlatformUtils;
import org.tantalum.util.L;
import java.util.Vector;

/**
 * A worker thread. Long-running and background tasks are queued and executed
 * here to keep the user interface thread free to be responsive.
 *
 * @author pahought
 */
public final class Worker extends Thread {

    public static final int HIGH_PRIORITY = 3;
    public static final int NORMAL_PRIORITY = 2;
    public static final int LOW_PRIORITY = 1;
    /*
     * Synchronize on the following object if your processing routine will
     * temporarily need a large amount of memory. Only one such activity can be
     * active at a time
     */
    public static final Object LARGE_MEMORY_MUTEX = new Object();
    /*
     * Genearal forkSerial of tasks to be done by any Worker thread
     */
    private static final Vector q = new Vector();
    private static Worker[] workers;
    /*
     * Higher priority forkSerial of tasks to be done only by this thread, in the
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
    private static final Vector lowPriorityQ = new Vector();
    private static final Vector shutdownQ = new Vector();
    private static int currentlyIdleCount = 0;
    private static boolean shuttingDown = false;
    private Workable workable = null; // Access only within synchronized(q)

    private Worker(final String name) {
        super(name);
    }

    /**
     * Initialize the Worker class at the start of your MIDlet.
     *
     * Generally numberOfWorkers=2 is suggested, but you can increase this later
     * when tuning your application's performance.
     *
     * @param midlet
     * @param numberOfWorkers
     */
    static void init(final int numberOfWorkers) {
        workers = new Worker[numberOfWorkers];
        createWorker(0); // First worker
        Worker.fork(new Workable() {
            /**
             * The first worker creates the others in the background
             */
            public Object exec(final Object in) {
                int i = 1;

                try {
                    for (; i < numberOfWorkers; i++) {
                        createWorker(i);
                    }
                } catch (Exception e) {
                    //#debug
                    L.e("Can not create worker", "i=" + i, e);
                }

                return in;
            }
        });
    }

    private static void createWorker(final int i) {
        workers[i] = new Worker("Worker" + i);
        workers[i].start();
    }

    public Worker() {
    }

    /**
     * Add an object to be executed in the background on the worker thread. This
     * well be executed FIFO (First In First Out), but some Worker threads may
     * be occupied with their own serialQueue() tasks which they prioritize over
     * main forkSerial compute.
     *
     * Shutdown() will be delayed indefinitely until items in the forkSerial
     * complete execution. If the shutdown signal comes from the phone (usually
     * because the user pressed the RED button to exit the application), then
     * shutdown will be delayed by a maximum of 3 seconds before forcing exit.
     *
     * @param workable
     */
    public static void fork(final Workable workable) {
        synchronized (q) {
            q.addElement(workable);
            try {
                if (workable instanceof Task) {
                    ((Task) workable).notifyTaskForked();
                }
            } catch (IllegalStateException e) {
                //#debug
                L.e("Can not fork", workable.toString(), e);
                q.removeElement(workable);
                throw e;
            }
            q.notify();
        }
    }

    /**
     * Worker.HIGH_PRIORITY : Jump an object to the beginning of the forkSerial
     * (LIFO - Last In First Out).
     *
     * Note that this is best used for ensuring that operations holding a lot of
     * memory are finished as soon as possible. If you are relying on this for
     * performance, be warned that multiple calls to this method may still bog
     * the system down.
     *
     * Note also that under the rare circumstance that all Workers are busy with
     * serialQueue() tasks, forkPriority() compute may be delayed. The
     * recommended solution then is to either increase the number of Workers.
     * You may also want to decrease reliance on serialQueue() elsewhere in you
     * your program and make your application logic more parallel.
     *
     * Worker.LOW_PRIORITY : Add an object to be executed at low priority in the
     * background on the worker thread. Execution will only begin when there are
     * no foreground tasks, and only if at least 1 Worker thread is left ready
     * for immediate execution of normal priority Workable tasks.
     *
     * Items in the idleQueue will not be executed if shutdown() is called
     * before they begin.
     *
     * @param workable
     * @param priority
     */
    public static void fork(final Workable workable, final int priority) {
        switch (priority) {
            case Worker.NORMAL_PRIORITY:
                fork(workable);
                break;
            case Worker.HIGH_PRIORITY:
                synchronized (q) {
                    q.insertElementAt(workable, 0);
                    try {
                        if (workable instanceof Task) {
                            ((Task) workable).notifyTaskForked();
                        }
                    } catch (IllegalStateException e) {
                        //#debug
                        L.e("Can not fork high priority", workable.toString(), e);
                        q.removeElement(workable);
                        throw e;
                    }
                    q.notify();
                }
                break;
            case Worker.LOW_PRIORITY:
                synchronized (q) {
                    lowPriorityQ.addElement(workable);
                    try {
                        if (workable instanceof Task) {
                            ((Task) workable).notifyTaskForked();
                        }
                    } catch (IllegalStateException e) {
                        //#debug
                        L.e("Can not fork low priority", workable.toString(), e);
                        lowPriorityQ.removeElement(workable);
                        throw e;
                    }
                    q.notify();
                }
                break;
            default:
                throw new IllegalArgumentException("Illegal priority '" + priority + "'");
        }
    }

    /**
     * Take an object out of the pending task queue. If the task has already
     * been started, or has not been fork()ed, or has been forkSerial() assigned
     * to a dedicated thread queue, then this will return false.
     *
     * @param workable
     * @return
     */
    public static boolean tryUnfork(final Workable workable) {
        boolean success;

        synchronized (q) {
            //#debug
            L.i("Unfork start", workable.toString());
            success = q.removeElement(workable);
            int i = 0;
            while (!success && i < workers.length) {
                success = workers[i++].serialQ.removeElement(workable);
            }
            //#debug
            L.i("Unfork end", workable + " success=" + success);

            return success;
        }
    }

    /**
     * Stop the specified task if it is currently running
     *
     * @param task
     * @return
     */
    static boolean interruptWorkable(final Task task) {
        if (task == null) {
            throw new IllegalArgumentException("interruptWorkable(null) not allowed");
        }
        synchronized (q) {
            boolean interrupted = false;

            for (int i = 0; i < workers.length; i++) {
                if (task.equals(workers[i].workable)) {
                    //#debug
                    L.i("Sending interrupt signal", "thread=" + workers[i].getName() + " workable=" + task);
                    interrupted = task == workers[i].workable;
                    if (interrupted) {
                        workers[i].interrupt();
                    }
                    break;
                }
            }

            return interrupted;
        }
    }

    /**
     * Queue compute to the Worker specified by serialQIndex. This compute will
     * be done after any previously serialQueue()d compute to this Worker. This
     * Worker will do only serialQueue() tasks until they are complete, then
     * will revert to doing general forkSerial(), forkPriority() and
     * forkLowPriority()
     *
     * @param workable
     * @param serialQIndex
     */
    public static void forkSerial(final Workable workable, final int serialQIndex) {
        if (serialQIndex >= workers.length) {
            throw new IndexOutOfBoundsException("serialQ to Worker " + serialQIndex + ", but there are only " + workers.length + " Workers");
        }
        workers[serialQIndex].serialQ.addElement(workable);
        try {
            if (workable instanceof Task) {
                ((Task) workable).notifyTaskForked();
            }
        } catch (IllegalStateException e) {
            workers[serialQIndex].serialQ.removeElement(workable);
            throw e;
        }
        synchronized (q) {
            /*
             * We must notifyAll to ensure the specified Worker is notified
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
     * Add an object to be executed in the background on the worker thread
     *
     * @param workable
     */
    public static void forkShutdownTask(final Workable workable) {
        synchronized (q) {
            shutdownQ.addElement(workable);
            q.notify();
        }
    }

    /**
     * Call MIDlet.doNotifyDestroyed() after all current queued and shutdown
     * Workable tasks are completed. Resources held by the system will be closed
     * and queued compute such as writing to the RMS or file system will
     * complete.
     *
     * @param block Block the calling thread up to three seconds to allow
     * orderly shutdown. This is only needed in MIDlet.doNotifyDestroyed(true)
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

                final int numWorkersToWaitFor = Thread.currentThread() instanceof Worker ? workers.length - 1 : workers.length;
                while (currentlyIdleCount < numWorkersToWaitFor) {
                    timeRemaining = shutdownTimeout - System.currentTimeMillis();
                    if (timeRemaining <= 0) {
                        //#debug
                        L.i("A worker blocked shutdown timeout", Worker.toStringWorkers());
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
        L.i("Shutdown exit", "currentlyIdleCount=" + currentlyIdleCount);
    }

    /**
     * For unit testing
     *
     * @return
     */
    static int getNumberOfWorkers() {
        return workers.length;
    }

    private static String toStringWorkers() {
        final StringBuffer sb = new StringBuffer();

        sb.append("WORKERS: currentlyIdleCount=");
        sb.append(Worker.currentlyIdleCount);
        sb.append(" q.size()=");
        sb.append(Worker.q.size());
        sb.append(" shutdownQ.size()=");
        sb.append(Worker.shutdownQ.size());
        sb.append(" lowPriorityQ.size()=");
        sb.append(Worker.lowPriorityQ.size());

        for (int i = 0; i < workers.length; i++) {
            final Worker w = workers[i];
            sb.append(" [");
            sb.append(w.getName());
            sb.append(" serialQsize=");
            sb.append(w.serialQ.size());
            sb.append(" currentWorkable=");
            sb.append(w.workable);
            sb.append("] ");
        }

        return sb.toString();
    }

    /**
     * Main worker loop. Each Worker thread pulls tasks from the common
     * forkSerial.
     *
     * The worker thread exits on uncaught errors or after shutdown() has been
     * called and all pending tasks and shutdown tasks have completed.
     */
    public void run() {
        try {
            while (true) {
                try {
                    final Workable w;
                    synchronized (q) {
                        workable = null;
                        if (serialQ.isEmpty()) {
                            if (q.size() > 0) {
                                // Normal compute
                                workable = (Workable) q.firstElement();
                                q.removeElementAt(0);
                            } else {
                                if (shuttingDown) {
                                    if (!shutdownQ.isEmpty()) {
                                        // SHUTDOWN PHASE 1: Execute shutdown actions
                                        workable = (Workable) shutdownQ.firstElement();
                                        shutdownQ.removeElementAt(0);
                                    } else if (currentlyIdleCount < workers.length - 1) {
                                        // Nothing more to do, but other threads are still finishing last tasks
                                        ++currentlyIdleCount;
                                        q.wait();
                                        --currentlyIdleCount;
                                    } else {
                                        // PHASE 2: Shutdown actions are all complete
                                        J2MEPlatformUtils.notifyDestroyed("currentlyIdleCount=" + currentlyIdleCount);
                                        //#mdebug
                                        L.i("Log notifyDestroyed", "");
                                        L.shutdown();
                                        //#enddebug
                                        shuttingDown = false;
                                    }
                                } else if (currentlyIdleCount >= workers.length && lowPriorityQ.size() > 0) {
                                    // Idle compute, at least half available threads in the pool are left for new normal priority tasks
                                    workable = (Workable) lowPriorityQ.firstElement();
                                    lowPriorityQ.removeElementAt(0);
                                } else {
                                    ++currentlyIdleCount;
                                    q.wait();
                                    --currentlyIdleCount;
                                }
                            }
                        } else {
                            workable = (Workable) serialQ.firstElement();
                            serialQ.removeElementAt(0);
                        }
                        w = workable;
                    }
                    if (w != null) {
                        if (w instanceof Task) {
                            final Task t = (Task) w;
                            if (t.getStatus() < Task.CANCELED) {
                                w.exec(t.getValue()); // Pass in argument
                            } else {
                                //#debug
                                L.i("Worker will not execute canceled task", t.toString());
                            }
                        } else {
                            w.exec(null);
                        }
                    }
                } catch (InterruptedException e) {
                    //#debug
                    L.i("Worker interrupted", "workable=" + workable);
                } catch (Exception e) {
                    //#debug
                    L.e("Uncaught worker error", "workable=" + workable, e);
                }
            }
        } catch (Throwable t) {
            //#debug
            L.e("Fatal worker error", "workable=" + workable, t);
        }
    }
}