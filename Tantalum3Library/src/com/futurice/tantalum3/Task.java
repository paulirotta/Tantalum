/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3;

/**
 *
 * @author phou
 */
public abstract class Task implements Workable {
    // status values

    public static final int PENDING = 0;
    public static final int RUNNING = 1;
    public static final int FINISHED = 2;
    public static final int CANCELED = 3;
    public static final int EXCEPTION = 4;
    protected Object result = null; // Always access within a synchronized block
    protected int status = PENDING; // Always access within a synchronized block

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
        setStatus(FINISHED);
    }

    /**
     * Never call get() from the UI thread unless you know the state is FINISHED
     * (as it is in a Closure). Otherwise it can make your UI freeze for
     * unpredictable periods of time.
     *
     * @return
     * @throws InterruptedException
     * @throws CancellationException
     * @throws ExecutionException
     */
    public final synchronized Object get() throws InterruptedException, CancellationException, ExecutionException {
        switch (status) {
            case PENDING:
                Worker.tryUnfork(this);
                return compute();
            case RUNNING:
                this.wait();
            case FINISHED:
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
    public final synchronized Object join(final long timeout) throws InterruptedException, CancellationException, ExecutionException, TimeoutException {
        if (status == PENDING) {
            Worker.tryUnfork(this);
            return compute();
        } else if (status == RUNNING) {
            this.wait(timeout);
            if (status == RUNNING) {
                throw new TimeoutException();
            }
        }
        if (status == CANCELED) {
            throw new CancellationException();
        }
        if (status == EXCEPTION) {
            throw new ExecutionException();
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
        setStatus(FINISHED);
        return this.result;
    }

    /**
     * Cancel execution if possible.
     *
     * @param mayInterruptIfRunning
     * @return
     */
    public final synchronized boolean cancel(final boolean mayInterruptIfRunning) {
        switch (status) {
            case PENDING:
                setStatus(CANCELED);
                return true;
            case RUNNING:
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
}
