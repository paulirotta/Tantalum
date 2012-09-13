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

    public static final int EXEC_PENDING = 1;
    public static final int EXEC_STARTED = 2;
    public static final int EXEC_FINISHED = 4;
    public static final int UI_RUN_FINISHED = 8; // for Closure extension
    public static final int CANCELED = 16;
    public static final int EXCEPTION = 32;
    private Object result = null; // Always access within a synchronized block
    protected int status = UI_RUN_FINISHED; // Always access within a synchronized block

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
    public final Object get() throws InterruptedException, CancellationException, ExecutionException {
        final Object r;
        synchronized (this) {
            switch (status) {
                case EXEC_PENDING:
                    Worker.tryUnfork(this);
                    break;
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
            r = result;
        }

        return exec(r);
    }

    protected final synchronized Object getResult() {
        return result;
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
    public final Object join(final long timeout) throws InterruptedException, CancellationException, ExecutionException, TimeoutException {
        if (timeout < 0) {
            throw new IllegalArgumentException("Can not join() with timeout < 0: timeout=" + timeout);
        }
        boolean doExec = false;
        final Object r;

        synchronized (this) {
            switch (status) {
                case EXEC_PENDING:
                    if (Worker.tryUnfork(this)) {
                        doExec = true;
                        break;
                    }
                case EXEC_STARTED:
                    //#debug
                    L.i("Start join wait()", "timeout=" + timeout + " " + this.toString());
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
            r = result;
        }
        if (doExec) {
            //#debug
            L.i("start out-of-sequence exec() after join() triggered unfork", this.toString());
            return exec(null);
        }

        return r;
    }

    public final void fork() {
        Worker.fork(this);
    }

    public final synchronized Object joinUI(final long timeout) throws InterruptedException, CancellationException, ExecutionException, TimeoutException {
        if (!(this instanceof Runnable)) {
            throw new ClassCastException("Can not joinUI() unless Task is a Runnable such as AsyncCallbackTask");
        }

        long t = System.currentTimeMillis();
        join(timeout);

        if (status < UI_RUN_FINISHED) {
            t = timeout - (System.currentTimeMillis() - t);
            //#debug
            L.i("Start joinUI wait()", "timeout remaining=" + t + " " + this.toString());
            if (t > 0) {
                this.wait(t);
            }
            if (status < UI_RUN_FINISHED) {
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
    protected final void setStatus(final int status) {
        synchronized (this) {
            this.status = status;
            this.notifyAll();
        }

        if (status == CANCELED || status == EXCEPTION) {
            PlatformUtils.runOnUiThread(new Runnable() {
                public void run() {
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
        try {
            synchronized (this) {
                if (status == CANCELED || status == EXCEPTION || status == EXEC_STARTED) {
                    throw new IllegalStateException("Can not exec() AsyncTask: status=" + status);
                }
                result = in;
                setStatus(EXEC_STARTED);
            }
            final Object r = doInBackground(in);
            synchronized (this) {
                result = r;
                setStatus(EXEC_FINISHED);
            }
            setStatus(EXEC_FINISHED);
            if (this instanceof Runnable) {
                PlatformUtils.runOnUiThread(new Runnable() {
                    public void run() {
                        ((Runnable) this).run();
                        setStatus(UI_RUN_FINISHED);
                    }
                });
            }

            return r;
        } catch (final Throwable t) {
            //#debug
            L.e("Async task exception", this.toString(), t);
            setStatus(EXCEPTION);
        }

        return in;
    }

    /**
     * Override to implement Worker thread code
     *
     * @param params
     */
    protected abstract Object doInBackground(Object in);

    /**
     * Cancel execution if possible.
     *
     * Generally you do not want to override this. onCancelled() is the normal
     * notification location, and is called from the UI thread.
     *
     * You may override this if for example you want to complete at task on the
     * worker thread, or if you need to prevent the state change because the
     * Task can still recover automatically from the situation.
     *
     * In this case, be sure that you know which thread cancel() will be called
     * from. In Tantalum itself, cancel() is only called from a Worker thread,
     * so you can trust that such activity is done in the background unless your
     * code accidentally calls cancel() from the UI thread.
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
    public void onCancelled() {
    }

    public String toString() {
        return super.toString() + " status=" + status + " result=" + result;
    }
}
