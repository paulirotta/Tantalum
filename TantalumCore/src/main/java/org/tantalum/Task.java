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

import java.util.Timer;
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
public abstract class Task {

    /**
     * Start the task immediately on a dedicated Thread. The Thread expires
     * after the Task completes.
     *
     * This is relatively expansive for performance. It can be useful in cases
     * such as multimedia when the Task make take a long time to complete. It
     * can also be useful if for best user experience (UX) the Task should start
     * immediately regardless of current load.
     *
     * Note that you must be careful to limit the number of simultaneous threads
     * which result or application performance and stability may drop.
     */
    public static final int DEDICATED_THREAD_PRIORITY = 8;
    /**
     * Queue the task the system UI thread where it will execute after any
     * pending system events like touch input.
     *
     * This has the ability to be chained before or after other Tasks. You can
     * use it just like any other Task running on a background thread. I/O,
     * <code>join()</code>ing other threads and heavy computation loops are
     * strongly discouraged to keep your UI responsive.Like other
     * <code>Tasks</code> you can
     * <code>join()</code> a
     * <code>UI_PRIORITY</code>
     * <code>Task</code> from another thread to sequence activities or receive
     * results.
     *
     * Note that a simpler alternative of alternative of
     * <code>PlatformUtils.getInstance.runOnUIThread(Runnable)</code>. It works
     * well and with lower UI thread loading in cases that are stateless, un-
     * <code>chain()</code>ed, and where a reusable
     * <code>Runnable</code> object can implement frequently-occurring display
     * events.
     */
    public static final int UI_PRIORITY = 7;
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
    public static final int SERIAL_PRIORITY = 5;
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
    public static final int HIGH_PRIORITY = 4;
    /**
     * FIFO with no guaranteed sequence (multi-thread concurrent execution).
     * Start execution after any previously
     * <code>fork()</code>ed work, first in is usually first out, however
     * multiple Workers in parallel means that execution start and completion
     * order is not guaranteed.
     */
    public static final int NORMAL_PRIORITY = 3;
    /**
     * FIFO with no guaranteed sequence (multi-thread concurrent execution).
     * Start execution if there is nothing else for the Workers to do. At least
     * one Worker will always be left idle for immediate activation if only
     * <code>IDLE_PRIORITY</code> work is queued for execution. This is intended
     * for background tasks such as pre-fetch and pre-processing of data that
     * doe not affect the current user view.
     */
    public static final int IDLE_PRIORITY = 2;
    /**
     * FIFO with no guaranteed sequence (multi-thread concurrent execution).
     * These tasks start when the application begins to close. The application
     * will not complete close until after they are all done.
     *
     * Start execution when
     * <code>PlatformUtils.getInstance().shutdown()</code> is called, and do not
     * exit the program until all such shutdown tasks are completed.
     *
     * Shutdown Tasks are run concurrently in no specified order. If you want
     * your Task to execute before a given cache closes, you should call
     * StaticCache.addShutdownTask() instead.
     *
     * Note that if shutdown takes too long and the phone is telling the
     * application to exit, then the phone may give the application a limited
     * time window (typically around 3 seconds) to complete all shutdown
     * activities before killing the application. Shutdown time can be unlimited
     * if the application initiates the exit as by clicking an "Exit" button in
     * the application, but since this can never be guaranteed to be the only
     * shutdown sequence, you must design for quick shutdown.
     *
     */
    public static final int SHUTDOWN = 1;
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
     * application shutdown begins. This is the default behavior.
     */
    public static final int DEQUEUE_ON_SHUTDOWN = 1;
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
    public static final int DEQUEUE_OR_INTERRUPT_ON_SHUTDOWN = 2;
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
    private int shutdownBehaviour = Task.DEQUEUE_ON_SHUTDOWN;
    /**
     * The next Task to be executed after this Task completes successfully. If
     * the current task is canceled or throws an exception, the chainedTask(s)
     * will have cancel() called to notify that they will not execute.
     */
    private Task chainedTask = null; // Run afterwords, passing output as input parameter
    private final int forkPriority;
    private final Object mutex = new Object();

