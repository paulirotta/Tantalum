/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3;

import com.futurice.tantalum3.log.Log;

/**
 *
 * @author phou
 */
public abstract class Task implements Workable {
    // status values

    public static final int WORKER_THREAD_PENDING = 0;
    public static final int WORKER_THREAD_RUNNING = 1;
    public static final int WORKER_THREAD_FINISHED = 2;
    public static final int UI_THREAD_FINISHED = 3; // for Closure extension
    public static final int CANCELED = 4;
    public static final int EXCEPTION = 5;
    protected Object result = null; // Always access within a synchronized block
    protected int status = WORKER_THREAD_PENDING; // Always access within a synchronized block

    /**
     * You can call this at the end of your overriding method once you have set
     * the result.
     *
     * If you implement set(), do not implement or use compute()
     *
     * @param o
     */
    public synchronized void set(final Object o) {
        this.result = o;
        setStatus(WORKER_THREAD_FINISHED);
    }

    /**
     * Never call get() from the UI thread unless you know the state is
     * WORKER_THREAD_FINISHED (as it is in a Closure). Otherwise it can make
     * your UI freeze for unpredictable periods of time.
     *
     * @return
     * @throws InterruptedException
     * @throws CancellationException
     * @throws ExecutionException
     */
    public final synchronized Object get() throws InterruptedException, CancellationException, ExecutionException {
        switch (status) {
            case WORKER_THREAD_PENDING:
                Worker.tryUnfork(this);
                return compute();
            case WORKER_THREAD_RUNNING:
                this.wait();
            case WORKER_THREAD_FINISHED:
                return result;
            case CANCELED:
                throw new CancellationException();
            case EXCEPTION:
            default:
                throw new ExecutionException();
        }
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
    public synchronized Object join(final long timeout) throws InterruptedException, CancellationException, ExecutionException, TimeoutException {
        switch (status) {
            case WORKER_THREAD_PENDING:
                if (Worker.tryUnfork(this)) {
                    //#debug
                    Log.l.log("Start join out-of-sequence compute() after unfork", this.toString());
                    result = compute();
                    break;
                }
            case WORKER_THREAD_RUNNING:
                //#debug
                Log.l.log("Start join wait()", this.toString());
                this.wait(timeout);
                //#debug
                Log.l.log("End join wait()", this.toString());
                if (status == WORKER_THREAD_RUNNING) {
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

        return result;
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
    protected void setStatus(final int status) {
        synchronized (this) {
            this.status = status;
            this.notifyAll();
        }

        if (status == CANCELED || status == EXCEPTION) {
            PlatformUtils.runOnUiThread(new Runnable() {
                public void run() {
                    onCancel();
                }
            });
        }
    }

    /**
     * You can call this as the return statement of your overriding method once
     * you have set the result
     *
     * If you implement compute(), do not implement or use set()
     *
     * @return
     */
    public synchronized Object compute() {
        setStatus(WORKER_THREAD_FINISHED);
        return this.result;
    }

    /**
     * Cancel execution if possible.
     *
     * You may override this if for example you want to complete at task on the
     * current thread rather than the onCancel() call which will be on the UI
     * thread. In this case, be sure that you know which thread cancel() will be
     * called from. In Tantalum itself, cancel() is only called from a Worker
     * thread, so you can trust that such activity is done in the background
     * unless your code calls cancel() from the UI thread.
     *
     * @param mayInterruptIfRunning
     * @return
     */
    public synchronized boolean cancel(final boolean mayInterruptIfRunning) {
        switch (status) {
            case WORKER_THREAD_PENDING:
                setStatus(CANCELED);
                return true;
            case WORKER_THREAD_RUNNING:
                if (mayInterruptIfRunning) {
                    //TODO find the task on a thread, then interrupt that thread
                    setStatus(CANCELED);
                    return true;
                } else {
                    return false;
                }
            default:
        }

        return false;
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
    public void onCancel() {
    }

    public String toString() {
        return super.toString() + " status=" + status + " result=" + result;
    }
}
