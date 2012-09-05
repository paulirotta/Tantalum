/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3;

import com.futurice.tantalum3.log.L;

/**
 *
 * @author phou
 */
public abstract class Task implements Workable {
    // status values

    public static final int EXEC_PENDING = 0;
    public static final int EXEC_STARTED = 1;
    public static final int EXEC_FINISHED = 2;
    public static final int UI_RUN_FINISHED = 3; // for Closure extension
    public static final int CANCELED = 4;
    public static final int EXCEPTION = 5;
    protected Object result = null; // Always access within a synchronized block
    protected int status = UI_RUN_FINISHED; // Always access within a synchronized block

    /**
     * You can call this at the end of your overriding method once you have set
     * the result.
     *
     * If you implement set(), do not implement or use exec()
     *
     * @param o
     */
    public synchronized void set(final Object o) {
        this.result = o;
        setStatus(EXEC_FINISHED);

        if (this instanceof Runnable) {
            // Continue to closure
            PlatformUtils.runOnUiThread((Runnable) this);
        }
    }

    /**
     * Never call get() from the UI thread unless you know the state is
     * EXEC_FINISHED (as it is in a Closure). Otherwise it can make your UI
     * freeze for unpredictable periods of time.
     *
     * @return
     * @throws InterruptedException
     * @throws CancellationException
     * @throws ExecutionException
     */
    public final synchronized Object get() throws InterruptedException, CancellationException, ExecutionException {
        switch (status) {
            case EXEC_PENDING:
                Worker.tryUnfork(this);
                exec();
                return result;
            case EXEC_STARTED:
                this.wait();
            case EXEC_FINISHED:
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
            case EXEC_PENDING:
                if (Worker.tryUnfork(this)) {
                    //#debug
                    L.i("Start join out-of-sequence exec() after unfork", this.toString());
                    exec();
                    break;
                }
            case EXEC_STARTED:
                //#debug
                L.i("Start join wait()", this.toString());
                this.wait(timeout);
                //#debug
                L.i("End join wait()", this.toString());
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

        return result;
    }

    public final synchronized Object joinUI(final long timeout) throws InterruptedException, CancellationException, ExecutionException, TimeoutException {
        if (!(this instanceof Runnable)) {
            throw new ClassCastException("Can not joinUI() unless Task is a Closure");
        }
        
        final long t = System.currentTimeMillis();
        join(timeout);

        if (status < UI_RUN_FINISHED) {
            //#debug
            L.i("Start joinUIThread wait()", this.toString());
            this.wait(timeout - (System.currentTimeMillis() - t));
            if (status < EXEC_STARTED) {
                throw new TimeoutException();
            }
            //#debug
            L.i("End joinUIThread wait()", this.toString());
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
     * If you implement exec(), do not implement or use set()
     *
     * @return
     */
    public synchronized void exec() {
        setStatus(EXEC_FINISHED);
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
            case EXEC_PENDING:
                setStatus(CANCELED);
                return true;
            case EXEC_STARTED:
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
