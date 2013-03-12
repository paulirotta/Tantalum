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
