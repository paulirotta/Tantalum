/*
 Copyright (c) 2013, Paul Houghton and Futurice Oy
 All rights reserved.

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
 * A
 * <code>Task</code> is the base unit of work in Tantalum. It is a single unit
 * of work executed on a background worker thread pool. The thread pool pulls
 * <code>Task</code>s from a common queue.
 *
 * Each
 * <code>Task</code> is
 * <code>fork()</code>ed and executed only one time after which it is discarded
 * or passed as a value object.
 * <code>Task</code>s receive an input value. During execution this value is
 * mutated into an output value which is from that point immutable.
 *
 * Each
 * <code>Task</code> is a functional programming "function" for asynchronous
 * concurrent fulfillment on a background thread pool. In other functional
 * programming platforms the function filled by
 * <code>Task</code> is known as a "future" (C++ 11 stdlib) or a "promise"
 * (JavaScript frameworks such as RX). All asynchronous
 * <code>Task</code>s can be converted for synchronous usage by calling
 * <code>get()</code> which will block until execution completes.
 *
 * Any piece of code which runs in response to an event and does not explicitly
 * need to be run on the user interface (UI) thread is usually implemented in an
 * extension of
 * <code>Task.exec()</code>. It will automatically be executed on a background
 * worker thread once
 * <code>fork()</code> has been called.
 *
 * A
 * <code>Task</code> which has
 * <code>Task.UI</code> will, after successful completion on a background worker
 * thread, be forked to the platform's UI Thread after successful completion.
 * The UI Thread will then call their
 * <code>run()</code> method for synchronous, serialized interface updates.
 *
 * A
 * <code>Task</code> is often an anonymous inner class extending
 * <code>Task</code> and adding a lambda expression (bit of asynchronously
 * executable code). Such inner classes are closures and exhibit lambda capture
 * (convenient reference access to final values in the outer scope). Since inner
 * classes maintain an object reference to their parent instance, the outer
 * scope parent can not be garbage collected until the Task completes. In cases
 * where rapid heap memory release is critical and the outer scope is holding
 * large objects that should be freed before the Task executes or completes, a
 * named static inner class may be more suitable.
 *
 * Long running
 * <code>Task</code>s may periodically check their status to see if
 * <code>cancel(false, "reason")</code> has been signaled. They can be stopped
 * instantly by calling
 * <code>cancel(true, "reason")</code>.
 * <code>Task.exec()</code> code which may be canceled should utilize
 * <code>try {..}finally{..}</code> to clean any associated resources.
 *
 * @author phou
 */
public abstract class Task implements Runnable {

