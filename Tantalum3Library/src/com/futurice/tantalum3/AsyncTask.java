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
     * standard the "Workable.work()" is performed on a background Worker thread
     * and "Runnable.run()" is performed on the EDT.
     *
     * @param runnable
     */
    public static void execute(final Runnable runnable) {
        Worker.queueSerial(new Workable() {
            public boolean work() {
                runnable.run();

                return false;
            }
        }, ASYNC_TASK_WORKER_INDEX);
    }

    /**
     * Do the same thing for all params parallel
     *
     * This method must be invoked on the UI thread
     *
     * @param params
     * @return
     */
    public final AsyncTask executeOnExecutor(final Object[] params) {
        Worker.queue(startExecute(params));
        
        return this;
    }

    public final AsyncTask execute(final Object params) {
        Worker.queueSerial(startExecute(params), ASYNC_TASK_WORKER_INDEX);
        
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

            public boolean work() {
                try {
                    if (status == CANCELED) {
                        return true;
                    }
                    result = doInBackground(params);
                    status = FINISHED;
                } catch (final Throwable t) {
                    //#debug
                    Log.l.log("Async task exception", this.toString(), t);
                    status = CANCELED;
                }
                return true;
            }

            public void run() {
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
