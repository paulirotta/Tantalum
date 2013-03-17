/*
 Copyright © 2012 Paul Houghton and Futurice on behalf of the Tantalum Project.
 All rights reserved.

 Tantalum software shall be used to make the world a better place for everyone.

 This software is licensed for use under the Apache 2 open source software license,
 http://www.apache.org/licenses/LICENSE-2.0.html

 You are kindly requested to return your improvements to this library to the
 open source community at http://projects.developer.nokia.com/Tantalum

 The above copyright and license notice notice shall be included in all copies
 or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package org.tantalum;

import java.util.Vector;
import org.tantalum.util.L;

/**
 * A generic worker thread. Long-running and background tasks are queued and
 * executed here to keep the user interface thread free to update and respond to
 * incoming user interface events.
 *
 * @author phou
 */
public final class Worker extends Thread {

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
    private Task currentTask = null; // Access only within synchronized(q)

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
        for (int i = 0; i < numberOfWorkers; i++) {
            workers[i] = new Worker("Worker" + i);
            workers[i].start();
        }
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
     * @param task
     */
     private static void fork(final Task task) {
        synchronized (q) {
            q.addElement(task);
            try {
                if (task instanceof Task) {
                    ((Task) task).notifyTaskForked();
                }
            } catch (IllegalStateException e) {
                //#debug
                L.e("Can not fork", task.toString(), e);
                q.removeElement(task);
                throw e;
            }
            q.notify();
        }
    }

