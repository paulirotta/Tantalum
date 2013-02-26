/*
 Copyright © 2012 Paul Houghton and Futurice on behalf of the Tantalum Project.
 All rights reserved.

 Tantalum software shall be used to make the world a better place for everyone.

 This software is licensed for use under the Apache 2 open source software license,
 http://www.apache.org/licenses/LICENSE-2.0.html

 You are kindly requested to return your improvements to this library to the
 open source community at http://projects.developer.nokia.com/Tantalum

 The above copyright and license notice notice shall be included in all copies
 or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package org.tantalum;

import org.tantalum.util.L;

/**
 * A Task is the base unit of work in the Tantalum toolset. Any piece of code
 * which runs in response to an event and does not explicitly need to be run on
 * the user interface (UI) thread is usually implemented in an extension of
 * Task.doInBackground(). It will automatically be executed on a background
 * worker thread once fork() has been called.
 *
 * @author phou
 */
public abstract class Task implements Workable {
    // status values

    /**
     * For join(), if a timeout is not specified, no thread can wait more than 2
     * minutes.
     */
    public static final int MAX_TIMEOUT = 120000; // 
    /**
     * status: created and forked for execution by a background Worker thread,
     * but no Worker has yet been free to accept this queued Task
     */
    public static final int EXEC_PENDING = 0;
    /**
     * status: doInBackground() has started but not yet finished
     */
    public static final int EXEC_STARTED = 1;
    /**
     * status: doInBackground() has finished. If this is a UI thread there still
     * may be pending activity for the user interface thread
     */
    public static final int EXEC_FINISHED = 2;
    /**
     * status: both Worker thread doInBackground() and UI thread onPostExecute()
     * have completed
     */
    public static final int UI_RUN_FINISHED = 3; // for Closure extension
    /**
     * state: cancel() was called
     */
    public static final int CANCELED = 4;
    /**
     * status: an uncaught exception was thrown during execution
     */
    public static final int EXCEPTION = 5;
    /**
     * status: the Task has been created, but fork() has not yet been called to
     * queue this for execution on a background Worker thread
     */
    public static final int READY = 6;
    private static final String[] STATUS_STRINGS = {"EXEC_PENDING", "EXEC_STARTED", "EXEC_FINISHED", "UI_RUN_FINISHED", "CANCELED", "EXCEPTION", "READY"};
    private Object value = null; // Always access within a synchronized block
    /**
     * The current execution state, one of several predefined constants
     */
    protected int status = READY; // Always access within a synchronized block
    /**
     * The next Task to be executed after this Task completes successfully. If
     * the current task is canceled or throws an exception, the chainedTask(s)
     * will have cancel() called to notify that they will not execute.
     */
    protected volatile Task chainedTask = null; // Run afterwords, passing output as input parameter

    /**
     * Create a Task with input value of null
     *
     * Use this constructor if your Task does not accept an input value,
     * otherwise use the Task(Object) constructor.
     *
     */
    public Task() {
    }

    /**
     * Create a Task with the specified input value.
     *
     * The default action is for the output value to be the same as the input
     * value, however many Tasks will return their own value.
     *
     * @param in
     */
    public Task(final Object in) {
        this.value = in;
    }

    /**
     * Check status of the object to ensure it can be queued at this time (it is
     * not already queued and running)
     *
     * @throws IllegalStateException if the task is currently queued or
     * currently running
     */
    public synchronized void notifyTaskForked() throws IllegalStateException {
        if (status < EXEC_FINISHED || (this instanceof Runnable && status < UI_RUN_FINISHED)) {
            throw new IllegalStateException("Task can not be re-forked, wait for previous exec to complete: status=" + getStatusString());
        }
        setStatus(EXEC_PENDING);
    }

    /**
     * Get the current input or result value of this Task without forcing
     * execution.
     *
     * If the task has not yet been executed, this will be the input value. If
     * the task has been executed, this will be the return value.
     *
     * @return
     */
    public final synchronized Object getValue() {
        return value;
    }

    /**
     * Execute synchronously if needed and return the resulting value.
     *
     * This is similar to join() with a very long timeout. Note that a
     * MAX_TIMEOUT of 2 minutes is enforced.
     *
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws CancellationException
     * @throws TimeoutException
     */
    public final Object get() throws InterruptedException, ExecutionException, CancellationException, TimeoutException {
        return join(MAX_TIMEOUT);
    }