    /**
     * Start the task as soon as possible, LIFO with no guaranteed sequence
     * order and higher absolute priority than
     * <code>HIGH_PRIORITY</code>.
     *
     * Unlike other priorities, there is one worker thread always held in
     * reserve and only available to fastlane tasks.
     *
     * This is useful for example to perform operations quickly even when all
     * the other threads are busy with tasks such as HTTP GET that can more than
     * a few milliseconds to complete.
     */
    public static final int FASTLANE_PRIORITY = 6;
    /**
     * LIFO with no guaranteed sequence (multi-thread concurrent execution)
     * start the task as soon as possible. The
     * <code>fork()</code> operation will place this as the next Task to be
     * completed unless subsequent
     * <code>HIGH_PRIORITY</code> fork operations occur before a Worker start
     * execution.
     *
     * This is the priority to use if your UI-related task must execute soon
     * even under heavy load AND it never takes very long to to execute. A good
     * example example reading from the local file system. A good example of
     * what not to put in the fastlane is reading from or writing to the network
     * as this can block for a long time and then the fastlane is not so fast
     * anymore.
     */
    public static final int HIGH_PRIORITY = 5;
    /**
     * FIFO with no guaranteed sequence (multi-thread concurrent execution).
     * Start execution after any previously
     * <code>fork()</code>ed work, first in is usually first out, however
     * multiple Workers in parallel means that execution start and completion
     * order is not guaranteed.
     */
    public static final int NORMAL_PRIORITY = 4;
    /**
     * FIFO with no guaranteed sequence (multi-thread concurrent execution).
     * Start execution if there is nothing else for the Workers to do. At least
     * one Worker will always be left idle for immediate activation if only
     * <code>IDLE_PRIORITY</code> work is queued for execution. This is intended
     * for background tasks such as pre-fetch and pre-processing of data that
     * doe not affect the current user view.
     */
    public static final int IDLE_PRIORITY = 3;
    /**
     * FIFO with guaranteed sequence (single-thread concurrent execution, this
     * one thread also does
     * <code>FASTLANE</code> work first, but then
     * <code>SERIAL_PRIORITY</code> tasks are prioritized above other
     * priorities).
     *
     * All
     * <code>SERIAL_PRIORITY</code> tasks are executed on a single thread for
     * guaranteed sequential execution in the order in which they are
     * <code>fork()</code>ed.
     *
     * This is normally used for persistence operations like flash write. In the
     * case of cached content it is not necessary to read in serial order since
     * the heap memory cache will shield out-of-order read inconsistencies which
     * would otherwise occur. Note that in some cases it is preferable for you
     * to sequence items explicitly within a single Task or by the sequence with
     * which one
     * <code>Task</code> chains to or forks other
     * <code>Task</code>s.
     */
    public static final int SERIAL_PRIORITY = 2;
    /**
     * FIFO with no guaranteed sequence (multi-thread concurrent execution).
     *
     * Start execution when
     * <code>PlatformUtils.getInstance().shutdown()</code> is called, and do not
     * exit the program until all such shutdown tasks are completed.
     *
     * Note that if shutdown takes too long and the phone is telling the
     * application to exit, then the phone may give the application a limited
     * time window (typically around 3 seconds) to complete all shutdown
     * activities before killing the application. Shutdown time can be unlimited
     * if the application initiates the exit as by clicking an "Exit" button in
     * the application, but since this can never be guaranteed to be the only
     * shutdown sequence, you must design for quick shutdown.
     */
    public static final int SHUTDOWN = 1;
    private static final int PRIORITY_NOT_SET = Integer.MIN_VALUE;
    /**
     * While holding no other locks, synchronize on the following during
     * critical code sections if your processing routine will temporarily need a
     * large amount of memory. Only one such activity can be active at a time.
     */
    public static final Object LARGE_MEMORY_MUTEX = new Object();
    // status values
    /**
     * For join(), if a timeout is not specified, no thread can wait more than 2
     * minutes.
     */
    public static final int MAX_TIMEOUT = 120000; // 
    /**
     * status: created and forked for execution by a background Worker thread,
     * but no Worker has yet been free to accept this queued Task
     */
    public static final int PENDING = 0;
    /**
     * status: exec() has finished. If this is a UI thread there still may be
     * pending activity for the user interface thread
     */
    public static final int FINISHED = 1;
    /**
     * state: cancel() was called
     */
    public static final int CANCELED = 2;
    private static final String[] STATUS_STRINGS = {"PENDING", "FINISHED", "CANCELED"};
    /**
     * Task will run without interruption or dequeue during shutdown
     */
    public static final int EXECUTE_NORMALLY_ON_SHUTDOWN = 0;
    /**
     * Tasks which have not yet started are removed from the task queue when an
     * application shutdown begins.
     */
    public static final int DEQUEUE_ON_SHUTDOWN = 1;
    /**
     * If a Task is still queued but not yet started, you can request
     * notification of the shutdown in order to complete some cleanup activity.
     * Note that you should usually not be holding resources when queued, so
     * cleanup of that form is probably a design error. You may however want to
     * briefly notify the user.
     *
     * An alternative to this is to create a Task that is run at shutdown time
     * using Worker.queueShutdownTask(Task).
     */
    public static final int DEQUEUE_BUT_LEAVE_RUNNING_IF_ALREADY_STARTED_ON_SHUTDOWN = 2;
    /**
     * Default shutdown behavior.
     *
     * If you explicitly do not want your Task to be cancel(true)ed during
     * shutdown or silently cancel(false)ed when it is queued, use this shutdown
     * behavior. This is useful for example if you need to complete an ongoing
     * Flash memory write operation.
     *
     * An alternative to this is to create a Task that is run at shutdown time
     * using Worker.queueShutdownTask(Task).
     */
    public static final int DEQUEUE_OR_CANCEL_ON_SHUTDOWN = 3;
    private Object value = null; // Always access within a synchronized block
    /**
     * The current execution state, one of several predefined constants
     */
    protected int status = PENDING; // Always access within a synchronized block
    /**
     * All tasks are removed from the queue without notice automatically on
     * shutdown unless specifically marked. For example, writing to flash memory
     * may be required during shutdown, but HttpGetter could block or a long
     * time and should be cancel()ed during shutdown to speed application close.
     */
    private int shutdownBehaviour = DEQUEUE_OR_CANCEL_ON_SHUTDOWN;
    /**
     * The next Task to be executed after this Task completes successfully. If
     * the current task is canceled or throws an exception, the chainedTask(s)
     * will have cancel() called to notify that they will not execute.
     */
    private Task chainedTask = null; // Run afterwords, passing output as input parameter
    private int forkPriority = Task.PRIORITY_NOT_SET; // Access only in synchronized(MUTEX) block
    private final Object MUTEX = new Object();
    /*
     * Should we run() this task on the UI thread after successful execution
     */
    private boolean runOnUIThreadWhenFinished = false;