    /**
     * Task.HIGH_PRIORITY : Jump an object to the beginning of the forkSerial
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
     * for immediate execution of normal priority Tasks.
     *
     * Items in the idleQueue will not be executed if shutdown() is called
     * before they begin.
     *
     * @param task
     * @param priority
     */
    static void fork(final Task task, final int priority) {
        switch (priority) {
            case Task.NORMAL_PRIORITY:
                fork(task);
                break;
            case Task.HIGH_PRIORITY:
                synchronized (q) {
                    q.insertElementAt(task, 0);
                    try {
                        if (task instanceof Task) {
                            ((Task) task).notifyTaskForked();
                        }
                    } catch (IllegalStateException e) {
                        //#debug
                        L.e("Can not fork high priority", task.toString(), e);
                        q.removeElement(task);
                        throw e;
                    }
                    q.notify();
                }
                break;
            case Task.LOW_PRIORITY:
                synchronized (q) {
                    lowPriorityQ.addElement(task);
                    try {
                        if (task instanceof Task) {
                            ((Task) task).notifyTaskForked();
                        }
                    } catch (IllegalStateException e) {
                        //#debug
                        L.e("Can not fork low priority", task.toString(), e);
                        lowPriorityQ.removeElement(task);
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
     * @param task
     * @return
     */
    static boolean tryUnfork(final Task task) {
        boolean success;

        if (task == null) {
            throw new IllegalArgumentException("Can not tryUnfork(null), probable application logic error");
        }

        synchronized (q) {
            //#debug
            L.i("Unfork start", task.toString());
            success = q.removeElement(task);
            //#debug
            L.i("Unfork continues", "success=" + success + " - " + task.toString());
            int i = 0;
            while (!success && i < workers.length) {
                success = workers[i++].serialQ.removeElement(task);
            }
            //#debug
            L.i("Unfork end", task + " success=" + success);

            return success;
        }
    }

    /**
     * Stop the specified task if it is currently running
     *
     * @param task
     * @return
     */
    static void interruptTask(final Task task) {
        if (task == null) {
            throw new IllegalArgumentException("interruptTask(null) not allowed");
        }
        synchronized (q) {
            final Thread currentThread = Thread.currentThread();

            for (int i = 0; i < workers.length; i++) {
                if (task.equals(workers[i].currentTask)) {
                    if (currentThread == workers[i]) {
                        //#debug
                        L.i("Task attempted hard interrupt, usually cancel(true, ..), in itself", "The task is canceled, but will execute to the end. It if faster and more clear execution if you cancel(false, ..)");
                        break;
                    }
                    //#debug
                    L.i("Sending interrupt signal", "thread=" + workers[i].getName() + " task=" + task);
                    if (task == workers[i].currentTask) {
                        /*
                         * Note that there is no race condition here (risk the
                         * task ends before you interrupt it) because currentTask
                         * is a variable only accessed within a q-synchronized block
                         * and Worker.run() is hardened against stray interrupts
                         */
                        workers[i].interrupt();
                    }
                    break;
                }
            }
        }
    }

    /**
     * Queue compute to the Worker specified by serialQIndex. This compute will
     * be done after any previously serialQueue()d compute to this Worker. This
     * Worker will do only serialQueue() tasks until they are complete, then
     * will revert to doing general forkSerial(), forkPriority() and
     * forkLowPriority()
     *
     * @param task
     * @param serialQIndex
     */
    public static void forkSerial(final Task task, final int serialQIndex) {
        if (serialQIndex >= workers.length) {
            throw new IndexOutOfBoundsException("serialQ to Worker " + serialQIndex + ", but there are only " + workers.length + " Workers");
        }
        workers[serialQIndex].serialQ.addElement(task);
        try {
            if (task instanceof Task) {
                ((Task) task).notifyTaskForked();
            }
        } catch (IllegalStateException e) {
            workers[serialQIndex].serialQ.removeElement(task);
            throw e;
        }
        synchronized (q) {
            /*
             * We must notifyAll to ensure the specified Worker is notified
             */
            q.notifyAll();
        }
    }

    /**
     * Get the id number for the next available Worker thread that can be used
     * to a specific application-define type of Worker.forkSerial() operations.
     * Assuming you have a limited number of types of forkSerial() operations,
     * this round-robin allocation reduces the number of serialized operations
     * assigned to any one generic Worker. Note that since forkSerial() work is
     * done by the specified Worker before general fork() operations, it is
     * higher priority work than a fork() with HIGH_PRIORITY, but only one
     * Worker can execute that type of task.
     *
     * You can use this to guarantee the sequence of execution of a given type
     * of task (such as writing to flash memory or to a server).
     *
     * @return
     */
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
     * @param task
     */
    public static void forkShutdownTask(final Task task) {
        synchronized (q) {
            shutdownQ.addElement(task);
            q.notify();
        }
    }

    /**
     * Call MIDlet.doNotifyDestroyed() after all current queued and shutdown
     * Tasks are completed. Resources held by the system will be closed and
     * queued compute such as writing to the RMS or file system will complete.
     *
     * @param block Block the calling thread up to three seconds to allow
     * orderly shutdown. This is only needed in MIDlet.doNotifyDestroyed(true)
     * which is called for example by the user pressing the red HANGUP button.
     */
    public static void shutdown(final boolean block) {
        try {
            synchronized (q) {
                shuttingDown = true;
                dequeueOrCancelOnShutdown(q);
                q.notifyAll();
            }
            for (int i = 0; i < workers.length; i++) {
                Worker.dequeueOrCancelOnShutdown(workers[i].serialQ);
                final Task t = workers[i].currentTask;
                if (t != null && t.getShutdownBehaviour() == Task.DEQUEUE_OR_CANCEL_ON_SHUTDOWN) {
                    ((Task) t).cancel(true, "Shutdown signal received, hard cancel signal sent");
                }
            }

            if (block) {
                final long shutdownTimeout = System.currentTimeMillis() + 3000;
                long timeRemaining;

                final int numWorkersToWaitFor = Thread.currentThread() instanceof Worker ? workers.length - 1 : workers.length;
                synchronized (q) {
                    while (currentlyIdleCount < numWorkersToWaitFor) {
                        timeRemaining = shutdownTimeout - System.currentTimeMillis();
                        if (timeRemaining <= 0) {
                            //#debug
                            L.i("A worker blocked shutdown timeout", Worker.toStringWorkers());
                            break;
                        }
                        q.wait(timeRemaining);
                    }
                }
            }
        } catch (InterruptedException ex) {
        }
    }

    private static void dequeueOrCancelOnShutdown(final Vector queue) {
        for (int i = queue.size() - 1; i >= 0; i--) {
            final Task t = (Task) queue.elementAt(i);

            switch (t.getShutdownBehaviour()) {
                case Task.EXECUTE_NORMALLY_ON_SHUTDOWN:
                    break;

                case Task.DEQUEUE_OR_CANCEL_ON_SHUTDOWN:
                case Task.DEQUEUE_BUT_LEAVE_RUNNING_IF_ALREADY_STARTED_ON_SHUTDOWN:
                    t.cancel(false, "Shutdown signal received, soft cancel signal sent");
                    queue.removeElementAt(i);
                    break;

                case Task.DEQUEUE_ON_SHUTDOWN:
                    queue.removeElementAt(i);
                    break;
            }
        }
    }

    /**
     * For unit testing
     *
     * @return
     */
    public static int getNumberOfWorkers() {
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
            if (w != null) {
                sb.append(" [");
                sb.append(w.getName());
                sb.append(" serialQsize=");
                sb.append(w.serialQ.size());
                sb.append(" currentTask=");
                sb.append(w.currentTask);
                sb.append("] ");
            }
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
                /*
                 * The following code is Thread-hardened such that Task.cancel(true, "blah")
                 * can generate a Thread.interrupt() at an point below _if_
                 * currentTask is non-null and this Thread will recover and continue
                 * without side-effects such as re-running a canceled Task because
                 * of race a condition.
                 */
                try {
                    final Task t;

                    synchronized (q) {
                        currentTask = null;
                        if (serialQ.isEmpty()) {
                            if (!q.isEmpty()) {
                                // Normal compute, hardened against async interrupt
                                try {
                                    t = (Task) q.firstElement();
                                    currentTask = t;
                                } finally {
                                    // Ensure we don't re-run in case of interrupt
                                    q.removeElementAt(0);
                                }
                            } else {
                                if (shuttingDown) {
                                    if (!shutdownQ.isEmpty()) {
                                        // SHUTDOWN PHASE 1: Execute shutdown actions
                                        t = (Task) shutdownQ.firstElement();
                                        shutdownQ.removeElementAt(0);
                                    } else if (currentlyIdleCount < workers.length - 1) {
                                        // Nothing more to do, but other threads are still finishing last tasks
                                        ++currentlyIdleCount;
                                        q.wait();
                                        t = null;
                                        --currentlyIdleCount;
                                    } else {
                                        // PHASE 2: Shutdown actions are all complete
                                        PlatformUtils.getInstance().notifyDestroyed("currentlyIdleCount=" + currentlyIdleCount);
                                        //#mdebug
                                        L.i("Log notifyDestroyed", "");
                                        L.shutdown();
                                        //#enddebug
                                        t = null;
                                        shuttingDown = false;
                                    }
                                } else if (currentlyIdleCount >= workers.length && lowPriorityQ.size() > 0) {
                                    // Idle compute, at least half available threads in the pool are left for new normal priority tasks
                                    try {
                                        t = (Task) lowPriorityQ.firstElement();
                                        currentTask = t;
                                    } finally {
                                        lowPriorityQ.removeElementAt(0);
                                    }
                                } else {
                                    ++currentlyIdleCount;
                                    q.wait();
                                    --currentlyIdleCount;
                                    t = null;
                                }
                            }
                        } else {
                            try {
                                t = (Task) serialQ.firstElement();
                                currentTask = t;
                            } finally {
                                serialQ.removeElementAt(0);
                            }
                        }
                    }

                    if (t != null) {
                        t.executeTask(null);
                    }
                } catch (InterruptedException e) {
                    synchronized (q) {
                        //#debug
                        L.i("Worker interrupted by call to Task.cancel(true, \"blah\"", "Obscure race conditions can do this, but the code is hardened to deal with it and continue smoothly to the next task. task=" + currentTask);
                    }
                } catch (Exception e) {
                    synchronized (q) {
                        //#debug
                        L.e("Uncaught worker error", "task=" + currentTask, e);
                    }
                }
            }
        } catch (Throwable t) {
            synchronized (q) {
                //#debug
                L.e("Fatal worker error", "task=" + currentTask, t);
            }
        }
        //#debug
        L.i("Thread shutdown", "currentlyIdleCount=" + currentlyIdleCount);
    }

    /**
     * When debugging, show what each Worker is doing and the queue length
     *
     * @return One ore more lines of text depending on the number of active
     * Workers
     */
    public static Vector getCurrentState() {
        final StringBuffer sb = new StringBuffer();
        final int n = Worker.getNumberOfWorkers();
        final Vector lines = new Vector(n + 1);

        sb.append('q');
        sb.append(Worker.q.size());
        sb.append('-');
        for (int i = 0; i < n; i++) {
            final Worker w = workers[i];
            if (w != null) {
                final Task task = w.currentTask;
                sb.append(task != null ? "T" : "t");
                sb.append(i);
                sb.append(':');
                if (task != null) {
                    lines.addElement(trimmedNameNoPackage(task.getClass().getName()));
                }
            }
        }
        lines.insertElementAt(sb.toString(), 0);

        return lines;
    }

    private static String trimmedNameNoPackage(String className) {
        final int i = className.lastIndexOf('.');

        if (i >= 0) {
            className = className.substring(i + 1);
        }

        return className;
    }
}