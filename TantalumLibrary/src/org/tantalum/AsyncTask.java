/*
 Copyright (c) 2013, Paul Houghton and Futurice Oy
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
package org.tantalum;

/**
 * An implementation of Android's AsyncTask pattern for cross platform
 * application on JME and Android. On Android it is intended as a drop-in
 * replacement for the platform library class. The less commonly used features
 * of the Android implementation are not supported. Unlike the Android version,
 * you can join() or jointUI() a Tantalum AsyncTask to trigger out-of-sequence
 * execution or wait a specified max time interval for the AsyncTask to
 * complete. This design pattern is from Java 7's fork-join framework, but
 * instead of using for server scalability we use it here for convenience in
 * improving client side user experience.
 */
public abstract class AsyncTask extends Task {
    /*
     * Control if objects passed to executeOnExecutor() are thread safe to
     * allow parallel handling on the UI and a worker thread.
     * 
     * Set this "false" to duplacate exactly the Android AsyncTask threading. This
     * will slow the worker thread execution fork until after onPreExecute()
     * has completed on the UI thread. In most cases, this is not needed and the
     * object can be queued to both the worker and UI threads simultaneously for
     * better performance.
     * 
     * Access only within static synchronized blocks
     */

    private static boolean agressiveThreading = true;

    /*
     * Note that an AsyncTask is stateful, including parameters. You can therefore
     * not fork() an AsyncTask instance more than once at a time. It is recommended
     * you clone a task if you want to queue it multiple times- this would also affect
     * cancelletion logic ("which queued instance do you want to cancel?").
     */
    private volatile Object params = ""; // For default toString debug helper

    /**
     * When you call this, it means you don't want this object to be
     * simultaneously on both the background worker task queue and the UI task
     * queue.
     *
     * Call this one time for complete, conservative (but lower performance)
     * compatability with all Android SDK versions. You may also want to call
     * this if your suspect your AsyncTasks are not thread safe (!)
     *
     * The Android threading model has changed over time back and forth between
     * different release versions, apparently because many programmers with
     * using AsyncTask without making their code thread safe.
     */
    public static synchronized void disableAgressiveThreading() {
        AsyncTask.agressiveThreading = false;
    }

    /**
     * For compatability with Android, run one Runnable task on a single
     * background thread. Execution is guaranteed to be in the same order
     * objects are queued.
     *
     * @param runnable
     */
    public static void execute(final Runnable runnable) {
        (new Task() {
            public Object exec(final Object in) {
                runnable.run();

                return in;
            }
        }).fork(Task.SERIAL_PRIORITY);
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
        synchronized (this) {
            if (status != PENDING) {
                throw new IllegalStateException("AsyncTask can not be started multiple times, create a new AsynTask instance each time: " + this);
            }
        }
        PlatformUtils.getInstance().runOnUiThread(new Runnable() {
            public void run() {
                onPreExecute();
                AsyncTask.this.fork(Task.SERIAL_PRIORITY);
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
    public final AsyncTask executeOnExecutor(final Object params) {
        synchronized (this) {
            if (status <= FINISHED) {
                throw new IllegalStateException("AsyncTask can not be started, wait for previous exec to complete: status=" + getStatusString());
            }
            setStatus(PENDING);
        }

        final boolean aggressive;
        synchronized (AsyncTask.class) {
            aggressive = AsyncTask.agressiveThreading;
        }
        this.params = params;

        PlatformUtils.getInstance().runOnUiThread(new Runnable() {
            public void run() {
                onPreExecute();
                if (!aggressive) {
                    /*
                     * This is the sequence Android uses, but it is slow
                     * so by default agressive = true and the task is
                     * queued to both worker and UI thread simultaneously
                     */
                    AsyncTask.this.fork();
                }
            }
        });
        if (aggressive) {
            AsyncTask.this.fork();
        }

        return this;
    }

    /**
     * Override if needed
     *
     */
    protected void onPreExecute() {
    }

    /**
     * Call this from any thread to initiate a call toe onProgressUpdate() on
     * the UI thread
     *
     * @param progress
     */
    protected void publishProgress(final Object progress) {
        PlatformUtils.getInstance().runOnUiThread(new Runnable() {
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
     * @param progress
     */
    protected void onProgressUpdate(final Object progress) {
    }

    /**
     * This is visible as part of extending the Task class. It will call
     * onPostExecute() for you
     */
    public final void run() {
        onPostExecute(getValue());
    }

    /**
     * This is executed on the UI thread
     *
     * Override if needed
     *
     * @param result
     */
    protected void onPostExecute(Object result) {
    }

    /**
     * Complete the AsyncTask on a background Worker thread
     *
     * @param params
     */
    public void doInBackground(final Object params) {
        set(params);
        fork();
    }

    //#mdebug
    /**
     * Debug helper, override for more specific debug info if needed
     *
     * @return
     */
    public String toString() {
        return this.getClass().getName() + ", AsyncTask params: " + params.toString();
    }
    //#enddebug
}