    /**
     * Override the current value of the task with a new value
     *
     * Note that this is not used in normal operations. Usually you set the
     * value when creating the task, or pass in the value as an argument from
     * the previous Task in a chain. It can be useful to update a Task not yet
     * forked, or to override the normal result of a Task.
     *
     * Because you face a race condition when setting the value on Task which is
     * currently forked or already running, you will receive an
     * IllegalStateException. Only use this method before forking and after
     * execution has completed.
     *
     * @param value
     */
    public synchronized final void set(final Object value) {
        if (status < AsyncTask.EXEC_FINISHED) {
            throw new IllegalStateException("Unpredictable run sequence- can not set Task value when status is " + status);
        }

        this.value = value;
    }

    /**
     * Set the return value of this task during or at the end of execution.
     *
     * Note that although you can use this to set or override the initial input
     * value of a task before fork()ing it, it is more clear and preferred to
     * use the Task(Object) constructor to set the input value.
     *
     * @param value
     * @return
     */
    public final synchronized Object setValue(final Object value) {
        return this.value = value;
    }

    /**
     * Execute this task asynchronously on a Worker thread.
     *
     * This will queue the Task with Worker.NORMAL_PRIORITY
     *
     * @return
     */
    public final Task fork() {
        Worker.fork(this);

        return this;
    }

    /**
     * Execute this task asynchronously on a Worker thread at the specified
     * priority.
     *
     * @param priority
     * @return
     */
    public final Task fork(final int priority) {
        Worker.fork(this, priority);

        return this;
    }

    /**
     * Wait for up to MAX_TIMEOUT milliseconds for the Task to complete.
     *
     * @return
     * @throws InterruptedException
     * @throws CancellationException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public final Object join() throws InterruptedException, CancellationException, ExecutionException, TimeoutException {
        return join(MAX_TIMEOUT);
    }

    /**
     * Wait for a maximum of timeout milliseconds for the Task to run and return
     * it's evaluation value, otherwise throw a TimeoutExeception.
     *
     * Similar to get(), except the total wait() time if the AsyncTask has not
     * completed is explicitly limited to prevent long delays.
     *
     * Never call join() from the UI thread with a timeout greater than 100ms.
     * This is still bad design and better handled with a chained Task or
     * UITask. You will receive a debug warning, but are not prevented from
     * making longer join() calls from the user interface Thread.
     *
     * @param timeout in milliseconds
     * @return final evaluation result of the Task
     * @throws InterruptedException - task was running when it was explicitly
     * canceled by another thread
     * @throws CancellationException - task was explicitly canceled by another
     * thread
     * @throws ExecutionException - an uncaught exception was thrown
     * @throws TimeoutException - UITask failed to complete within timeout
     * milliseconds
     */
    public final Object join(long timeout) throws InterruptedException, CancellationException, ExecutionException, TimeoutException {
        if (timeout < 0) {
            throw new IllegalArgumentException("Can not join() with timeout < 0: timeout=" + timeout);
        }
        //#mdebug
        if (PlatformUtils.isUIThread() && timeout > 100) {
            L.i("WARNING- slow Task.join() on UI Thread", "timeout=" + timeout + " " + this);
        }
        //#enddebug
        boolean doExec = false;
        Object r;

        synchronized (this) {
            //#debug
            L.i("Start join", "timeout=" + timeout + " " + this);
            switch (status) {
                case EXEC_PENDING:
                    //#debug
                    L.i("Start join of EXEC_PENDING task", "timeout=" + timeout + " " + this.toString());
                    if (Worker.tryUnfork(this)) {
                        doExec = true;
                        break;
                    }
                // Continue to next state
                case READY:
                    if (status == READY) {
                        Worker.fork(this, Worker.HIGH_PRIORITY);
                    }
                // Continue to next state
                case EXEC_STARTED:
                    //#debug
                    L.i("Start join wait()", "status=" + getStatusString());
                    do {
                        final long t = System.currentTimeMillis();

                        wait(timeout);
                        if (status == EXEC_FINISHED) {
                            break;
                        }
                        if (status == CANCELED) {
                            throw new CancellationException("join() was to a Task which had already been canceled: " + this);
                        }
                        if (status == EXCEPTION) {
                            throw new ExecutionException("join() was to a Task which had already expereienced an uncaught runtime exception: " + this);
                        }
                        timeout -= System.currentTimeMillis() - t;
                    } while (timeout > 0);
                    //#debug
                    L.i("End join wait()", "status=" + getStatusString());
                    if (status == EXEC_STARTED) {
                        throw new TimeoutException("Task was already started when join() was call and did not complete during " + timeout + " milliseconds");
                    }
                    break;
                case CANCELED:
                    throw new CancellationException("join() was to a Task which was running but then canceled: " + this);
                case EXCEPTION:
                    throw new ExecutionException("join() was to a Task which had an uncaught exception: " + this);
                default:
                    ;
            }
            r = value;
        }
        if (doExec) {
            //#debug
            L.i("Start exec() out-of-sequence exec() after join() and successful unfork()", this.toString());
            r = exec(r);
        }

        return r;
    }