    /**
     * Indicate if spawn to UI thread will be automatically called on this Task
     * after successful exec()
     *
     * The default value is false.
     *
     * @return
     */
    public boolean isRunOnUIThreadWhenFinished() {
        synchronized (MUTEX) {
            return runOnUIThreadWhenFinished;
        }
    }

    /**
     * Tell this task if it should spawn to UI thread to complete an override of
     * the run() method after successful exec()
     *
     * @param runOnUIThreadWhenFinished
     * @return
     */
    public Task setRunOnUIThreadWhenFinished(boolean runOnUIThreadWhenFinished) {
        synchronized (MUTEX) {
            this.runOnUIThreadWhenFinished = runOnUIThreadWhenFinished;

            return this;
        }
    }

    /**
     * Create a
     * <code>Task</code> with input value of null
     *
     * Use this constructor if your
     * <code>Task</code> does not accept an input value, otherwise use the
     * <code>Task(Object)</code> constructor.
     *
     */
    public Task() {
    }

    /**
     * Create a Task and specify the priority at which this
     * <code>Task</code> should be forked within a chain if the previous
     * <code>Task</code> completes normally.
     *
     * @param priority
     */
    public Task(final int priority) {
        this();
        setForkPriority(priority);
    }

    /**
     * Create a Task with the specified input value.
     *
     * The default action is for the output value to be the same as the input
     * value, however many Tasks will return their own value.
     *
     * @param initialValue
     */
    public Task(final Object initialValue) {
        this();
        set(initialValue);
    }

    public Task(final int priority, final Object initialValue) {
        this(initialValue);
        setForkPriority(priority);
    }

    /**
     * Return the currently set shutdown behavior
     *
     * @return
     */
    public final int getShutdownBehaviour() {
        synchronized (MUTEX) {
            return shutdownBehaviour;
        }
    }

