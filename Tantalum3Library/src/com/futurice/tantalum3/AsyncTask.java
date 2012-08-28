package com.futurice.tantalum3;

import com.futurice.tantalum3.log.Log;

/**
 *
 *
 */
public abstract class AsyncTask {

    /*
     * If tasks are called with execute(Params) then they will all execute in
     * guaranteed sequence on one Worker thread. It is generally better to use
     * executeOnExecutor(Params) instead unless
     */
    public static final int ASYNC_TASK_WORKER_INDEX = Worker.nextSerialWorkerIndex();
    public static final int PENDING = 1;
    public static final int RUNNING = 2;
    public static final int FINISHED = 3;
    public static final int CANCELED = 4;
    private volatile int status;
    private Object params = ""; // For default toString debug helper

    public AsyncTask() {
        status = PENDING;
    }

    /**
     * Cancel execution if possible.
     *
     * @param mayInterruptIfRunning
     * @return
     */
    public final boolean cancel(final boolean mayInterruptIfRunning) {
        switch (status) {
            case PENDING:
                status = CANCELED;
                return true;
            case RUNNING:
                if (mayInterruptIfRunning) {
                    status = CANCELED;
                    return true;
                } else {
                    return false;
                }
            default:
        }

        return false;
    }

    /**
     * For compatability with Android, run one Runnable task on a single background
     * thread in queued order.
     *
     * NOTE: This Android use of "Runnable" is not consistent with the Tantalum
     * standard the "Workable.compute()" is performed on a background Worker thread
     * and "Runnable.run()" is performed on the EDT.
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
        Worker.forkSerial(startExecute(params), ASYNC_TASK_WORKER_INDEX);
        
        return this;
    }

    /**
     * Run the async task on a background thread pool, using the next available
     * thread. Execution order is not guaranteed, but this may return results
     * faster than execute() due to multi-core processors and concurrence benefits
     * during blocking IO calls.
     *
     * This method must be invoked on the UI thread
     *
     * @param params
     * @return
     */
    public final AsyncTask executeOnExecutor(final Object[] params) {
        Worker.fork(startExecute(params));
        
        return this;
    }

    /**
     * Do the same thing for all params
     *
     * This method must be invoked on the UI thread
     *
     * @param params
     * @return
     */
    public final Closure startExecute(final Object params) {
        status = RUNNING;
        onPreExecute();
        final Closure closure = new Closure() {
            Object result = null;

            public Object compute() {
                try {
                    if (status == CANCELED) {
                        return this;
                        //TODO FIXME update this once Task is seperated from Workable and we support error error responses on the UI thread
                    }
                    result = doInBackground(params);
                    status = FINISHED;
                } catch (final Throwable t) {
                    //#debug
                    Log.l.log("Async task exception", this.toString(), t);
                    status = CANCELED;
                }
                return this;
            }

            public void run() {
                //TODO FIXME update this once Task is seperated from Workable and we support error error responses on the UI thread
                if (status == CANCELED) {
                    onCancel();
                } else {
                    onPostExecute(result);
                }
            }
        };
        
        return closure;
    }

    public final int getStatus() {
        return status;
    }

    public final boolean isCancelled() {
        return status == CANCELED;
    }

    protected void onPreExecute() {
    }

    /**
     * This method will run in the background in parallel
     *
     * @param params
     * @return true if successful
     */
    protected abstract Object doInBackground(Object params);

    protected void publishProgress(Object progress) {
    }

    protected void onProgressUpdate() {
    }

    protected void onPostExecute(Object result) {
    }

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
