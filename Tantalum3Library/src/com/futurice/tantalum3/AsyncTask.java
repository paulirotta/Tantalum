package com.futurice.tantalum3;

import com.futurice.tantalum3.log.Log;

/**
 * An implementation of Android's AsyncTask pattern for J2ME. This can be used
 * on both J2ME and Android, and on Android it is intended as a drop-in
 * replacement to provide cross platform functionality.
 *
 * Note that if you are not set on using the Android pattern, you may prefer the
 * simpler Task (Java7-like fork-join), Closure (Task with UI thread completion)
 * or basic open loop Workable patterns.
 */
public abstract class AsyncTask extends Closure {

    public static final int PENDING = 0;
    public static final int RUNNING = 1;
    public static final int FINISHED = 2;
    public static final int CANCELED = 3;
    public static final int EXCEPTION = 4;

    /*
     * Control if objects passed to executeOnExecutor() are thread safe to
     * allow parallel handling on the UI and a worker thread.
     * 
     * Set this "false" to duplacate exactly the Android AsyncTask threading. This
     * will slow the worker thread execution fork until after onPreExecute()
     * has completed on the UI thread. In most cases, this is not needed and the
     * object can be queued to both the worker and UI threads simultaneously for
     * better performance.
     */
    public static boolean agressiveThreading = true;

    /*
     * If tasks are called with execute(Params) then they will all execute in
     * guaranteed sequence on one Worker thread. It is generally better to use
     * executeOnExecutor(Params) instead unless
     */
    public static final int ASYNC_TASK_WORKER_INDEX = Worker.nextSerialWorkerIndex();
    private Object params = ""; // For default toString debug helper
    protected Object result = null;
    private int status = PENDING; // Always access within a synchronized block

    /**
     * Never call get() from the UI thread, it will make your UI freeze for
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

    private synchronized void setStatus(final int status) {
        this.status = status;
        this.notifyAll();
    }

    /**
     * For compatability with Android, run one Runnable task on a single
     * background thread. Execution is guaranteed to be in the same order
     * objects are queued.
     *
     * NOTE: This Android use of "Runnable" is not consistent with the Tantalum
     * standard the "Workable.compute()" is performed on a background Worker
     * thread and "Runnable.run()" is performed on the EDT.
     *
     * @param runnable
     */
    public static void execute(final Runnable runnable) {
        Worker.forkSerial(new Workable() {
            public Object compute() {
                runnable.run();

                return null;
            }
        }, ASYNC_TASK_WORKER_INDEX);
    }

    /**
     * Run the async task on a single background thread. Note that this may be
     * slower than executeOnExecutor() but is preferred if you require execution
     * in the order in which execute() is called, or if execute() is not
     * thread-safe such that multiple execute() calls can not run in parallel.
     *
     * @param params
     * @return
     */
    public final AsyncTask execute(final Object params) {
        this.params = params;
        setStatus(PENDING);
        PlatformUtils.runOnUiThread(new Runnable() {
            public void run() {
                onPreExecute();
                Worker.forkSerial(AsyncTask.this, ASYNC_TASK_WORKER_INDEX);
            }
        });

        return this;
    }

    /**
     * Run the async task on a background thread pool, using the next available
     * thread. Execution order is not guaranteed, but this may return results
     * faster than execute() due to multi-core processors and concurrence
     * benefits during blocking IO calls.
     *
     * This method must be invoked on the UI thread
     *
     * @param params
     * @return
     */
    public final AsyncTask executeOnExecutor(final Object[] params) {
        final boolean agressive = AsyncTask.agressiveThreading;

        this.params = params;
        setStatus(PENDING);
        PlatformUtils.runOnUiThread(new Runnable() {
            public void run() {
                onPreExecute();
                if (!agressive) {
                    Worker.fork(AsyncTask.this);
                }
            }
        });
        if (!agressive) {
            Worker.fork(AsyncTask.this);
        }

        return this;
    }

    public final Object compute() {
        try {
            synchronized (this) {
                if (status == CANCELED || status == EXCEPTION) {
                    return result;
                }
                setStatus(RUNNING);
            }
            result = doInBackground(params);
            setStatus(FINISHED);
        } catch (final Throwable t) {
            //#debug
            Log.l.log("Async task exception", this.toString(), t);
            setStatus(EXCEPTION);
        }
        return result;
    }

    public final void run() {
        if (isCancelled()) {
            onCancel();
        } else {
            onPostExecute(result);
        }
    }

    public final synchronized boolean isCancelled() {
        return status == AsyncTask.CANCELED || status == EXCEPTION;
    }

    /**
     * Override if needed
     *
     */
    protected void onPreExecute() {
    }

    /**
     * This method will run in the background in parallel
     *
     * @param params
     * @return true if successful
     */
    protected abstract Object doInBackground(Object params);

    /**
     * Call this from any thread to initiate a call toe onProgressUpdate() on
     * the UI thread
     *
     * @param progress
     */
    protected void publishProgress(final Object progress) {
        PlatformUtils.runOnUiThread(new Runnable() {
            public void run() {
                onProgressUpdate(progress);
            }
        });
    }

    /**
     * This is executed on the UI thread
     *
     * Override if needed and use the volatile variable "progress"
     *
     */
    protected void onProgressUpdate(final Object progress) {
    }

    /**
     * This is executed on the UI thread
     *
     * Override if needed
     *
     */
    protected void onPostExecute(Object result) {
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
    protected void onCancel() {
    }

    /**
     * Debug helper, override for more specific debug info if needed
     *
     * @return
     */
    public String toString() {
        return this.getClass().getName() + ", AsyncTask params: " + params.toString();
    }
}