    /**
     * Override the default shutdown behavior
     *
     * If for example you want an ongoing task to finish if it has already
     * started when the shutdown signal comes, but be removed from the queue if
     * it has not yet started, set to Task.DEQUEUE_ON_SHUTDOWN
     *
     * Note that items passed to Worker.queueShutdownTask() ignore this value
     * and will all run normally to completion during shutdown unless the
     * platform (not the application) initiated the shutdown and slow shutdown
     * Tasks result in a shutdown times-out. If this occurs during persistent
     * state write operations, behavior is unpredictable, so it is best to write
     * state as-you-go so that you can shut down quickly.
     *
     * @param shutdownBehaviour
     * @return
     */
    public final Task setShutdownBehaviour(final int shutdownBehaviour) {
        synchronized (MUTEX) {
            if (shutdownBehaviour < Task.EXECUTE_NORMALLY_ON_SHUTDOWN || shutdownBehaviour > Task.DEQUEUE_OR_CANCEL_ON_SHUTDOWN) {
                throw new IllegalArgumentException("Invalid shutdownBehaviour value: " + shutdownBehaviour);
            }

            this.shutdownBehaviour = shutdownBehaviour;

            return this;
        }
    }

    /**
     * Get the current value of this Task. Access is synchronized and execution
     * is not forced execution. If the task has not yet been executed, this will
     * return the input value. If the task has been executed, this will be the
     * return value.
     *
     * @return
     */
    final Object getValue() {
        synchronized (MUTEX) {
            return value;
        }
    }

    /**
     * Execute synchronously if needed and return the resulting value.
     *
     * This is similar to
     * <code>join()</code> with a very long timeout. Note that a
     * <code>MAX_TIMEOUT</code> of 2 minutes is enforced. As with
     * <code>join()</code>, you must
     * <code>fork()</code> or a preceeding
     * <code>Task</code>in a chain before you
     * <code>get()</code> or the worker thread will erroneously assume this is
     * <code>fork()</code>ed along a chain and may wait the full timeout.
     *
     * <code>Task</code> value is final and immutable after execution completes.
     * You may safely use the Task as a value object. Since a
     * <code>Task</code> is a "future" or a "promise" that will be executed as
     * soon as processing and other associated resources become available, you
     * may store the Task as a functional programming value object as soon as it
     * is created. If you
     * <code>get()</code> the value of the object before it has naturally
     * executed, the
     * <code>get()</code> will block and out-of-order execution will attempt to
     * speed up execution. If you choose not to
     * <code>fork()</code> the
     * <code>Task</code>, this is a form of lazy execution at first use. Note
     * that in this case references from the UI Thread will block until
     * execution completes.
     *
     * @return
     * @throws CancellationException
     * @throws TimeoutException
     */
    public final Object get() throws CancellationException, TimeoutException {
        return join();
    }

    /**
     * Set the return value of this task during or at the end of execution.
     *
     * Note that although you can use this to set or override the initial input
     * value of a task before fork()ing it, it is more clear and preferred to
     * use the Task(Object) constructor to set the input value.
     *
     * @param value
     * @return
     */
    public final Object set(final Object value) {
        synchronized (MUTEX) {
            if (this.status >= Task.FINISHED) {
                throw new IllegalStateException("Can not setValue(), Task value is final after execution: " + this);
            }

            return this.value = value;
        }
    }

    /**
     * Execute this task asynchronously on a Worker thread.
     *
     * This will queue the Task with Worker.NORMAL_PRIORITY
     *
     * @return
     */
    public final Task fork() {
        return doFork(getPriorityDuringFork());
    }

    /**
     * Get the priority. If it has not yet been asserted, set it to default.
     *
     * Since priority can only be set once, this and PRIORITY_NOT_SET help allow
     * checks to prevent common multi-chain-or-fork errors.
     *
     * @return
     */
    private int getPriorityDuringFork() {
        synchronized (MUTEX) {
            if (forkPriority == Task.PRIORITY_NOT_SET) {
                setForkPriority(Task.NORMAL_PRIORITY);
            }

            return forkPriority;
        }
    }

    /**
     * Execute this task asynchronously on a Worker thread at the specified
     * priority.
     *
     * @param priority
     * @return
     */
    public final Task fork(final int priority) {
        setForkPriority(priority);

        return doFork(priority);
    }