    /**
     * Init on first request. Many apps will not use this extra thread, and this
     * inner class is not loaded until first reference. Class loading is
     * thread-safe, so this is proper thread-safe lazy instantiation.
     */
    private static class TimerHolder {

        static Timer timer = new Timer();
    }

    /**
     * Create a Task and specify the priority at which this
     * <code>Task</code> should be forked within a chain if the previous
     * <code>Task</code> completes normally.
     *
     * Use this constructor if your
     * <code>Task</code> does not accept an input value, otherwise use the
     * <code>Task(Object)</code> constructor.
     *
     * @param priority
     */
    public Task(final int priority) {
        if ((priority < Task.SHUTDOWN) || priority > Task.DEDICATED_THREAD_PRIORITY) {
            throw new IllegalArgumentException("Can not set illegal Task priority " + priority + ". Use one of the constants such as Task.NORMAL_PRIORITY");
        }
        forkPriority = priority;
    }

    /**
     * Create a Task of Task.NORMAL_PRIORITY
     *
     */
    public Task() {
        this(Task.NORMAL_PRIORITY);
    }

    /**
     * Create a Task with the specified input value.
     *
     * The default action is for the output value to be the same as the input
     * value, however many Tasks will return their own value.
     *
     * @param priority
     * @param initialValue
     */
    public Task(final int priority, final Object initialValue) {
        this(priority);
        set(initialValue);
    }

    /**
     * Return a general use Timer thread singleton. Note that the Timer thread
     * is not created unless you call this method.
     *
     * @return
     */
    public static Timer getTimer() {
        return TimerHolder.timer;
    }

