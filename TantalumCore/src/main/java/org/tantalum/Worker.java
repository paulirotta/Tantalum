/*
 Copyright (c) 2013 Nokia Corporation. All rights reserved.
 Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation.
 Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 affiliates. Other product and company names mentioned herein may be trademarks
 or trade names of their respective owners.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
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
final class Worker extends Thread {

    /**
     * The following are state variables. During application shutdown, all tasks
     * from the previous state must complete before the next state is entered.
     */
    private static final int STATE_RUNNING = 0;
    private static final int STATE_WAIT_FOR_FINISH_OR_INTERRUPT_TASKS_THAT_EXPLICITLY_PERMIT_INTERRUPT_BEFORE_STARTING_SHUTDOWN_TASKS = 1;
    private static final int STATE_RUNNING_SHUTDOWN_TASKS = 2;
    /**
     * Serial priority is bumped above HIGH_PRIORITY when the serialQ backlog
     * gets above this number.
     *
     * Usually the seriaQ is used for writing to the file system. We mostly want
     * such writes to be in idle time when there is no other activity in the
     * thread pool. But in a phone where the user activity gives zero idle time
     * for such writes, we need to start doing them before there is a risk of
     * OutOfMemory from serialQ length growth.
     */
    private static final int MAX_SERIAL_Q_LENGTH_BEFORE_PRIORITY_BOOST = 0;
    /*
     * Genearal forkSerial of tasks to be done by any Worker thread
     */
    private static final Vector q = new Vector();
    private static final Vector dedicatedThreads = new Vector();
    private static Worker[] workers;
    /*
     * Higher priority forkSerial of tasks to be done only by this thread, in the
     * exact order they appear in the serialQ. Other threads which don't have
     * such dedicated compute to do will drop back to the more general q
     */
    private final Vector serialQ = new Vector();
    private static final Vector fastlaneQ = new Vector();
    private static final Vector idleQ = new Vector();
    private static final Vector shutdownQ = new Vector();
    private static int currentlyIdleCount = 0;
    private static int runState = STATE_RUNNING;
    private Task currentTask = null; // Access only within synchronized(q)
    private final boolean isDedicatedFastlaneWorker;

    private Worker(final String name, final boolean isDedicatedFastlaneWorker) {
        super(name);

        this.isDedicatedFastlaneWorker = isDedicatedFastlaneWorker;
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
            final boolean fastlane = i == numberOfWorkers - 1;
            final String name = fastlane ? "Fastlane" : "Worker" + i;

            workers[i] = new Worker(name, fastlane);
            workers[i].start();
        }
    }

    private static void forkToUIThread(final Task task) {
        PlatformUtils.getInstance().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    task.executeTask(task.getValue());
                } catch (Exception e) {
                    //#debug
                    L.e(task, "Uncaught Task exception on UI thread", "task=" + task, e);
                }
            }
        });
    }

    private static void forkSerialToSameThread(final Task task) {
        final Thread currentThread = Thread.currentThread();

        if (currentThread instanceof Worker) {
            ((Worker) currentThread).serialQ.addElement(task);
        } else if (currentThread instanceof DedicatedThread) {
            ((DedicatedThread) currentThread).taskRunnable.serialQ.addElement(task);
        } else {
            throw new IllegalArgumentException("You must be in a Task running on a Tantalum Worker thread or DedicatedThread to fork a new Task.SERIAL_CURRENT_THREAD_PRIORITY");
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
     * Worker.IDLE_PRIORITY : Add an object to be executed at low priority in
     * the background on the worker thread. Execution will only begin when there
     * are no foreground tasks, and only if at least 1 Worker thread is left
     * ready for immediate execution of normal priority Tasks.
     *
     * Items in the idleQueue will not be executed if shutdown() is called
     * before they begin.
     *
     * @param task
     * @param priority
     */
    static Task fork(final Task task) {
        if (task.getStatus() != Task.PENDING) {
            L.i(task, "Can not fork() a Task multiple times. Tasks are disposable, create a new instance each time. Task may have been cancel()ed by another thread.", "" + task);

            return task;
        }
        final int priority = task.getForkPriority();
        //#debug
        L.i(task, "Fork", "priority=" + task.getPriorityString());
        synchronized (q) {
            switch (priority) {
                case Task.DEDICATED_THREAD_PRIORITY:
                    final DedicatedThread thread = new DedicatedThread(new DedicatedThread.TaskRunnable(task), task.getClassName());
                    thread.start();
                    break;

                case Task.UI_PRIORITY:
                    forkToUIThread(task);
                    break;

                case Task.FASTLANE_PRIORITY:
                    fastlaneQ.insertElementAt(task, 0);
                    /**
                     * notify() vs notifyAll(): Any thread will do as all
                     * Workers (and nothing else) waits on this lock and will
                     * accept into use Fastlane tasks. If in future these
                     * conditions are not met, this optimization will no longer
                     * work and notifyAll() will be needed
                     */
                    q.notify();
                    break;

                case Task.SERIAL_CURRENT_THREAD_PRIORITY:
                    if (PlatformUtils.getInstance().isUIThread()) {
                        forkToUIThread(task);
                    } else {
                        forkSerialToSameThread(task);
                    }
                    // No need to notifyAll()
                    break;

                case Task.SERIAL_PRIORITY:
                    workers[0].serialQ.addElement(task);
                    q.notifyAll();
                    break;

                case Task.HIGH_PRIORITY:
                    q.insertElementAt(task, 0);
                    q.notifyAll();
                    break;

                case Task.NORMAL_PRIORITY:
                    q.addElement(task);
                    q.notifyAll();
                    break;

                case Task.IDLE_PRIORITY:
                    idleQ.addElement(task);
                    q.notifyAll();
                    break;

                case Task.SHUTDOWN:
                    shutdownQ.addElement(task);
                    q.notifyAll();
                    break;

                default:
                    throw new IllegalArgumentException("Illegal priority '" + priority + "'");
            }

            return task;
        }
    }

    static Task[] fork(final Task[] tasks) {
        synchronized (q) {
            for (int i = 0; i < tasks.length; i++) {
                fork(tasks[i]);
            }

            return tasks;
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

        synchronized (q) {
            success = q.removeElement(task);
            if (!success) {
                success = fastlaneQ.removeElement(task);
            }
            if (!success) {
                success = idleQ.removeElement(task);
            }
        }
        //#debug
        L.i(task, "Unfork", "success=" + success + " task=" + task);

        return success;
    }

    /**
     * Stop the specified task if it is currently running on any Worker
     *
     * @param task
     * @return null, or the thread found running the Task and to which
     * interrupt() was sent
     */
    static Thread interruptTask(final Task task) {
        if (task == null) {
            throw new NullPointerException("interruptTask(null) not allowed");
        }

        final Thread currentThread = Thread.currentThread();

        synchronized (q) {
            for (int i = 0; i < workers.length; i++) {
                if (currentThread == workers[i]) {
                    /**
                     * Never send interrupt to own thread. The task state will
                     * change to canceled, which is enough.
                     */
                    continue;
                }

                if (task.equals(workers[i].currentTask)) {
                    //#debug
                    L.i(task, "cancel() is sending Thread.interrupt()", "thread=" + workers[i].getName() + " task=" + task);
                    /*
                     * Note that there is no race condition here (risk the
                     * task ends before you interrupt it) because currentTask
                     * is a variable only accessed within a q-synchronized block
                     * and Worker.run() is hardened against stray interrupts
                     */
                    workers[i].interrupt();
                    return workers[i];
                }
            }
        }

        synchronized (dedicatedThreads) {
            for (int i = 0; i < dedicatedThreads.size(); i++) {
                final DedicatedThread dedicatedThread = (DedicatedThread) dedicatedThreads.elementAt(i);

                if (currentThread == dedicatedThread) {
                    /**
                     * Never send interrupt to own thread, even if there is a
                     * match. The task state will change.
                     */
                    continue;
                }

                if (task.equals(dedicatedThread.taskRunnable.task)) {
                    //#debug
                    L.i(task, "cancel() is sending Thread.interrupt()", "thread=" + workers[i].getName() + " task=" + task);
                    /*
                     * Note that there is no race condition here (risk the
                     * task ends before you interrupt it) because currentTask
                     * is a variable only accessed within a q-synchronized block
                     * and Worker.run() is hardened against stray interrupts
                     */
                    dedicatedThread.interrupt();
                    return dedicatedThread;
                }
            }
        }

        return null;
    }

    private static Task[] copyOfQueueTasks(final Vector queue) {
        synchronized (queue) {
            final Task[] tasks = new Task[queue.size()];
            queue.copyInto(tasks);

            return tasks;
        }
    }

    /**
     * Call PlatformUtils.getInstance().shutdown() after all current queued and
     * shutdown Tasks are completed. Resources held by the system will be closed
     * and queued compute such as writing to the RMS or file system will
     * complete.
     *
     * @param block Block the calling thread up to three seconds to allow
     * orderly shutdown. This is only needed in shutdown(true) which is called
     * for example by the user pressing the red HANGUP button.
     */
    static void shutdown(final boolean block, final String reason) {
        try {
            /*
             * Removed queued tasks which can be removed
             */
            synchronized (q) {
                runState = STATE_WAIT_FOR_FINISH_OR_INTERRUPT_TASKS_THAT_EXPLICITLY_PERMIT_INTERRUPT_BEFORE_STARTING_SHUTDOWN_TASKS;
                q.notifyAll();
            }
            dequeueOrCancelOnShutdown(fastlaneQ);
            for (int i = 0; i < workers.length; i++) {
                Worker.dequeueOrCancelOnShutdown(workers[i].serialQ);
            }
            dequeueOrCancelOnShutdown(q);
            dequeueOrCancelOnShutdown(idleQ);
            final DedicatedThread[] dt;
            synchronized (dedicatedThreads) {
                dt = new DedicatedThread[dedicatedThreads.size()];
                dedicatedThreads.copyInto(dt);
            }
            for (int i = 0; i < dt.length; i++) {
                Worker.dequeueOrCancelOnShutdown(dt[i].taskRunnable.serialQ);
            }

            /*
             * Interrupt currently running tasks which can be interrupted
             */
            synchronized (q) {
                for (int i = 0; i < workers.length; i++) {
                    final Task t = workers[i].currentTask;
                    if (t != null && t.getShutdownBehaviour() == Task.DEQUEUE_OR_INTERRUPT_ON_SHUTDOWN) {
                        workers[i].interrupt();
                    }
                }
            }

            DedicatedThread dedicatedThread;
            do {
                synchronized (dedicatedThreads) {
                    if (dedicatedThreads.size() > 0) {
                        dedicatedThread = (DedicatedThread) dedicatedThreads.firstElement();
                        dedicatedThreads.removeElementAt(0);
                    } else {
                        dedicatedThread = null;
                    }
                }
                if (dedicatedThread != null) {
                    synchronized (dedicatedThread.taskRunnable.serialQ) {
                        if (dedicatedThread.taskRunnable.task.getShutdownBehaviour() == Task.DEQUEUE_OR_INTERRUPT_ON_SHUTDOWN) {
                            dedicatedThread.interrupt();
                        }
                    }
                }
            } while (dedicatedThread != null);

            if (block) {
                final long shutdownTimeout = System.currentTimeMillis();
                try {
                    /*
                     * Block this thread up to 3 seconds while remaining tasks complete normally
                     */
                    synchronized (q) {
                        if (Thread.currentThread() instanceof Worker) {
                            // A Worker initialized shutdown. Adjust so this does not corrupt shutdown end state transition detection
                            currentlyIdleCount++;
                        }
                        while (currentlyIdleCount < workers.length - 1 || !shutdownQ.isEmpty() || !q.isEmpty() || !fastlaneQ.isEmpty()) {
                            final long timeRemaining = shutdownTimeout + 3000 - System.currentTimeMillis();

                            if (timeRemaining <= 0) {
                                //#debug
                                L.i("A worker blocked shutdown timeout", Worker.toStringWorkers());
                                break;
                            }
                            q.wait(timeRemaining);
                        }
                    }
                } finally {
                    PlatformUtils.getInstance().shutdownComplete(reason
                            + " - Blocking shutdown ending: shutdownTime="
                            + (System.currentTimeMillis() - shutdownTimeout));
                }
            }
        } catch (InterruptedException ex) {
            //#debug
            L.e("Shutdown was interrupted", "", ex);
        }
    }

    private static void dequeueOrCancelOnShutdown(final Vector queue) {
        if (queue == null) {
            return;
        }
        final Task[] tasks = copyOfQueueTasks(queue);

        for (int i = tasks.length - 1; i >= 0; i--) {
            dequeue(tasks[i], queue, true);
        }
    }

    static boolean dequeue(final Task task, final Vector queue, final boolean interruptIfRunning) {
        final int sd = task.getShutdownBehaviour(); 
                
        switch (sd) {
            case Task.EXECUTE_NORMALLY_ON_SHUTDOWN:
                return false;

            case Task.DEQUEUE_OR_INTERRUPT_ON_SHUTDOWN:
                if (interruptIfRunning) {
                    task.cancel(interruptIfRunning, "Shutdown signal received with interruptIfRunning=true");
                }
            // continue to next case

            case Task.DEQUEUE_ON_SHUTDOWN:
                return queue.removeElement(task);
                
            default:
                throw new IllegalStateException("Can not dequeue Task, illegal shutdown behaviour state: " + sd);
        }
    }

    static boolean dequeue(final Task task) {
        boolean removed = dequeue(task, q, false);

        if (!removed) {
            removed = dequeue(task, fastlaneQ, false);
        }
        if (!removed && workers != null) {
            for (int i = 0; i < workers.length; i++) {
                removed = dequeue(task, workers[i].serialQ, false);
                if (removed) {
                    break;
                }
            }
        }
        if (!removed) {
            removed = dequeue(task, idleQ, false);
        }

        return removed;
    }

    /**
     * For unit testing
     *
     * @return
     */
    public static int getNumberOfWorkers() {
        return workers.length;
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
                    Object in = null;

                    synchronized (q) {
                        try {
                            currentTask = null;
                            switch (runState) {
                                case STATE_WAIT_FOR_FINISH_OR_INTERRUPT_TASKS_THAT_EXPLICITLY_PERMIT_INTERRUPT_BEFORE_STARTING_SHUTDOWN_TASKS:
                                    if (allWorkersIdleExceptThisOne()) {
                                        runState = STATE_RUNNING_SHUTDOWN_TASKS;
                                        q.notifyAll();
                                        break;
                                    }

                                // Continue from previous
                                case STATE_RUNNING:
                                    getNormalRunTask();
                                    break;

                                case STATE_RUNNING_SHUTDOWN_TASKS:
                                    getShutdownTask();
                                    break;
                                    
                                default:
                                    throw new IllegalStateException("Illegal Worker run state: " + runState);
                            }
                        } finally {
                            if (currentTask == null) {
                                /*
                                 * Nothing for this thread to do
                                 */
                                try {
                                    ++currentlyIdleCount;
                                    if (runState == STATE_RUNNING_SHUTDOWN_TASKS
                                            && allWorkersIdleExceptThisOne() && allDedicatedThreadsComplete()
                                            && !PlatformUtils.getInstance().shutdownComplete("currentlyIdleCount=" + currentlyIdleCount)) {
                                        q.notifyAll();
                                    }
                                    q.wait();
                                } finally {
                                    --currentlyIdleCount;
                                }
                            } else {
                                in = currentTask.getValue();
                            }
                        }
                    }

                    if (currentTask != null) {
                        currentTask.executeTask(in);
                    }
                } catch (InterruptedException e) {
                    //#mdebug
                    synchronized (q) {
                        L.i(currentTask, "Worker interrupted by call to Task.cancel(true, \"blah\"", "Obscure race conditions can do this, but the code is hardened to deal with it and continue smoothly to the next task. task=" + currentTask);
                    }
                    //#enddebug
                } catch (Exception e) {
                    //#mdebug
                    synchronized (q) {
                        L.e(currentTask, "Uncaught Task exception", "task=" + currentTask, e);
                    }
                    //#enddebug
                }
            }
        } catch (Throwable t) {
            //#mdebug
            synchronized (q) {
                L.e(currentTask, "Fatal worker error", "task=" + currentTask, t);
            }
            //#enddebug
        } finally {
            synchronized (q) {
                currentTask = null;
            }
        }
        //#debug
        L.i("Thread shutdown", "currentlyIdleCount=" + currentlyIdleCount);
    }

    private boolean allWorkersIdleExceptThisOne() {
        return currentlyIdleCount == workers.length - 1;
    }

    private boolean allDedicatedThreadsComplete() {
        return dedicatedThreads.isEmpty();
    }

    private void getFastlaneTask() {
        try {
            currentTask = (Task) fastlaneQ.firstElement();
        } finally {
            fastlaneQ.removeElementAt(0);
        }
    }

    private void getSerialTask() {
        try {
            currentTask = (Task) serialQ.firstElement();
        } finally {
            serialQ.removeElementAt(0);
        }
    }

    private void getNormalRunTask() {
        if (!fastlaneQ.isEmpty()) {
            getFastlaneTask();
        } else if (!isDedicatedFastlaneWorker) {
            if (serialQ.size() <= MAX_SERIAL_Q_LENGTH_BEFORE_PRIORITY_BOOST
                    && !q.isEmpty()) {
                // Normal compute, hardened against async interrupt
                try {
                    currentTask = (Task) q.firstElement();
                } finally {
                    // Ensure we don't re-run in case of interrupt
                    q.removeElementAt(0);
                }
            } else if (!serialQ.isEmpty()) {
                getSerialTask();
            } else if (allWorkersIdleExceptThisOne() && idleQ.size() > 0) {
                getIdleTask();
            }
        }
    }

    private void getIdleTask() {
        try {
            currentTask = (Task) idleQ.firstElement();
        } finally {
            idleQ.removeElementAt(0);
        }
    }

    private void getShutdownTask() {
        if (!shutdownQ.isEmpty()) {
            try {
                currentTask = (Task) shutdownQ.firstElement();
            } finally {
                shutdownQ.removeElementAt(0);
            }
        }
    }

    //#mdebug
    private static String toStringWorkers() {
        final StringBuffer sb = new StringBuffer();

        sb.append("WORKERS: currentlyIdleCount=");
        sb.append(Worker.currentlyIdleCount);
        sb.append(" q.size()=");
        sb.append(Worker.q.size());
        sb.append(" shutdownQ.size()=");
        sb.append(Worker.shutdownQ.size());
        sb.append(" lowPriorityQ.size()=");
        sb.append(Worker.idleQ.size());

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

    static Vector getCurrentState() {
        synchronized (q) {
            final StringBuffer sb = new StringBuffer();
            final int n = Worker.getNumberOfWorkers();
            final Vector lines = new Vector(n + 1);

            for (int i = 0; i < n; i++) {
                final Worker w = Worker.workers[i];
                if (w != null) {
                    final Task task = w.currentTask;
                    sb.append(task != null ? 'o' : ' ');
                }
            }
            if (!fastlaneQ.isEmpty()) {
                sb.append('F');
                sb.append(fastlaneQ.size());
                sb.append('-');
            }
            if (!workers[0].serialQ.isEmpty()) {
                sb.append('S');
                sb.append(workers[0].serialQ.size());
                sb.append(' ');
            }
            if (!q.isEmpty()) {
                sb.append('Q');
                sb.append(Worker.q.size());
            }
            if (!idleQ.isEmpty()) {
                sb.append('B');
                sb.append(Worker.idleQ.size());
            }
            for (int i = 0; i < n; i++) {
                final Worker w = Worker.workers[i];
                if (w != null) {
                    final Task task = w.currentTask;
                    if (task != null) {
                        lines.addElement(trimmedNameNoPackage(Task.getClassName(task)));
                    }
                }
            }
            lines.insertElementAt(sb.toString(), 0);

            return lines;
        }
    }

    private static String trimmedNameNoPackage(String className) {
        final int i = className.lastIndexOf('.');

        if (i >= 0) {
            className = className.substring(i + 1);
        }

        return className;
    }
    //#enddebug

    /**
     * A thread similar to a Worker which runs only until there is no more work
     * in its serialQ to perform.
     *
     * Usually only a single Task is performed, but this may fork additional
     * tasks using Task.SERIAL_CURRENT_THREAD_PRIORITY in which case multiple
     * Tasks are executed before thread death
     */
    private static class DedicatedThread extends Thread {

        final DedicatedThread.TaskRunnable taskRunnable;

        static class TaskRunnable implements Runnable {

            Task task = null;
            final Vector serialQ = new Vector();

            TaskRunnable(final Task firstTask) {
                if (firstTask == null) {
                    throw new IllegalArgumentException("Can not create a DedicatedThread with null Task (no work to do)");
                }

                serialQ.addElement(firstTask);
            }

            public void run() {
                try {
                    while (true) {
                        synchronized (serialQ) {
                            if (serialQ.isEmpty()) {
                                task = null;
                                break;
                            }
                            task = (Task) serialQ.firstElement();
                            serialQ.removeElementAt(0);
                        }

                        try {
                            task.executeTask(task.getValue());
                        } catch (final Throwable t) {
                            //mdebug
                            L.e("Uncaught Task exception on DEDICATED_THREAD_PRIORITY thread", "task=" + task, t);
                        }
                    }
                } finally {
                    dedicatedThreads.removeElement(currentThread());
                    synchronized (q) {
                        q.notifyAll(); // Shutdown sequence may need this notification to complete
                    }
                }
            }
        }

        DedicatedThread(final DedicatedThread.TaskRunnable taskRunnable, final String threadName) {
            super(taskRunnable, threadName);

            this.taskRunnable = taskRunnable;
            dedicatedThreads.addElement(this);
        }
    }
}