    private Task doFork(final int taskPriority) {
        return Worker.fork(this, taskPriority);
    }

    /**
     * Wait for up to MAX_TIMEOUT milliseconds for the Task to complete.
     *
     * @return
     * @throws CancellationException
     * @throws TimeoutException
     */
    public final Object join() throws CancellationException, TimeoutException {
        return join(MAX_TIMEOUT);
    }

    /**
     * Wait for a maximum of timeout milliseconds for the
     * <code>Task</code> to run and return it's evaluation value, otherwise
     * throw a
     * <code>TimeoutExeception</code>.
     *
     * Similar to
     * <code>get()</code>, except the total
     * <code>wait()</code> time if the AsyncTask has not completed is explicitly
     * limited to prevent long delays.
     *
     * Always fork() the Task or some previous Task in the chain before calling
     * <code>join()</code>. Note that out-of-order execution may speed
     * <code>join()</code> of a
     * <code>PENDING</code> task, but only if the
     * <code>Task</code> you
     * <code>join()</code> is the first in a chain.
     *
     * Never call
     * <code>join()</code> from the UI thread with a timeout greater than 100ms.
     * This is still bad design and better handled with a chained
     * <code>Task</code>. You will receive a debug warning, but are not
     * prevented from making longer
     * <code>join()</code> calls from the user interface Thread.
     *
     * If you call
     * <code>join()</code> on
     * <code>chain()</code>ed
     * <code>Task</code>, note that there is by definition no out-of-order
     * execution so you must wait for every previous step of the
     * <code>chain()</code> to start and complete at their designated
     * priorities.
     *
     * If you call
     * <code>join()</code> a
     * <code>Task.SERIAL_PRIORITY</code> priority
     * <code>Task</code>, note that there is by definition no out-of-order
     * execution so you must wait for normal completion at the designated
     * priority.
     *
     * @param timeout in milliseconds
     * @return final evaluation result of the Task
     * @throws CancellationException - task was explicitly canceled by another
     * thread
     * @throws TimeoutException - UITask failed to complete within timeout
     * milliseconds
     */
    public final Object join(final long timeout) throws CancellationException, TimeoutException {
        if (timeout < 0) {
            throw new IllegalArgumentException("Can not join() with timeout < 0: timeout=" + timeout);
        }
        //#mdebug
        if (PlatformUtils.getInstance().isUIThread() && timeout > 200) {
            L.i("WARNING- slow Task.join() on UI Thread", "timeout=" + timeout + " " + this);
        }
        //#enddebug
        Object out = null;

        if (getStatus() == PENDING && Worker.tryUnfork(this)) {
            return executeOutOfOrderAfterSuccessfulUnfork();
        }

        synchronized (MUTEX) {
            //#debug
            L.i("Start join", "timeout=" + timeout + " " + this);
            switch (status) {
                case FINISHED:
                    return value;

                case PENDING:
                    final long t = System.currentTimeMillis();
                    try {
                        //#debug
                        L.i("Can not unfork, must be an executing or chained task. Start join() wait", "timeout=" + timeout + " - " + this.toString());

                        try {
                            MUTEX.wait(timeout);
                        } catch (InterruptedException e) {
                            //#debug
                            L.e("InterruptedException during join() wait", "Task will canel: " + this, e);
                        }
                        if (status == FINISHED) {
                            return value;
                        }
                        if (System.currentTimeMillis() > t) {
                            throw new TimeoutException("join(" + timeout + ") was to a Task which did not complete within the specified timeout: " + this);
                        }
                        throw new CancellationException("join(" + timeout + ") was to a Task which was PENDING but was then canceled or externally interrupted: " + this);
                    } finally {
                        //#debug
                        L.i("End join(" + timeout + ") wait", "join wait time=" + (System.currentTimeMillis() - t) + " - " + this.toString());
                    }

                default:
                case CANCELED:
                    throw new CancellationException("join() was to a Task which was canceled: " + this);
            }
        }
    }

