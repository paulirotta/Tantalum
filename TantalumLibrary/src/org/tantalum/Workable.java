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

/**
 * A task to be completed on the Worker thread
 * 
 * Conceptually this is the same as the Runnable interface, but helps to keep
 * clear which objects objects are intended for Worker threads and which for
 * other threads such as the event dispatch thread.
 * 
 * An object may implement both Workable and Closure to provide automatic passage
 * to the UI thread after completing a task on a Worker thread. This is used to
 * perform any UI cleanup. AsyncTask provides additional support for this
 * using a Java7-like fork-join pattern and an Android-like asynchronous
 * thread hand-off pattern and progress update pattern.
 * 
 * @author phou
 */
public interface Workable {
    
    /**
     * Do a task on a background thread, possibly returning a result for
     * asynchronous pipeline operations.
     * 
     * @param in 
     * @return the output result of the work, used for chain()ing one Task to
     * another Task in overriding classes. If you are implementing a Workable
     * directly instead of a Task, this return value is usually not used since
     * the Worker thread will ignore the result.
     */
    public Object exec(Object in);
}