    /**
     * Return the currently set shutdown behavior
     *
     * @return
     */
    public final int getShutdownBehaviour() {
        synchronized (mutex) {
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
        synchronized (mutex) {
            if (shutdownBehaviour < Task.EXECUTE_NORMALLY_ON_SHUTDOWN || shutdownBehaviour > Task.DEQUEUE_OR_INTERRUPT_ON_SHUTDOWN) {
                throw new IllegalArgumentException(getClassName() + " invalid shutdownBehaviour value: " + shutdownBehaviour);
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
        synchronized (mutex) {
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
    public final Task set(final Object value) {
        synchronized (mutex) {
            if (this.status != Task.PENDING) {
                throw new IllegalStateException(getClassName() + " can not set(), Task value is final after execution: " + this);
            }

            this.value = value;

            return this;
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
        return Worker.fork(this);
    }

    /**
     * <code>fork()</code> several tasks as a single atomic operation. This
     * allows more precise influence on which of several
     * <code>Task</code>s will begin next.
     *
     * When you
     * <code>fork()</code> one-by-one, the first can start execution before the
     * last is queued. This may mean for example that there is a race between
     * how fast you
     * <code>fork()</code> and which
     * <code>Task.HIGH_PRIORITY</code> or
     * <code>Task.FASTLANE_PRIORITY</code>
     * <code>Task</code> starts next.
     *
     * You can address this by precisely sorting your
     * <code>Task[]</code> in advance and submitting them all at once. They will
     * then be
     * <code>fork()</code>ed in sequence. With
     * <code>Task.NORMAL_PRIORITY</code> that means the first item will begin
     * executing first. With
     * <code>Task.FASTLANE_PRIORITY</code> the last item will begin execution
     * first.
     *
     * @param tasks
     * @return
     */
    public static Task[] fork(final Task[] tasks) {
        return Worker.fork(tasks);
    }

    /**
     * Perform multiple operations as a single atomic update to the task queues.
     *
     * For example, you can pass in a Runnable which calls
     * <code>StaticWebCache.getAsync()</code> several times to guarantee the
     * order in which these asynchronous operations start after the Runnable
     * completes.
     *
     * Note that until your Runnable completes, no new Task can be fork()ed and
     * no new Task can start. Thus be careful to do any
     *
     * @param runnable
     */
    public static void runAtomic(final Runnable runnable) {
        Worker.runAtomic(runnable);
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
     * @return final evaluation result of the Task, or if the Task returns a
     * Task t2, then the value of t2.join()
     * @throws CancellationException - task was explicitly canceled by another
     * thread
     * @throws TimeoutException - UITask failed to complete within timeout
     * milliseconds
     */
    public final Object join(long timeout) throws CancellationException, TimeoutException {
        if (timeout < 0) {
            throw new TimeoutException("Can not join(" + timeout + ") " + this);
        }
        //#mdebug
        if (PlatformUtils.getInstance().isUIThread() && timeout > 200) {
            L.i(this, "WARNING- slow join(" + timeout + ")", "UI Thread may become unresponsive " + this);
        }
        //#enddebug

        if (getStatus() == PENDING && Worker.tryUnfork(this)) {
            return executeOutOfOrderAfterSuccessfulUnfork();
        }

        synchronized (mutex) {
            //#debug
            L.i(this, "start join(" + timeout + ")", "" + this);
            switch (status) {
                case FINISHED:
                    break;

                case PENDING:
                    final long t = System.currentTimeMillis();
                    try {
                        //#debug
                        L.i(this, "Can not unfork, must be an executing or chained Task. Start join(" + timeout + ")", "" + this);

                        try {
                            mutex.wait(timeout);
                        } catch (InterruptedException e) {
                            //#debug
                            L.e(this, "InterruptedException during join(" + timeout + ") wait", "Task will cancel: " + this, e);
                        }
                        //#debug
                        L.i(this, "End join(" + timeout + ") after can not unfork executing or chained Task", "" + this);
                        if (status == FINISHED) {
                            timeout -= System.currentTimeMillis() - t;
                            break;
                        }
                        if (System.currentTimeMillis() > t && !(value instanceof Task)) {
                            throw new TimeoutException(getClassName() + " join(" + timeout + ") was to a Task which did not complete within the specified timeout: " + this);
                        }
                        throw new CancellationException(getClassName() + " join(" + timeout + ") was to a Task which was PENDING but was then canceled or externally interrupted: " + this);
                    } finally {
                        //#debug
                        L.i(this, "End join(" + timeout + ") wait", "join wait timeElapsed=" + (System.currentTimeMillis() - t) + " - " + this.toString());
                    }

                default:
                case CANCELED:
                    throw new CancellationException(getClassName() + " join(" + timeout + ") was to a Task which was canceled: " + this);
            }

            return value;
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
        L.i(this, "Successful unfork join() PENDING task", "Out of order exec: " + this);

        synchronized (mutex) {
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
            L.i("WARNING- slow", "Task.joinAll(" + timeout + ") on UI Thread");
        }
        //#enddebug

        //#debug
        L.i("Start joinAll(" + timeout + ")", "numberOfTasks=" + tasks.length);
        final long startTime = System.currentTimeMillis();
        try {
            for (int i = 0; i < tasks.length; i++) {
                final long timeLeft = startTime + timeout - System.currentTimeMillis();

                if (timeLeft <= 0) {
                    throw new TimeoutException("joinAll(" + timeout + ") timout exceeded (" + timeLeft + "): " + tasks[i]);
                }
                tasks[i].join(timeout);
            }
        } finally {
            //#debug
            L.i("End joinAll(" + timeout + ")", "numberOfTasks=" + tasks.length + " timeElapsed=" + (System.currentTimeMillis() - startTime));
        }
    }

    /**
     * Find out the execution state of the Task
     *
     * @return
     */
    public final int getStatus() {
        synchronized (mutex) {
            return status;
        }
    }

    /**
     * Return the current status as a string for easy debug information display
     *
     * @return
     */
    public final String getStatusString() {
        synchronized (mutex) {
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
            throw new IllegalArgumentException(getClassName() + ": do not setStatus(Task.CANCELED). Call Task.cancel(false, \"Reason for cancel\") instead to keep your code debuggable");
        }

        doSetStatus(status, "");
    }

    private void doSetStatus(final int status, final String reason) {
        final Task t;
        synchronized (mutex) {
            if (this.status == status) {
                //#debug
                L.i(this, "State change from " + getStatusString() + " to " + Task.STATUS_STRINGS[status] + " is ignored", this.toString());
                return;
            }
            if (this.status >= FINISHED) {
                //#debug
                L.i(this, "(Task already FINISHED or CANCELED) State change from " + getStatusString() + " to " + Task.STATUS_STRINGS[status] + " is ignored", this.toString());
                return;
            }
            this.status = status;
            t = chainedTask;
            if (status == CANCELED) {
                /*
                 * Unchain as we cancel to simplify avoiding any future reference to
                 * canceled chained tasks. This can also speed garbage collection.
                 */
                chainedTask = null;
            }
            mutex.notifyAll();
        }

        if (status == CANCELED) {
            PlatformUtils.getInstance().runOnUiThread(new Runnable() {
                public void run() {
                    //#debug
                    L.i(this, "Firing onCanceled(" + reason + ")", Task.this.toString());
                    onCanceled(reason);
                }
            });
            // Also cancel any chained Tasks expecting the output of this Task
            if (t != null) {
                t.cancel(false, "Previous task in chain was canceled: " + this);
            }
        }
    }

    /**
     * Execute the chained
     * <code>Task</code> after this
     * <code>Task</code>, using this
     * <code>Task</code>'s output as the nextTask input.
     *
     * <code>nextTask</code> will be
     * <code>fork()</code>ed at the same time as any previously
     * <code>chain()</code>ed
     * <code>Task</code>s for concurrent execution.
     *
     * nextTask == null is legal and has no effect.
     *
     * Each
     * <code>Task</code> in a chain will run at the same priority as the
     * previous
     * <code>Task</code> unless you explicitly set a different priority for it.
     * Best practice is for you to explicitly set the priority for each
     * <code>Task</code> in a chain for code clarity.
     *
     * If the
     * <code>Task</code> is already chained, this new
     * <code>Task</code> will be inserted into the chain immediately after the
     * current task.
     *
     * <code>insertAsNextLink</code> - The default value is
     * <code>false</code>, meaning multiple
     * <code>Task</code>s chained after this
     * <code>Task</code> will be
     * <code>fork()</code>ed in parallel for concurrent execution. Set
     * <code>insertAsNextLink = true</code> if you prefer to have
     * <code>nextTask</code> run serially before any previously
     * <code>chain()</code>ed
     * <code>Task</code>s. This is unusual but useful for example if you insert
     * a validator or change tactics to add a new approach when the first
     * <code>Task</code> results are not satisfactory.
     *
     * @param nextTask
     * @return this
     */
    public final Task chain(final Task nextTask) {
        if (nextTask == null) {
            //#debug
            L.i(this, "Ignoring chain(null)", "" + this);
            return this;
        }
        if (nextTask == this) {
            throw new IllegalArgumentException("Can not chain a task to itself");
        }
        synchronized (mutex) {
            if (getStatus() > Task.PENDING) {
                throw new IllegalStateException("Can not chain() to a Task unless it is still PENDING: " + this);
            }

            if (chainedTask == null) {
                chainedTask = nextTask;
            } else if (chainedTask instanceof ChainSplitter) {
                ((ChainSplitter) chainedTask).addSplit(nextTask);
            } else {
                chainedTask = new ChainSplitter(chainedTask, nextTask);
            }
            //#mdebug
            if (previousTaskInChain == nextTask) {
                L.i(this, "ERROR", "Do not chain a task to the task before it in a chain()" + showChain());
            }
            nextTask.setPreviousTaskInChain(this);
            L.i(this, "Chain added", showChain());
            //#enddebug

            return this;
        }
    }
    //#mdebug
    // Always access in a synchronized(MUTEX) block
    private Task previousTaskInChain = null;

    private void setPreviousTaskInChain(final Task previousTaskInChain) {
        if (previousTaskInChain == null) {
            throw new IllegalArgumentException("setPreiouvTaskInChain(null) not allowed");
        }

        this.previousTaskInChain = previousTaskInChain;
        /*
         * We want to eliminate long chains of used-up previous chain step which
         * can not be garbage collected. This is occurs naturally in a production
         * build since there are not backward references in the chain. But for
         * debug clarity, knowing where a Task receives it's value is useful. Thus
         * in the debug build we keep the previous step for debug display. This
         * referese reference might however cause artificial memory problems in
         * the debug build which do not apply to the production build, so to minimize
         * the issue we only keep one step back in the chain, not the entire previous
         * chain.
         */
        previousTaskInChain.previousTaskInChain = null;
    }
    //#enddebug

    /**
     * Return the priority value applied when this Task was fork()ed.
     *
     * If the Task is not yet fork()ed, for example if it is part of a Task
     * chain, this is the value which will be applied when it is forked.
     *
     * @return
     */
    public final int getForkPriority() {
        return forkPriority;
    }

    /**
     * You can call this as the return statement of your overriding method once
     * you have set the result
     *
     * @return
     */
    final Object executeTask(final Object in) {
        Object out;

        try {
            synchronized (mutex) {
                if (status == Task.CANCELED) {
                    throw new IllegalStateException(this.getStatusString() + " state can not be executed: " + this);
                }
            }
            /*
             * Execute the Task without holding any locks
             */
            out = exec(in);

            final boolean executionSuccessful;
            final Task t;
            synchronized (mutex) {
                executionSuccessful = status == Task.PENDING;
                if (executionSuccessful) {
                    value = out;
                    t = chainedTask;
                    setStatus(FINISHED);
                } else {
                    // Task was canceled
                    t = null;
                }
            }
            if (t != null) {
                //#debug
                L.i(this, "Begin fork chained task", t + " outputBecomesNextTaskInput=" + out);
                if (out != null) {
                    t.set(out);
                }
                t.fork();
            }
        } catch (final Throwable t) {
            final String s = "Exception during Task exec(): ";
            //#debug
            L.e(s, "" + this, t);
            cancel(false, s + " : " + this, t);
            out = null;
        }

        return out;
    }

    /**
     * Override to implement Worker thread code
     *
     * @param in
     * @return
     * @throws CancellationException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    protected abstract Object exec(Object in) throws CancellationException, TimeoutException, InterruptedException;

    /**
     * Cancel execution of this Task
     *
     * @param mayInterruptIfRunning
     * @param reason
     * @return
     */
    public final boolean cancel(final boolean mayInterruptIfRunning, final String reason) {
        return doCancel(mayInterruptIfRunning, reason, null, null);
    }

    /**
     * Cancel execution of this Task. This is called on the Worker thread
     *
     * Do not override this unless you also call super.cancel(boolean). You may
     * want to override this method instead of onCanceled() if you wish to
     * perform an action synchronously at the moment of cancellation, not some
     * milliseconds later on the UI thread.
     *
     * Override onCanceled() is the normal notification location, and is called
     * from the UI thread with Task state updates handled for you.
     *
     * @param mayInterruptIfRunning
     * @param reason
     * @param t
     * @return true if a Task was actually canceled
     */
    public boolean cancel(final boolean mayInterruptIfRunning, final String reason, final Throwable t) {
        return doCancel(mayInterruptIfRunning, reason, t, null);
    }
    
    boolean doCancel(final boolean mayInterruptIfRunning, final String reason, final Throwable t, Thread thread) {
        if (reason == null) {
            throw new IllegalArgumentException("For clean debug, you must provide a reason for cancel(), null will not do");
        }

        final String s = reason + " : " + t;

        synchronized (mutex) {
            if (status >= Task.FINISHED) {
                //#debug
                L.i(this, "Ignoring cancel(\"" + reason + "\" - " + s + ")", "Already FINISHED or CANCELED: " + this);

                return false;
            }
        }
        //#debug
        L.i(this, "Begin cancel \"" + reason + "\" mayInterruptIfRunning=" + mayInterruptIfRunning, s + " - " + this);
        if (mayInterruptIfRunning) {
            if (thread != null) {
                thread.interrupt();
            } else {
                thread = Worker.interruptTask(this);
            }
            //#debug
            L.i(this, "cancel(\"" + reason + "\")", "interrupt sent to thread=" + thread);

            synchronized (mutex) {
                if (status >= Task.FINISHED) {
                    //#debug
                    L.i(this, "End cancel(\"" + reason + "\")", "Successful interrupt sent and received, thread=" + thread);
                    return true;
                }
            }
            //#debug
            L.i(this, "cancel(\"" + reason + "\")", "Unable to find and interrupt() on known threads. Task is not currently running, so we will just change status and notify chained tasks by canceling them also");
        }
        doSetStatus(CANCELED, s);
        //#debug
        L.i(this, "End cancel(\"" + reason + "\")", "status=" + this.getStatusString() + " " + this);

        return true;
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
     * @param reason
     */
    protected void onCanceled(final String reason) {
        //#debug
        L.i(this, "default Task.onCanceled() - this method was not overridden: cancellationReason=" + reason, this.toString());
    }

    /**
     * Check of the task has been had cancel() called or has thrown an uncaught
     * exception while executing.
     *
     * @return
     */
    public final boolean isCanceled() {
        synchronized (mutex) {
            return status == AsyncTask.CANCELED;
        }
    }
    //#mdebug
    private String anonInnerClassName = null;

    /**
     * Get the real and possibly aritificially assigned debug class name for an
     * anonymous inner class. This helps the debug logs to be more easily
     * readable.
     *
     * @param o
     * @return
     */
    public static String getClassName(final Object o) {
        if (o instanceof Task) {
            return ((Task) o).getClassName();
        }
        if (o == null) {
            return "<null>";
        }

        return o.getClass().getName();
    }
    //#enddebug

    /**
     * Return the class name.
     *
     * If this is an anonymous inner class, the name return will include any
     * appended name you assigned by a previous call to setClassName(). This can
     * be useful for debugging since it is common to have anonymous inner
     * classes which extend Task that are run asynchronously.
     *
     * @return
     */
    public String getClassName() {
        String s = this.getClass().getName();
        //#mdebug
        synchronized (mutex) {
            if (anonInnerClassName != null) {
                s += anonInnerClassName;
            }
        }
        //#enddebug

        return s;
    }

    /**
     * You can call this to make your debug output more clear if needed since by
     * default anonymous inner classes don't have very useful names.
     *
     * This code will be automatically removed without a performance impact when
     * you obfuscate your final build using the production Tantalum.JAR
     *
     * @param name
     * @return
     */
    public Task setClassName(final String name) {
        //#debug
        this.anonInnerClassName = name;

        return this;




    }

    private static final class ChainSplitter extends Task {

        final Vector tasksToFork = new Vector();

        ChainSplitter(final Task t1, final Task t2) {
            super(Task.FASTLANE_PRIORITY);

            addSplit(t1);
            addSplit(t2);
        }

        void addSplit(final Task t) {
            if (t == this) {
                throw new IllegalArgumentException("Can not add split chain linking to itself: " + this + " - " + t);
            }
            if (tasksToFork.contains(t)) {
                //#debug
                L.i(this, "Duplicate chain warning", "You already chain()ed the same task to the same place: " + this + " - " + t);
                return;
            }
            tasksToFork.addElement(t);
            //#mdebug
            if (tasksToFork.size() > 1) {
                L.i(this, "Splitting the Task chain, tasks will be fork()ed for concurrentl execution", "" + this);
            }
            //#enddebug
        }

        protected Object exec(final Object in) {
            //#debug
            L.i(this, "Start chain split", "" + this);
            for (int i = 0; i < tasksToFork.size(); i++) {
                final Task t = (Task) tasksToFork.elementAt(i);

                if (in != null) {
                    t.set(in);
                }
                t.fork();
            }

            return in;
        }

        protected void onCanceled(final String reason) {
            //#debug
            L.i(this, "onCanceled(" + reason + ")", "" + this);

            final Task[] tasks;
            synchronized (tasksToFork) {
                tasks = new Task[tasksToFork.size()];

                tasksToFork.copyInto(tasks);
            }
            for (int i = 0; i < tasks.length; i++) {
                tasks[i].cancel(false, "Previous task in chain was canceled, then the chain split");
            }
        }

//#mdebug
        public String toString() {
            final StringBuffer sb = new StringBuffer();

            sb.append("ChainSplitter concurrent fork() tasks: ");
            synchronized (tasksToFork) {
                for (int i = 0; i < tasksToFork.size(); i++) {
                    final Task t = (Task) tasksToFork.elementAt(i);
                    if (t instanceof Task) {
                        sb.append(t.getClassName());
                        sb.append(" ");
                    }
                }
            }

            return sb.toString();
        }
//#enddebug
    }
    private static String[] PRIORITY_STRINGS = {"PRIORITY_NOT_SET", "SHUTDOWN", "IDLE_PRIORITY", "NORMAL_PRIORITY", "HIGH_PRIORITY", "SERIAL_PRIORITY", "FASTLANE_PRIORITY", "UI_PRIORITY", "DEDICATED_THREAD_PRIORITY"};

    String getPriorityString() {
        return PRIORITY_STRINGS[getForkPriority()];
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
        return toString(true);
    }

    /**
     * Show the pre- and post- tasks in the chain for debug
     *
     * Do not call within a synchronized block
     *
     * @return
     */
    private String showChain() {
        final StringBuffer sb = new StringBuffer();
        final Task previousTask;
        final Task nextTask;

        synchronized (mutex) {
            previousTask = previousTaskInChain;
            nextTask = chainedTask;
        }

        sb.append(L.CRLF);
        sb.append("   chain: ");
        if (previousTask == null) {
            sb.append("<null>");
        } else {
            sb.append(previousTask.getClassName());
        }
        sb.append(" -> this -> ");
        if (nextTask == null) {
            sb.append("<null>");
        } else {
            sb.append(nextTask.getClassName());
        }
        sb.append(L.CRLF);

        return sb.toString();
    }

    /**
     * Debug output for the Task
     *
     * @param showChain
     * @return
     */
    private String toString(final boolean showChain) {
        final String chain = showChain ? showChain() : "";

        synchronized (mutex) {
            StringBuffer sb = new StringBuffer(300);

            sb.append("{task=");
            sb.append(this.getClassName());
            sb.append(" status=");
            sb.append(getStatusString());
            sb.append(" priority=");
            sb.append(getPriorityString());
            sb.append(chain);
            sb.append("   value=");
            if (value instanceof byte[]) {
                sb.append("byte[");
                sb.append(((byte[]) value).length);
                sb.append(']');
            } else if (value instanceof Task) {
                sb.append(((Task) value).getClassName());
            } else {
                sb.append(value);
            }
            sb.append("}" + L.CRLF);

            return sb.toString();
        }
    }
//#enddebug    
}
