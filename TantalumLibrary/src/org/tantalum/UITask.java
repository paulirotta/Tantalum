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

import org.tantalum.util.L;

/**
 * A unit of work which, once the Task.exec() is completed on a
 * background Worker thread, should update the user interface from the UI
 * Thread in onPostExecute().
 * 
 * This is the save behavior as an AsyncTask, but is suitable for short background
 * tasks since it does not include the hook for periodically updating the user
 * interface with ongoing progress.
 * 
 * @author phou
 */
public abstract class UITask extends Task implements Runnable {

    /**
     * Create a Task which will also run onPostExecute() on the user interface
     * (UI) thread after exec() completes on a Worker thread
     * 
     * The initial input value of the Task is null and may be set by a previous
     * Task if this is chain()ed to another Task.
     */
    public UITask() {
        super();
    }

    /**
     * Create a UITask and specify the initial input value. This type of UITask
     * does not need to be chained to a previous task to receive input parameters
     * so you can call fork() on it directly rather than chain()ing it to another
     * Task.
     * 
     * @param in 
     */
    public UITask(final Object in) {
        super(in);
    }

    /**
     * Execute onPostExecute() in the background with status transitions to the
     * Task state.
     * 
     * Do not call this directly. It will be called automatically on the UI thread
     * after exec() completes successfully on a background Worker thread.
     */
    public final void run() {
        try {
            final Object value = getValue();
            if (value != null) {
                onPostExecute(value);
            }
        } catch (Throwable t) {
            L.e("UITask onPostExecute uncaught error", this.toString(), t);
        } finally {
            if (this.status < UI_RUN_FINISHED) {
                setStatus(UI_RUN_FINISHED);
            }
        }
    }

    /**
     * You may optionally override this method if you wish to perform work on a
     * Worker thread before proceeding to the UI thread.
     *
     * @param in
     * @return
     */
    protected Object exec(final Object in) {
        return in;
    }

    /**
     * Override this method with the work you want to complete on the UI thread
     * after the Task is complete on the Worker thread.
     *
     * @param result
     */
    protected abstract void onPostExecute(Object result);
}
