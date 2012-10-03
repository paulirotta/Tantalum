/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum4;

import com.futurice.tantalum4.log.L;

/**
 *
 * @author phou
 */
public abstract class Task implements Workable {
    // status values

    public static final int MAX_TIMEOUT = 120000; // If not specified, no thread can wait more than 2 minutes
    public static final int EXEC_PENDING = 1;
    public static final int EXEC_STARTED = 2;
    public static final int EXEC_FINISHED = 4;
    public static final int UI_RUN_FINISHED = 8; // for Closure extension
    public static final int CANCELED = 16;
    public static final int EXCEPTION = 32;
    public static final int READY = 64;
    private Object result = null; // Always access within a synchronized block
    protected int status = READY; // Always access within a synchronized block

    public Task() {
    }

    public Task(final Object in) {
        this.result = in;
    }

    /**
     * Check status of the object to ensure it can be queued at this time (it is
     * not already queued and running)
     *
     * @throws IllegalStateException if the task is currently queued or
     * currently running
     */
    public synchronized void notifyTaskQueued() throws IllegalStateException {
        if (status < EXEC_FINISHED || (this instanceof Runnable && status < UI_RUN_FINISHED)) {
            throw new IllegalStateException("Task can not be re-forked, wait for previous exec to complete: status=" + status);
        }
        setStatus(EXEC_PENDING);
    }

    protected final synchronized Object getResult() {
        return result;
    }

    public final Object get() throws InterruptedException, ExecutionException, CancellationException, TimeoutException {
        return join(MAX_TIMEOUT);
    }

    protected final synchronized Object setResult(final Object result) {
        return this.result = result;
    }

    /**
     * Never call join() from the UI thread. You might succeed with a very short
     * timeout, but this is still bad design and better handled with proper
     * worker threading.
     *
     * Similar to get(), except the total wait() time if the AsyncTask has not
     * completed is limited
     *
     * @param timeout
     * @return
     * @throws InterruptedException
     * @throws CancellationException
     * @throws ExecutionException
     */
    public final Object join(long timeout) throws InterruptedException, CancellationException, ExecutionException, TimeoutException {
        if (timeout < 0) {
            throw new IllegalArgumentException("Can not join() with timeout < 0: timeout=" + timeout);
        }
        boolean doExec = false;
        Object r;

        synchronized (this) {
        //#debug
        L.i("Start join", "timeout=" + timeout + " status=" + status);
            switch (status) {
                case EXEC_PENDING:
                    //#debug
                    L.i("Start join of EXEC_PENDING task", "timeout=" + timeout + " " + this.toString());
                    if (Worker.tryUnfork(this)) {
                        doExec = true;
                        break;
                    }
                case READY:
                case EXEC_STARTED:
                    //#debug
                    L.i("START WAIT", "status=" + status);
                    do {
                        final long t = System.currentTimeMillis();
                        
                        wait(timeout);
                        if (status == EXEC_FINISHED) {
                            break;
                        }
                        timeout -= System.currentTimeMillis() - t;
                    } while (timeout > 0);
                    //#debug
                    L.i("End join wait()", "status=" + status);
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
            r = result;
        }
        if (doExec) {
            //#debug
            L.i("Start exec() out-of-sequence exec() after join() and successful unfork()", this.toString());
            r = exec(r);
        }

        return r;
    }

    public final Task fork() {
        Worker.fork(this);

        return this;
    }

    public final Object joinUI(long timeout) throws InterruptedException, CancellationException, ExecutionException, TimeoutException {
        if (!(this instanceof Runnable)) {
            throw new ClassCastException("Can not joinUI() unless Task is a Runnable such as AsyncCallbackTask");
        }

        long t = System.currentTimeMillis();
        join(timeout);

        synchronized (this) {
            if (status < UI_RUN_FINISHED) {
                    //#debug
                    L.i("Start joinUI wait", "status=" + status);
                    timeout -= System.currentTimeMillis() - t;
                    do {
                        final long t2 = System.currentTimeMillis();                        
                        wait(timeout);
                        if (status == UI_RUN_FINISHED) {
                            break;
                        }
                        timeout -= System.currentTimeMillis() - t2;
                    } while (timeout > 0);
                    //#debug
                    L.i("End joinUI wait", "status=" + status);
                    if (status == EXEC_STARTED) {
                        throw new TimeoutException();
                    }
                
//                t = timeout - (System.currentTimeMillis() - t);
//                //#debug
//                L.i("Start joinUI wait()", "timeout remaining=" + t + " " + this.toString());
//                if (t > 0) {
//                    this.wait(t);
//                }
//                if (status < UI_RUN_FINISHED) {
//                    throw new TimeoutException();
//                }
//                //#debug
//                L.i("End joinUIThread wait()", this.toString());
            }

            return result;
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
                    L.i("Cancelled task", this.toString());
                    onCancelled();
                }
            });
        }
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
                    throw new IllegalStateException("Can not exec() AsyncTask: status=" + status);
                }
                result = in;
                setStatus(EXEC_STARTED);
            }
            out = doInBackground(in);

            final boolean doRun;
            synchronized (this) {
                result = out;
                doRun = status == EXEC_STARTED;
                if (doRun) {
                    setStatus(EXEC_FINISHED);
                }
            }
            if (this instanceof Runnable && doRun) {
                PlatformUtils.runOnUiThread(new Runnable() {
                    public void run() {
                        ((Runnable) Task.this).run();
                        setStatus(UI_RUN_FINISHED);
                    }
                });
            }
        } catch (final Throwable t) {
            //#debug
            L.e("Async task exception", this.toString(), t);
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
     * Override onCancelled() is the normal notification location, and is called
     * from the UI thread with Task state updates handled for you.
     *
     * @param mayInterruptIfRunning (not yet supported)
     * @return
     */
    public synchronized boolean cancel(final boolean mayInterruptIfRunning) {
        boolean cancelled = false;

        //#debug
        L.i("Cancel task", toString());
        switch (status) {
            case EXEC_STARTED:
                if (mayInterruptIfRunning) {
                    //FIXME find the task on a thread, then interrupt that thread
                    setStatus(CANCELED);
                    cancelled = true;
                }
                break;

            default:
                setStatus(CANCELED);
                cancelled = true;
        }

        return cancelled;
    }

    /**
     * This is executed on the UI thread
     *
     * Override if needed
     *
     * Use getStatus() to distinguish between CANCELLED and EXCEPTION states if
     * necessary.
     *
     */
    protected void onCancelled() {
    }

    public synchronized String toString() {
        return super.toString() + " status=" + status + " result=" + result;
    }
}