    /**
     * Wait up to a maximum specified timeout for all tasks in the list to
     * complete execution. If any or all of them have any problem, the first
     * associated Exception to occur will be thrown.
     *
     * @param tasks - list of Tasks to wait for
     * @param timeout - milliseconds, max total time from for all Tasks to
     * complete
     * @throws InterruptedException
     * @throws CancellationException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public static void joinAll(final Task[] tasks, final long timeout) throws InterruptedException, CancellationException, ExecutionException, TimeoutException {
        doJoinAll(tasks, timeout, false);
    }

    /**
     * Wait up to a maximum specified timeout for all tasks in the list to
     * complete execution including any follow up tasks on the UI thread. If any
     * or all of them have any problem, the first associated Exception to occur
     * will be thrown.
     *
     * Note that unlike joinUI(), it is legal to call joinUI() with some, or
     * all, of the Tasks being of type Task, not type UITask. This allows easier
     * mixing of Task and UITask in the list for convenience, where joinUI()
     * calls to a Task that is not a UITask() would be a logical error.
     *
     * @param tasks - list of Tasks to wait for
     * @param timeout - milliseconds, max total time from for all Tasks to
     * complete
     * @throws InterruptedException
     * @throws CancellationException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public static void joinAllUI(final Task[] tasks, final long timeout) throws InterruptedException, CancellationException, ExecutionException, TimeoutException {
        doJoinAll(tasks, timeout, true);
    }

    private static void doJoinAll(final Task[] tasks, final long timeout, final boolean joinUI) throws InterruptedException, CancellationException, ExecutionException, TimeoutException {
        if (tasks == null) {
            throw new IllegalArgumentException("Can not joinAll(), list of tasks to join is null");
        }
        if (timeout < 0) {
            throw new IllegalArgumentException("Can not joinAll() with timeout < 0: timeout=" + timeout);
        }
        //#mdebug
        if (PlatformUtils.isUIThread() && timeout > 100) {
            L.i("WARNING- slow Task.joinAll() on UI Thread", "timeout=" + timeout);
        }
        //#enddebug

        //#debug
        L.i("Start joinAll(" + timeout + ")", "numberOfTasks=" + tasks.length);
        long timeLeft = Long.MAX_VALUE;
        try {
            final long startTime = System.currentTimeMillis();
            for (int i = 0; i < tasks.length; i++) {
                final Task task = tasks[i];
                timeLeft = startTime + timeout - System.currentTimeMillis();

                if (timeLeft <= 0) {
                    throw new TimeoutException("joinAll(" + timeout + ") timout exceeded (" + timeLeft + ")");
                }
                if (joinUI && task instanceof UITask) {
                    task.joinUI(timeout);
                } else {
                    task.join(timeout);
                }
            }
        } finally {
            //#debug
            L.i("End joinAll(" + timeout + ")", "numberOfTasks=" + tasks.length + " timeElapsed=" + (timeout - timeLeft));
        }
    }

    /**
     * Wait up to MAX_TIMEOUT milliseconds for the UI thread to complete the
     * Task
     *
     * @return
     * @throws InterruptedException
     * @throws CancellationException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public final Object joinUI() throws InterruptedException, CancellationException, ExecutionException, TimeoutException {
        return joinUI(MAX_TIMEOUT);
    }

    /**
     * Wait for a maximum of timeout milliseconds for the UITask to complete if
     * needed and then also complete the followup action on the user interface
     * thread.
     *
     * You will receive a debug time warning if you are currently on the UI
     * thread and the timeout value is greater than 100ms.
     *
     * @param timeout in milliseconds
     * @return final evaluation result of the Task
     * @throws InterruptedException - task was running when it was explicitly
     * canceled by another thread
     * @throws CancellationException - task was explicitly canceled by another
     * thread
     * @throws ExecutionException - an uncaught exception was thrown
     * @throws TimeoutException - UITask failed to complete within timeout
     * milliseconds
     */
    public final Object joinUI(long timeout) throws InterruptedException, CancellationException, ExecutionException, TimeoutException {
        if (!(this instanceof UITask)) {
            throw new ClassCastException("Can not joinUI() unless Task is a UITask");
        }

        long t = System.currentTimeMillis();
        join(timeout);

        synchronized (this) {
            if (status < UI_RUN_FINISHED) {
                //#debug
                L.i("Start joinUI wait", "status=" + getStatusString());
                timeout -= System.currentTimeMillis() - t;
                while (timeout > 0) {
                    final long t2 = System.currentTimeMillis();
                    wait(timeout);
                    if (status == UI_RUN_FINISHED) {
                        break;
                    }
                    timeout -= System.currentTimeMillis() - t2;
                }
                //#debug
                L.i("End joinUI wait", "status=" + getStatusString());
                if (status < UI_RUN_FINISHED) {
                    throw new TimeoutException("JoinUI(" + timeout + ") failed to complete quickly enough");
                }
            }

            return value;
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
     * Return the current status as a string for easy debug information display
     *
     * @return
     */
    public final synchronized String getStatusString() {
        return Task.STATUS_STRINGS[status];
    }

    /**
     * Change the status
     *
     * You can only change status CANCELED or EXCEPTION to the READY state to
     * explicitly indicate you are going to re-use this Task. Note that Task
     * re-use is generally not advised, but can be valid if you have a special
     * need such as performance when re-creating the Task is particularly
     * expensive.
     *
     * @param status
     */
    public final void setStatus(final int status) {
        if (status < EXEC_PENDING || status > READY) {
            throw new IllegalArgumentException("setStatus(" + status + ") not allowed");
        }
        final Task t;
        synchronized (this) {
            if ((this.status == CANCELED || this.status == EXCEPTION) && status != READY) {
                //#debug
                L.i("State change from " + getStatusString() + " to " + Task.STATUS_STRINGS[status] + " is ignored", this.toString());
                return;
            }

            this.status = status;
            this.notifyAll();
            t = chainedTask;
        }

        if (status == CANCELED || status == EXCEPTION) {
            PlatformUtils.runOnUiThread(new Runnable() {
                public void run() {
                    //#debug
                    L.i("Task onCanceled()", Task.this.toString());
                    onCanceled();
                }
            });
            // Also cancel any chained Tasks expecting the output of this Task
            if (t != null) {
                t.cancel(false);
            }
        }
    }

    /**
     * Add a Task (or UITAsk, etc) which will run immediately after the present
     * Task and on the same Worker thread.
     *
     * The output result of the present task is fed as the input to
     * doInBackground() on the nextTask, so any processing changes can
     * propagated forward if the nextTask is so designed. This Task behavior may
     * thus be slightly different from the first Task in the chain, which by
     * default receives "null" as the input argument unless setValue() is called
     * before fork()ing the first task in the chain.
     *
     * Note that if the Task is already chained, the new additional nextTask
     * will be added after the last existing link in the chain.
     *
     * Note that you can not unchain. Do not start chaining until you know that
     * you really do want to chain. If you change your mind later before you
     * fork(), make a new Task. If you change your mind later after you fork(),
     * you may want to cancel() the Task already running, but cancel() for
     * simple logic reasons is usually indicative of a design problem, is bad
     * practice and constructing and starting then stopping Task chains will
     * decrease performance.
     *
     * Setting nextTask to null is treated as an application logic error and
     * will throw an IllegalArgumentException
     *
     * @param nextTask
     * @return nextTask
     */
    public final Task chain(final Task nextTask) {
        if (nextTask != null) {
            final Task multiLinkChain;
            synchronized (this) {
                if (this.chainedTask == null) {
                    this.chainedTask = nextTask;
                    multiLinkChain = null;
                } else {
                    // Already chained- we must add to the end of the chain
                    multiLinkChain = this.chainedTask;
                }
            }
            if (multiLinkChain != null) {
                /*
                 * Call this outside the above synchronized block so that we are not
                 * holding multiple locks for Tasks at the same time
                 */
                multiLinkChain.chain(nextTask);
            }
        }

        return nextTask;
    }

    /**
     * You can call this as the return statement of your overriding method once
     * you have set the result
     *
     * @return
     */
    public final Object exec(Object in) {
        Object out = in;

        try {
            synchronized (this) {
                if (in == null) {
                    in = value;
                } else {
                    value = in;
                }
                if (status == Task.CANCELED || status == Task.EXCEPTION) {
                    throw new IllegalStateException(this.getStatusString() + " state can not be executed: " + this);
                } else if (status != Task.EXEC_STARTED) {
                    setStatus(Task.EXEC_STARTED);
                }
            }
            out = doInBackground(in);

            final boolean doRun;
            final Task t;
            synchronized (this) {
                value = out;
                doRun = status == EXEC_STARTED;
                if (doRun) {
                    setStatus(EXEC_FINISHED);
                }
                t = chainedTask;
            }
            if (this instanceof UITask && doRun) {
                PlatformUtils.runOnUiThread((UITask) this);
            }
            if (t != null) {
                //#debug
                L.i("Begin exec chained task", chainedTask.toString() + " INPUT: " + out);
                t.exec(out);
                //#debug
                L.i("End exec chained task", chainedTask.toString());
            }
        } catch (final Throwable t) {
            //#debug
            L.e("Unhandled task exception", this.toString(), t);
            setStatus(EXCEPTION);
        }

        return out;
    }

    /**
     * Override to implement Worker thread code
     *
     * @param in
     * @return
     */
    protected abstract Object doInBackground(Object in);

    /**
     * Cancel execution if possible. This is called on the Worker thread
     *
     * Do not override this unless you also call super.cancel(boolean).
     *
     * Override onCanceled() is the normal notification location, and is called
     * from the UI thread with Task state updates handled for you.
     *
     * @param mayInterruptIfRunning (not yet supported)
     * @return
     */
    public synchronized boolean cancel(final boolean mayInterruptIfRunning) {
        boolean canceled = false;

        //#debug
        L.i("Begin explicit cancel task", "status=" + this.getStatusString() + " " + this);
        switch (status) {
            case EXEC_STARTED:
                if (mayInterruptIfRunning && (Worker.interruptWorkable(this))) {
                    setStatus(CANCELED);
                    canceled = true;
                }
                break;

            case EXEC_FINISHED:
                if (this instanceof UITask && mayInterruptIfRunning) {
                    if (Worker.interruptWorkable(this)) {
                        break;
                    }
                }
            case UI_RUN_FINISHED:
                //#debug
                L.i("Attempt to cancel Task after EXEC_FINISHED. Suspicious but may be normal due to race-to-cancel condition", this.toString());
                break;

            default:
                setStatus(CANCELED);
                canceled = true;
        }

        return canceled;
    }

    /**
     * This is executed on the UI thread
     *
     * Override if needed, the default implementation does nothing except
     * provide debug output.
     *
     * Use getStatus() to distinguish between CANCELED and EXCEPTION states if
     * necessary.
     *
     */
    protected void onCanceled() {
        //#debug
        L.i("Task canceled", this.toString());
    }

    /**
     * Check of the task has been had cancel() called or has thrown an uncaught
     * exception while executing.
     *
     * @return
     */
    public final synchronized boolean isCanceled() {
        return status == AsyncTask.CANCELED || status == EXCEPTION;
    }

    /**
     * For debug
     *
     * @return
     */
    public synchronized String toString() {
        return "TASK: status=" + getStatusString() + " result=" + value + " nextTask=(" + chainedTask + ")";
    }
}
