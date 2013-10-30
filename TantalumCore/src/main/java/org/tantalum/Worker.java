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
    private static final int STATE_WAIT_FOR_RUNNING_TASKS_TO_FINISH_BEFORE_STARTING_SHUTDOWN_TASKS = 1;
    private static final int STATE_RUNNING_SHUTDOWN_TASKS = 2;
    private static final int QUEUE_SIZE_LIMIT = 32;
    private static final int SHUTDOWN_TIMEOUT = 15000; // ms, how long to hold the calling thread when system initiates a shutdown
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
    private static final Vector shutdownUI_Q = new Vector();
    private static final Vector idleQ = new Vector();
    private static final Vector shutdownQ = new Vector();
    private static int runState = STATE_RUNNING;
    volatile static boolean shuttingDown = false;
    private Task currentTask = null; // Access only within synchronized(q)
    private final boolean isDedicatedFastlaneWorker;

    private Worker(final String name, final boolean isDedicatedFastlaneWorker) {
        super(name);

        this.isDedicatedFastlaneWorker = isDedicatedFastlaneWorker;
    }

    /**
     * Return true until shutdown has started
     *
     * @return
     */
    static boolean isNormalRunState() {
        synchronized (q) {
            return runState == Worker.STATE_RUNNING;
        }
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

    /**
     * True during the shutdown process
     *
     * @return
     */
    static boolean isShuttingDown() {
        return shuttingDown;
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
            ((DedicatedThread) currentThread).serialQ.addElement(task);
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
                    final DedicatedThread thread = DedicatedThread.getDedicatedThread();

                    thread.serialQ.addElement(task);
                    thread.start();
                    break;

                case Task.UI_PRIORITY:
                    forkToUIThread(task);
                    break;

                case Task.FASTLANE_PRIORITY:
                    fastlaneQ.insertElementAt(task, 0);
                    if (fastlaneQ.size() > QUEUE_SIZE_LIMIT) {
                        fastlaneQ.removeElementAt(fastlaneQ.size() - 1);
                    }
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
                    if (q.size() > QUEUE_SIZE_LIMIT) {
                        q.removeElementAt(q.size() - 1);
                    }
                    q.notifyAll();
                    break;

                case Task.NORMAL_PRIORITY:
                    if (q.size() >= QUEUE_SIZE_LIMIT) {
                        q.removeElementAt(q.size() - 1);
                    }
                    q.addElement(task);
                    q.notifyAll();
                    break;

                case Task.IDLE_PRIORITY:
                    idleQ.addElement(task);
                    q.notifyAll();
                    break;

                case Task.SHUTDOWN_UI:
                    shutdownUI_Q.addElement(task);
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
     * Note: by definition, you can not unfork tasks in the serialQ of a Worker.
     * These will always be run in guaranteed order.
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
        L.i(task, "tryUnfork", "success=" + success + " task=" + task);

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

                if (task.equals(dedicatedThread.task)) {
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
     */
    static void shutdown(final String reason) {
        try {
            /*
             * Removed queued tasks which can be removed
             */
            //#debug
            L.i("shutdown called", "reason=" + reason);
            shuttingDown = true;
            synchronized (shutdownUI_Q) {
                while (!shutdownUI_Q.isEmpty()) {
                    final Task t = (Task) shutdownUI_Q.firstElement();

                    PlatformUtils.getInstance().runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                t.executeTask(t.getValue());
                            } catch (Exception e) {
                                //#debug
                                L.e("Can not run on SHUTDOWN_UI task", reason, e);
                            }
                        }
                    });
                    shutdownUI_Q.removeElementAt(0);
                }
            }
//            synchronized (q) {
//                runState = STATE_WAIT_FOR_RUNNING_TASKS_TO_FINISH_BEFORE_STARTING_SHUTDOWN_TASKS;
//                q.notifyAll();
//                for (int i = 0; i < workers.length; i++) {
//                    final Task t = workers[i].currentTask;
//
//                    if (t != null) {
//                        t.shutdownNotify();
//                    }
//                }
//            }
//            fastlaneQ.removeAllElements();
//            for (int i = 0; i < workers.length; i++) {
//                workers[i].serialQ.removeAllElements();
//            }
            q.removeAllElements();
//            idleQ.removeAllElements();
//            final DedicatedThread[] dt;
//            synchronized (dedicatedThreads) {
//                dt = new DedicatedThread[dedicatedThreads.size()];
//                dedicatedThreads.copyInto(dt);
//            }
//            for (int i = 0; i < dt.length; i++) {
//                dt[i].serialQ.removeAllElements();
//            }

            /*
             * Interrupt currently running tasks which can be interrupted
             */
//            synchronized (q) {
//                for (int i = 0; i < workers.length; i++) {
//                    if (workers[i] == Thread.currentThread()) {
//                        continue;
//                    }
//                    final Task t = workers[i].currentTask;
//                    if (t != null && t.getShutdownBehaviour() == Task.EXECUTE_NORMALLY_ON_SHUTDOWN) {
//                        // Replace this worker- we don't wait for any such task
//                        workers[i] = new Worker("Shutdown-replace-" + workers[i].getName(), workers[i].isDedicatedFastlaneWorker);
//                        workers[i].start();
//                    }
//                }
//            }
//            DedicatedThread dedicatedThread;
//            do {
//                synchronized (dedicatedThreads) {
//                    if (dedicatedThreads.size() > 0) {
//                        dedicatedThread = (DedicatedThread) dedicatedThreads.firstElement();
//                        dedicatedThreads.removeElementAt(0);
//                    } else {
//                        dedicatedThread = null;
//                    }
//                }
//
//                if (dedicatedThread != null) {
//                    synchronized (dedicatedThread.serialQ) {
//                        if (dedicatedThread.task != null && dedicatedThread.task.getShutdownBehaviour() == Task.DEQUEUE_OR_INTERRUPT_ON_SHUTDOWN) {
//                            dedicatedThread.interrupt();
//                        }
//                    }
//                }
//            } while (dedicatedThread != null);
            runShutdownTasks(reason);
        } catch (Throwable ex) {
            //#debug
            L.e("Shutdown throwable", "", ex);
        } finally {
            synchronized (q) {
                q.notifyAll();
            }

        }
    }

    private static void runShutdownTasks(final String reason) throws InterruptedException {
        final long shutdownTimeout = System.currentTimeMillis();

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        try {
            // Wait for all Workers to die except this thread
            if (workers != null) {
                for (int i = 0; i < workers.length; i++) {
                    try {
                        final Worker w = workers[i];

                        if (w == Thread.currentThread()) {
                            //#debug
                            L.i("Skipping current thread wait during shutdown", "reason=" + reason);
                            continue;
                        }
                        if (w.isAlive()) {
                            //#debug
                            L.i("Join active thread during shutdown", w.toString());
                            w.join();
                            //#debug
                            L.i("Continue after join active thread during shutdown", w.toString());
                        } else {
                            //#debug
                            L.i("Idle thread during shutdown", w.toString());
                        }
                    } catch (Throwable t) {
                        //#debug
                        L.e("Problem waiting for a worker to die", "", t);
                    }
                }
            }

            // Run all queues until empty
            //#debug
            L.i("Start executing shutdown tasks", "size=" + shutdownQ.size());
            Task task;
            do {
                task = getShutdownTask();
                if (task != null) {
                    //#debug
                    L.i("Start executing shutdown task", task.toString());
                    task.executeTask(task.getValue());
                }
            } while (task != null);
            //#debug
            L.i("Done with all shutdown tasks", "size=" + shutdownQ.size());            

//            /*
//             * Block this thread up to 3 seconds while remaining tasks complete normally
//             */
//            synchronized (q) {
//                while (!shutdownQ.isEmpty() || !q.isEmpty() || !fastlaneQ.isEmpty() || !allWorkersIdleExceptThisOne()) {
//                    final long timeRemaining = shutdownTimeout + SHUTDOWN_TIMEOUT - System.currentTimeMillis();
//
//                    if (timeRemaining <= 0) {
//                        //#debug
//                        L.i("A worker blocked shutdown timeout", Worker.toStringWorkers());
//                        break;
//                    }
//                    q.wait(timeRemaining);
//                }
//            }
        } finally {
            PlatformUtils.getInstance().shutdownComplete(reason
                    + " - Shutdown ending: shutdownTime="
                    + (System.currentTimeMillis() - shutdownTimeout));
        }
    }

