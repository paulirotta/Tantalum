/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.tantalum4;

import org.tantalum.tantalum4.log.L;

/**
 *
 * @author phou
 */
public abstract class Task implements Workable {
    // status values

    public static final int MAX_TIMEOUT = 120000; // If not specified, no thread can wait more than 2 minutes
    public static final int EXEC_PENDING = 0;
    public static final int EXEC_STARTED = 1;
    public static final int EXEC_FINISHED = 2;
    public static final int UI_RUN_FINISHED = 3; // for Closure extension
    public static final int CANCELED = 4;
    public static final int EXCEPTION = 5;
    public static final int READY = 6;
    private static final String[] STATUS_STRINGS = {"EXEC_PENDING", "EXEC_STARTED", "EXEC_FINISHED", "UI_RUN_FINISHED", "CANCELED", "EXCEPTION", "READY"};
    private Object value = null; // Always access within a synchronized block
    protected int status = READY; // Always access within a synchronized block
    protected volatile Task chainedTask = null; // Run afterwords, passing output as input parameter

    /**
     * Create a Task with input value of null
     *
     * Use this constructor if your Task does not accept an input value,
     * otherwise use the Task(Object) constructor.
     *
     */
    public Task() {
    }

    /**
     * Create a Task with the specified input value.
     *
     * The default action is for the output value to be the same as the input
     * value, however many Tasks will return their own value.
     *
     * @param in
     */
    public Task(final Object in) {
        this.value = in;
    }

    /**
     * Check status of the object to ensure it can be queued at this time (it is
     * not already queued and running)
     *
     * @throws IllegalStateException if the task is currently queued or
     * currently running
     */
    public synchronized void notifyTaskForked() throws IllegalStateException {
        if (status < EXEC_FINISHED || (this instanceof Runnable && status < UI_RUN_FINISHED)) {
            throw new IllegalStateException("Task can not be re-forked, wait for previous exec to complete: status=" + getStatusString());
        }
        setStatus(EXEC_PENDING);
    }

    /**
     * Get the current input or result value of this Task without forcing
     * execution.
     *
     * If the task has not yet been executed, this will be the input value. If
     * the task has been executed, this will be the return value.
     *
     * @return
     */
    protected final synchronized Object getValue() {
        return value;
    }