    /**
     * Run the application on the current thread. In almost all cases the task
     * is still PENDING and will run, but there is a rare race condition whereby
     * at this point the Task state may have changed, so check again.
     *
     * @return
     * @throws CancellationException
     */
    private Object executeOutOfOrderAfterSuccessfulUnfork() throws CancellationException {
        //#debug
        L.i("Successful unfork of start join of PENDING task", "out of order execution starting on this thread: " + this.toString());

        switch (getStatus()) {
            case PENDING:
                return executeTask(getValue());

            case FINISHED:
                return value;

            default:
            case CANCELED:
                throw new CancellationException("join() was to a Task which was canceled: " + this);
        }
    }

    /**
     * Wait up to a Task.MAX_TIMEOUT milliseconds for all tasks in the list to
     * complete execution. If any or all of them have any problem, the first
     * associated Exception to occur will be thrown.
     *
     * @param tasks
     * @throws CancellationException
     * @throws TimeoutException
     */
    public static void joinAll(final Task[] tasks) throws CancellationException, TimeoutException {
        joinAll(tasks, Task.MAX_TIMEOUT);
    }

    /**
     * Wait up to a maximum specified timeout for all tasks in the list to
     * complete execution. If any or all of them have any problem, the first
     * associated Exception to occur will be thrown.
     *
     * @param tasks - list of Tasks to wait for
     * @param timeout - milliseconds, max total time from for all Tasks to
     * complete
     * @throws CancellationException
     * @throws TimeoutException
     */
    public static void joinAll(final Task[] tasks, final long timeout) throws CancellationException, TimeoutException {
        if (tasks == null) {
            throw new IllegalArgumentException("Can not joinAll(), list of tasks to join is null");
        }
        if (timeout < 0) {
            throw new IllegalArgumentException("Can not joinAll() with timeout < 0: timeout=" + timeout);
        }
        //#mdebug
        if (PlatformUtils.getInstance().isUIThread() && timeout > 100) {
            L.i("WARNING- slow Task.joinAll() on UI Thread", "timeout=" + timeout);
        }
        //#enddebug

        //#debug
        L.i("Start joinAll(" + timeout + ")", "numberOfTasks=" + tasks.length);
        long timeLeft = Long.MAX_VALUE;
        try {
            final long startTime = System.currentTimeMillis();
            for (int i = 0; i < tasks.length; i++) {
                final Task task = tasks[i];
                timeLeft = startTime + timeout - System.currentTimeMillis();

                if (timeLeft <= 0) {
                    throw new TimeoutException("joinAll(" + timeout + ") timout exceeded (" + timeLeft + ")");
                }
                task.join(timeout);
            }
        } finally {
            //#debug
            L.i("End joinAll(" + timeout + ")", "numberOfTasks=" + tasks.length + " timeElapsed=" + (timeout - timeLeft));
        }
    }

    /**
     * Find out the execution state of the Task
     *
     * @return
     */
    public final int getStatus() {
        synchronized (MUTEX) {
            return status;
        }
    }

    /**
     * Return the current status as a string for easy debug information display
     *
     * @return
     */
    public final String getStatusString() {
        synchronized (MUTEX) {
            return Task.STATUS_STRINGS[status];
        }
    }

    /**
     * Change the status
     *
     * You can only change status CANCELED or EXCEPTION to the READY state to
     * explicitly indicate you are going to re-use this Task. Note that Task
     * re-use is generally not advised, but can be valid if you have a special
     * need such as performance when re-creating the Task is particularly
     * expensive.
     *
     * @param status
     */
    final void setStatus(final int status) {
        if (status == CANCELED) {
            throw new IllegalArgumentException("Do not setStatus(Task.CANCELED). Call Task.cancel(false, \"Reason for cancel\") instead to keep your code debuggable");
        }

        doSetStatus(status);
    }

