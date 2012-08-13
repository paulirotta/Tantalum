package com.futurice.tantalum3;

/**
 *
 *
 */
public abstract class AsyncTask {

    public static final int PENDING = 1;
    public static final int RUNNING = 2;
    public static final int FINISHED = 3;
    public static final int CANCELED = 4;
    private volatile int status;

    public AsyncTask() {
        status = PENDING;
    }

    /**
     * Cancel execution if possible. 
     * 
     * @param mayInterruptIfRunning
     * @return 
     */
    public final boolean cancel(boolean mayInterruptIfRunning) {
        if (status < RUNNING) {
            status = CANCELED;
            return true;
        } else if (status == RUNNING) {
            if (mayInterruptIfRunning) {
                status = CANCELED;
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    //TODO: 
    public static void execute(Runnable runnable) {
    }

    // Do the same thing for all params parallel.
    public final AsyncTask executeOnExecutor(final Object[] params) {
        status = RUNNING;
        onPreExecute();
        Worker.queue(new AsyncParallelWorkable(params));
        return this;
    }

    // Do the same thing for all params in succession
    public final AsyncTask execute(final Object[] params) {
        onPreExecute();
        status = RUNNING;
        Worker.queue(new Workable() {

            public boolean work() {
                for (int i = 0; i < params.length; i++) {
                    final Object current = params[i];
                    if (status != CANCELED) {
                        doInBackground(current);
                    } else {
                        Worker.queueEDT(new Runnable() {

                            public void run() {
                                onCancel();
                            }
                        });
                    }
                }
                Worker.queueEDT(new Runnable() {

                    public void run() {
                        onPostExecute();
                    }
                });
                return true;
            }
        });
        return this;
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
     * This method will run in the background in parallell.
     *
     * @param param
     * @return true if successful
     */
    protected abstract boolean doInBackground(Object param);

    protected void publishProgress(Object progress) {
    }

    protected void onProgressUpdate() {
    }

    protected void onPostExecute() {
    }

    protected void onCancel() {
    }

    private class AsyncParallelWorkable implements Workable, Runnable {

        private Object[] params;
        private volatile int workers;

        /**
         * Create the workable with the objects it will do parallel work on.
         *
         * @param params
         */
        public AsyncParallelWorkable(Object[] params) {
            this.params = params;
            workers = params.length;
        }

        /**
         * Create other workables that work in paralell on the parameters
         *
         * @return
         */
        public boolean work() {            
            for (int i = 0; i < params.length; i++) {
                final Object current = params[i];
                
                Worker.queue(new Workable() {

                    public boolean work() {
                        boolean success = doInBackground(current);
                        updateDone(--workers);
                        return success;
                    }
                });
            }
            return true;
        }

        /**
         * When the workers are done, queue this on EDT
         *
         * @param doneWorkers
         */
        public void updateDone(int doneWorkers) {
            if (doneWorkers <= 0) {
                Worker.queueEDT(this);
            }
        }

        /**
         * Run onPostExecute on the EDT.
         */
        public void run() {
            onPostExecute();
        }
    }
}