    /**
     * Execute synchronously if needed and return the resulting value.
     *
     * This is similar to join() with a very long timeout. Note that a
     * MAX_TIMEOUT of 2 minutes is enforced.
     *
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws CancellationException
     * @throws TimeoutException
     */
    public final Object get() throws InterruptedException, ExecutionException, CancellationException, TimeoutException {
        return join(MAX_TIMEOUT);
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
    protected final synchronized Object setValue(final Object value) {
        return this.value = value;
    }

    /**
     * Execute this task asynchronously on a Worker thread as soon as possible.
     *
     * This will queue the Task with Worker.NORMAL_PRIORITY
     *
     * @return
     */
    public final Task fork() {
        Worker.fork(this);

        return this;
    }

    /**
     * Wait for a maximum of timeout milliseconds for the Task to run and return
     * it's evaluation value, otherwise throw a TimeoutExeception.
     *
     * Similar to get(), except the total wait() time if the AsyncTask has not
     * completed is explicitly limited to prevent long delays.
     *
     * Never call join() from the UI thread with a timeout greater than 100ms.
     * This is still bad design and better handled with a chained Task or
     * UITask. You will receive a debug warning, but are not prevented from
     * making longer join() calls from the user interface Thread.
     *
     * @param timeout in milliseconds
     * @return final evaluation result of the Task
     * @throws InterruptedException - task was running when it was explicitly
     * canceled by another thread
     * @throws CancellationException - task was explicitly canceled by another
     * thread
     * @throws ExecutionException - an uncaught exception was thrown
     * @throws TimeoutException - UITask failed to complete within timeout
     * milliseconds
     */
    public final Object join(long timeout) throws InterruptedException, CancellationException, ExecutionException, TimeoutException {
        if (timeout < 0) {
            throw new IllegalArgumentException("Can not join() with timeout < 0: timeout=" + timeout);
        }
        if (PlatformUtils.isUIThread() && timeout > 100) {
            //#debug
            L.i("WARNING- slow Task.join() on UI Thread", "timeout=" + timeout + " " + this);
        }
        boolean doExec = false;
        Object r;

        synchronized (this) {
            //#debug
            L.i("Start join", "timeout=" + timeout + " " + this);
            switch (status) {
                case EXEC_PENDING:
                    //#debug
                    L.i("Start join of EXEC_PENDING task", "timeout=" + timeout + " " + this.toString());
                    if (Worker.tryUnfork(this)) {
                        doExec = true;
                        break;
                    }
                // Continue to next state
                case READY:
                    if (status == READY) {
                        Worker.fork(this, Worker.HIGH_PRIORITY);
                    }
                // Continue to next state
                case EXEC_STARTED:
                    //#debug
                    L.i("Start join wait()", "status=" + getStatusString());
                    do {
                        final long t = System.currentTimeMillis();

                        wait(timeout);
                        if (status >= EXEC_FINISHED) {
                            break;
                        }
                        timeout -= System.currentTimeMillis() - t;
                    } while (timeout > 0);
                    //#debug
                    L.i("End join wait()", "status=" + getStatusString());
                    if (status == EXEC_STARTED) {
                        throw new TimeoutException();
                    }
                    break;
                case CANCELED:
                    throw new CancellationException();
                case EXCEPTION:
                    throw new ExecutionException();
                default:
                    ;
            }
            r = value;
        }
        if (doExec) {
            //#debug
            L.i("Start exec() out-of-sequence exec() after join() and successful unfork()", this.toString());
            r = exec(r);
        }

        return r;
    }

    /**
     * Wait for a maximum of timeout milliseconds for the UITask to complete if
     * needed and then also complete the followup action on the user interface
     * thread.
     *
     * You will receive a debug time warning if you are currently on the UI
     * thread and the timeout value is greater than 100ms.
     *
     * @param timeout in milliseconds
     * @return final evaluation result of the Task
     * @throws InterruptedException - task was running when it was explicitly
     * canceled by another thread
     * @throws CancellationException - task was explicitly canceled by another
     * thread
     * @throws ExecutionException - an uncaught exception was thrown
     * @throws TimeoutException - UITask failed to complete within timeout
     * milliseconds
     */
    public final Object joinUI(long timeout) throws InterruptedException, CancellationException, ExecutionException, TimeoutException {
        if (!(this instanceof UITask)) {
            throw new ClassCastException("Can not joinUI() unless Task is a UITask");
        }

        long t = System.currentTimeMillis();
        join(timeout);

        synchronized (this) {
            if (status < UI_RUN_FINISHED) {
                //#debug
                L.i("Start joinUI wait", "status=" + getStatusString());
                timeout -= System.currentTimeMillis() - t;
                while (timeout > 0) {
                    final long t2 = System.currentTimeMillis();
                    wait(timeout);
                    if (status == UI_RUN_FINISHED) {
                        break;
                    }
                    timeout -= System.currentTimeMillis() - t2;
                };
                //#debug
                L.i("End joinUI wait", "status=" + getStatusString());
                if (status < UI_RUN_FINISHED) {
                    throw new TimeoutException();
                }
            }

            return value;
        }
    }

    /**
     * Find out the execution state of the Task
     *
     * @return
     */
    public final synchronized int getStatus() {
        return status;
    }

    /**
     * Return the current status as a string for easy debug information display
     *
     * @return
     */
    public final synchronized String getStatusString() {
        return Task.STATUS_STRINGS[status];
    }

    /**
     * Change the status
     *
     * @param status
     */
    protected synchronized final void setStatus(final int status) {
        this.status = status;
        this.notifyAll();

        if (status == CANCELED || status == EXCEPTION) {
            PlatformUtils.runOnUiThread(new Runnable() {
                public void run() {
                    //#debug
                    L.i("Task onCanceled()", Task.this.toString());
                    onCanceled();
                }
            });
            // Also cancel any chained Tasks expecting the output of this Task
            final Task t;
            synchronized (this) {
                t = chainedTask;
            }
            if (t != null) {
                t.cancel(false);
            }
        }
    }

    /**
     * Add a Task (or UITAsk, etc) which will run immediately after the present
     * Task and on the same Worker thread.
     *
     * The output result of the present task is fed as the input to
     * doInBackground() on the nextTask, so any processing changes can
     * propagated forward if the nextTask is so designed. This Task behavior may
     * thus be slightly different from the first Task in the chain, which by
     * default receives "null" as the input argument unless setValue() is called
     * before fork()ing the first task in the chain.
     *
     * @param nextTask
     * @return nextTask
     */
    public final synchronized Task chain(final Task nextTask) {
        //#mdebug
        if (nextTask == null) {
            L.i("WARNING", "chain(null) is probably a mistake- no effect");
        }
        //#enddebug
        this.chainedTask = nextTask;

        return nextTask;
    }

    /**
     * You can call this as the return statement of your overriding method once
     * you have set the result
     *
     * @return
     */
    public final Object exec(final Object in) {
        Object out = in;

        try {
            synchronized (this) {
                if (status == CANCELED || status == EXCEPTION || status == EXEC_STARTED) {
                    throw new IllegalStateException("Can not exec() Task: status=" + getStatusString() + " " + this);
                }
                value = in;
                setStatus(EXEC_STARTED);
            }
            out = doInBackground(in);

            final boolean doRun;
            final Task t;
            synchronized (this) {
                value = out;
                doRun = status == EXEC_STARTED;
                if (doRun) {
                    setStatus(EXEC_FINISHED);
                }
                t = chainedTask;
            }
            if (this instanceof UITask && doRun) {
                PlatformUtils.runOnUiThread((UITask) this);
            }
            if (t != null) {
                //#debug
                L.i("Begin exec chained task", chainedTask.toString() + " INPUT: " + out);
                t.exec(out);
                //#debug
                L.i("End exec chained task", chainedTask.toString());
            }
        } catch (final Throwable t) {
            //#debug
            L.e("Unhandled task exception", this.toString(), t);
            setStatus(EXCEPTION);
        }

        return out;
    }

    /**
     * Override to implement Worker thread code
     *
     * @param params
     */
    protected abstract Object doInBackground(Object in);

    /**
     * Cancel execution if possible. This is called on the Worker thread
     *
     * Do not override this unless you also call super.cancel(boolean).
     *
     * Override onCanceled() is the normal notification location, and is called
     * from the UI thread with Task state updates handled for you.
     *
     * @param mayInterruptIfRunning (not yet supported)
     * @return
     */
    public synchronized boolean cancel(final boolean mayInterruptIfRunning) {
        boolean canceled = false;

        //#debug
        L.i("Begin explicit cancel task", "status=" + this.getStatusString() + " " + this);
        switch (status) {
            case EXEC_STARTED:
                if (mayInterruptIfRunning) {
                    Worker.interruptWorkable(this);
                    setStatus(CANCELED);
                    canceled = true;
                }
                break;

            default:
                setStatus(CANCELED);
                canceled = true;
        }

        return canceled;
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
     * For debug
     *
     * @return
     */
    public synchronized String toString() {
        return super.toString() + " status=" + getStatusString() + " result=" + value + " nextTask=(" + chainedTask + ")";
    }
}