    private void doSetStatus(final int status) {
        final Task t;
        synchronized (MUTEX) {
            if (this.status == status) {
                //#debug
                L.i("State change from " + getStatusString() + " to " + Task.STATUS_STRINGS[status] + " is ignored", this.toString());
                return;
            }
            if (status > FINISHED) {
                throw new IllegalArgumentException("setStatus(" + Task.STATUS_STRINGS[status] + ") not allowed, already FINISHED or CANCELED: " + this);
            }
            this.status = status;
            MUTEX.notifyAll();
            t = chainedTask;
            if (status == CANCELED) {
                /*
                 * Unchain as we cancel to simplify avoiding any future reference to
                 * canceled chained tasks. This can also speed garbage collection.
                 */
                chainedTask = null;
            }
        }

        if (status == CANCELED) {
            PlatformUtils.getInstance().runOnUiThread(new Runnable() {
                public void run() {
                    //#debug
                    L.i("Task onCanceled()", Task.this.toString());
                    onCanceled();
                }
            });
            // Also cancel any chained Tasks expecting the output of this Task
            if (t != null) {
                t.cancel(false, "Previous task in chain was canceled");
            }
        }
    }

    /**
     * Set a
     * <code>Task</code> which will
     * <code>fork()</code>ed after the current
     * <code>Task</code> completes. nextTask == null is legal and has no effect.
     *
     * Each
     * <code>Task</code> in a chain will run at the same priority as the
     * previous
     * <code>Task</code> in the chain unless you explicitly set a different
     * priority for it.
     *
     * @param nextTask
     * @return nextTask
     */
    public final Task chain(final Task nextTask) {
        if (nextTask != null) {
            final Task multiLinkChain;
            synchronized (MUTEX) {
                if (chainedTask == null) {
                    chainedTask = nextTask;
                    multiLinkChain = null;
                } else {
                    // Already chained- we must add to the end of the chain
                    multiLinkChain = chainedTask;
                }
            }
            if (multiLinkChain != null) {
                /*
                 * Call this outside the above synchronized block so that we are not
                 * holding multiple locks for Tasks at the same time
                 */
                multiLinkChain.chain(nextTask);
            }
        }

        return nextTask;
    }

    /**
     * Update the priority parameter which will be applied to this Task when it
     * is fork()ed as link in a Task chain.
     *
     * @param priority
     * @return
     */
    private Task setForkPriority(final int priority) {
        synchronized (MUTEX) {
            if ((priority < Task.SHUTDOWN) || priority > Task.FASTLANE_PRIORITY) {
                throw new IllegalArgumentException("Can not set illegal Task priority " + priority + ". Use one of the constants such as Task.NORMAL_PRIORITY");
            }
            if (forkPriority != Task.PRIORITY_NOT_SET) {
                throw new IllegalStateException("Task priority has alrady been set: " + this);
            }
            forkPriority = priority;

            return this;
        }
    }

    /**
     * Return the priority value applied when this Task was fork()ed.
     *
     * If the Task is not yet fork()ed, for example if it is part of a Task
     * chain, this is the value which will be applied when it is forked.
     *
     * @return
     */
    public final int getForkPriority() {
        synchronized (MUTEX) {
            return forkPriority;
        }
    }

    private int assertForkPriority(final int priorityIfPriorityNotSet) {
        synchronized (MUTEX) {
            if (forkPriority == Task.PRIORITY_NOT_SET) {
                forkPriority = priorityIfPriorityNotSet;
            }

            return forkPriority;
        }
    }