//    private static void dequeueOrCancelOnShutdown(final Vector queue) {
//        if (queue == null) {
//            return;
//        }
//        final Task[] tasks = copyOfQueueTasks(queue);
//
//        for (int i = tasks.length - 1; i >= 0; i--) {
//            dequeue(tasks[i]);
//        }
//    }
//    static boolean dequeue(final Task task, final Vector queue, final boolean interruptIfRunning) {
//        final int sd = task.getShutdownBehaviour();
//
//        switch (sd) {
//            case Task.EXECUTE_NORMALLY_ON_SHUTDOWN:
//                return false;
//
////            case Task.DEQUEUE_OR_INTERRUPT_ON_SHUTDOWN:
////                if (interruptIfRunning) {
////                    task.cancel(interruptIfRunning, "Shutdown signal received with interruptIfRunning=true");
////                }
//            // continue to next case
//            case Task.DO_NOT_WAIT_FOR_THIS_ON_SHUTDOWN:
//            case Task.DEQUEUE_ON_SHUTDOWN:
//                return queue.removeElement(task);
//
//            default:
//                throw new IllegalStateException("Can not dequeue Task, illegal shutdown behaviour state: " + sd);
//        }
//    }
    static boolean dequeue(final Task task) {
        synchronized (q) {
            if (q.contains(task)) {
                q.removeElement(task);
                return true;
            }
        }

        synchronized (fastlaneQ) {
            if (fastlaneQ.contains(task)) {
                fastlaneQ.removeElement(task);
                return true;
            }
        }

        synchronized (idleQ) {
            if (idleQ.contains(task)) {
                idleQ.removeElement(task);
                return true;
            }
        }

        if (workers != null) {
            for (int i = 0; i < workers.length; i++) {
                synchronized (workers[i].serialQ) {
                    if (workers[i].serialQ.contains(task)) {
                        workers[i].serialQ.removeElement(task);
                        return true;
                    }
                }
            }
        }

        return false;
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
            while (!shuttingDown) {
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
                                case STATE_WAIT_FOR_RUNNING_TASKS_TO_FINISH_BEFORE_STARTING_SHUTDOWN_TASKS:
                                    continue;
//                                    if (allWorkersIdleExceptThisOne()) {
//                                        runState = STATE_RUNNING_SHUTDOWN_TASKS;
//                                        q.notifyAll();
//                                        break;
//                                    }

                                // Continue from previous
                                case STATE_RUNNING:
                                    getNormalRunTask();
                                    break;

                                case STATE_RUNNING_SHUTDOWN_TASKS:
                                    continue;
//                                    getNormalRunTask();
//                                    if (currentTask == null) {
//                                        getShutdownTask();
//                                    }
//                                    break;

                                default:
                                    throw new IllegalStateException("Illegal Worker run state: " + runState);
                            }
                        } finally {
                            if (currentTask == null) {
                                /*
                                 * Nothing for this thread to do
                                 */
                                if (runState == STATE_RUNNING_SHUTDOWN_TASKS
                                        && allWorkersIdleExceptThisOne() /*&& allDedicatedThreadsComplete()*/) {
                                    q.notifyAll(); // Let any hold-for-shutdown thread continue
//                                    PlatformUtils.getInstance().shutdownComplete("All workers idle except this thread");
                                    return;
                                }
                                q.wait();
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
                q.notifyAll();
            }
        }
        //#debug
        L.i(this, "Thread shutdown", null);
    }

    private static boolean allWorkersIdleExceptThisOne() {
        for (int i = 0; i < workers.length; i++) {
            if (workers[i] == Thread.currentThread()) {
                continue;
            }
            if (workers[i].currentTask != null) {
                return false;
            }
        }

        return true;
    }

    private boolean allDedicatedThreadsComplete() {
        return dedicatedThreads.isEmpty();
    }

    /**
     * Hardened against async interrupt
     *
     */
    private void getFastlaneTask() {
        try {
            currentTask = (Task) fastlaneQ.firstElement();
        } finally {
            fastlaneQ.removeElementAt(0);
        }
    }

    /**
     * Hardened against async interrupt
     *
     */
    private void getSerialTask() {
        try {
            currentTask = (Task) serialQ.firstElement();
        } finally {
            serialQ.removeElementAt(0);
        }
    }

    /**
     * Hardened against async interrupt
     *
     */
    private void getNormalTask() {
        try {
            currentTask = (Task) q.firstElement();
        } finally {
            // Ensure we don't re-run in case of interrupt
            q.removeElementAt(0);
        }

    }

    /**
     * Get the next task (if any) appropriate for this thread in the normal
     * running state (not shutdown)
     */
    private void getNormalRunTask() {
        if (serialQ.size() > 8) {
            getSerialTask();
        } else if (!Worker.fastlaneQ.isEmpty()) {
            getFastlaneTask();
        } else if (!isDedicatedFastlaneWorker) {
            if (!Worker.q.isEmpty()) {
                getNormalTask();
            } else if (!serialQ.isEmpty()) {
                getSerialTask();
            } else if (!idleQ.isEmpty() && allWorkersIdleExceptThisOne()) {
                getIdleTask();
            }
        } else if (!serialQ.isEmpty()) {
            getSerialTask();
        }
//        if (!serialQ.isEmpty()) {
//            getSerialTask();
//        } else if (!Worker.fastlaneQ.isEmpty()) {
//            getFastlaneTask();
//        } else if (!isDedicatedFastlaneWorker) {
//            if (!Worker.q.isEmpty()) {
//                getNormalTask();
//            } else if (!idleQ.isEmpty() && allWorkersIdleExceptThisOne()) {
//                getIdleTask();
//            }
//        }
    }

    private void getIdleTask() {
        try {
            currentTask = (Task) idleQ.firstElement();
        } finally {
            idleQ.removeElementAt(0);
        }
    }

    private static Task getShutdownTask() {
        if (!q.isEmpty()) {
            try {
                return (Task) q.firstElement();
            } finally {
                q.removeElementAt(0);
            }
        }
        if (!shutdownQ.isEmpty()) {
            try {
                return (Task) shutdownQ.firstElement();
            } finally {
                shutdownQ.removeElementAt(0);
            }
        }

        return null;
    }

    //#mdebug
    private static String toStringWorkers() {
        final StringBuffer sb = new StringBuffer();

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

        private static DedicatedThread prebuildDedicatedThread = null;
        Task task = null;
        final Vector serialQ = new Vector();
        static int dedicatedThreadCounter = 0;

        static synchronized DedicatedThread getDedicatedThread() {
            final DedicatedThread t;

            if (prebuildDedicatedThread != null) {
                t = prebuildDedicatedThread;
                prebuildDedicatedThread = null;
            } else {
                t = new DedicatedThread();
            }
            makeNextThread();

            return t;
        }

        private static void makeNextThread() {
            if (isNormalRunState()) {
                new Task(Task.FASTLANE_PRIORITY) {
                    protected Object exec(Object in) throws CancellationException, TimeoutException, InterruptedException {
                        synchronized (DedicatedThread.class) {
                            if (prebuildDedicatedThread == null) {
                                prebuildDedicatedThread = new DedicatedThread();
                            }

                            return in;
                        }
                    }
                }.setClassName("PrebuildDedicatedThread").fork();
            }
        }

        public void run() {
            try {
                while (!shuttingDown) {
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
                //#debug
                L.i("DedicatedThread end", this.getName());
            }
        }

        private DedicatedThread() {
            super("DedicatedThread" + dedicatedThreadCounter++);
            dedicatedThreads.addElement(this);
        }
    }
}