    /**
     * You can call this as the return statement of your overriding method once
     * you have set the result
     *
     * @return
     */
    final Object executeTask(final Object in) {
        Object out = null;

        try {
            synchronized (MUTEX) {
                if (status == Task.CANCELED) {
                    throw new IllegalStateException(this.getStatusString() + " state can not be executed: " + this);
                }
            }
            /*
             * Execute the Task without holding any locks
             */
            out = exec(in);

            final boolean doRun;
            final boolean executionSuccessful;
            final Task t;
            synchronized (MUTEX) {
                executionSuccessful = status == Task.PENDING;
                if (executionSuccessful) {
                    value = out;
                    t = chainedTask;
                    doRun = this.runOnUIThreadWhenFinished;
                    setStatus(FINISHED);
                } else {
                    // Task was canceled
                    t = null;
                    doRun = false;
                }
            }
            if (doRun) {
                PlatformUtils.getInstance().runOnUiThread(this);
            }
            if (t != null) {
                //#debug
                L.i("Begin fork chained task", t.toString() + " INPUT: " + in);
                t.set(out);
                t.assertForkPriority(forkPriority);
                t.fork();
            }
        } catch (final Throwable t) {
            //#debug
            L.e("Exception during Task exec()", this.toString(), t);
            cancel(false, "Unhandled task exception: " + this.toString() + " : " + t);
            out = null;
        }

        return out;
    }

    /**
     * Override to implement Worker thread code
     *
     * @param in
     * @return
     */
    protected abstract Object exec(Object in);

    /**
     * Cancel execution if possible. This is called on the Worker thread
     *
     * Do not override this unless you also call super.cancel(boolean).
     *
     * Override onCanceled() is the normal notification location, and is called
     * from the UI thread with Task state updates handled for you.
     *
     * @param mayInterruptIfRunning
     * @param reason
     * @return
     */
    public boolean cancel(final boolean mayInterruptIfRunning, final String reason) {
        synchronized (MUTEX) {
            if (reason == null || reason.length() == 0) {
                throw new IllegalArgumentException("For clean debug, you must provide a reason for cancel(), null will not do");
            }

            boolean canceled = status != Task.CANCELED;

            if (canceled) {
                //#debug
                L.i("Ignore cancel() - " + reason, "already CANCELED: " + this);
            } else {
                //#debug
                L.i("Begin cancel() - " + reason, "status=" + this.getStatusString() + " " + this);
                switch (status) {
                    case FINISHED:
                        //#debug
                        L.i("Ignored attempt to interrupt an EXEC_FINISHED Task", this.toString());
                        break;

                    case PENDING:
                        if (mayInterruptIfRunning) {
                            canceled = true;
                            Worker.interruptTask(this);
                            if (status == Task.CANCELED) {
                                /*
                                 * If the Task.cancel() sent an interrupt which was
                                 * caught, the Task was canceled at that point.
                                 */
                                break;
                            }
                        }
                    // continue to default

                    default:
                        canceled = true;
                        doSetStatus(CANCELED);
                }
                //#debug
                L.i("End cancel() - " + reason, "status=" + this.getStatusString() + " " + this);
            }

            return canceled;
        }
    }

    /**
     * This is executed on the UI thread
     *
     * Override if needed, the default implementation does nothing except
     * provide debug output.
     *
     * Use getStatus() to distinguish between CANCELED and EXCEPTION states if
     * necessary.
     *
     */
    protected void onCanceled() {
        //#debug
        L.i("Task canceled", this.toString());
    }

    /**
     * Check of the task has been had cancel() called or has thrown an uncaught
     * exception while executing.
     *
     * @return
     */
    public final boolean isCanceled() {
        synchronized (MUTEX) {
            return status == AsyncTask.CANCELED;
        }
    }

    /**
     * The default implementation of run() does nothing. If you
     * task.fork(Task.UI) then you should override this method.
     *
     * Your overriding run() method probably will want to access and display the
     * result with getValue()
     *
     */
    public void run() {
    }

    //#mdebug
    /**
     * When debugging, show what each Worker is doing and the queue length
     *
     * @return One ore more lines of text depending on the number of active
     * Workers
     */
    public static Vector getCurrentState() {
        return Worker.getCurrentState();
    }

    /**
     * For debug
     *
     * @return
     */
    public String toString() {
        synchronized (MUTEX) {
            return "TASK: status=" + getStatusString() + " result=" + value + " nextTask=(" + chainedTask + ")";
        }
    }
    //#enddebug
}
