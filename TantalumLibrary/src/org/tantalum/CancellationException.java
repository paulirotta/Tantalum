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
 * A queued Task had Task.cancel() called on it before it could complete
 * execution.
 * 
 * The return value of most tasks is not predictable and should not be used
 * when this exception is thrown. Any chained tasks will have cancel() called
 * to notify them that they will not be executed.
  * 
 * @author phou
 */
public class CancellationException extends Exception {
    
    /**
     * Create an exception indicating the a Task has been explicitly cancel()ed
     * 
     */
    public CancellationException() {
        super();
    }
    
    /**
     * Create an exception indicating the a Task has been explicitly cancel()ed
     * and explain why it was canceled.
     * 
     * @param explanation 
     */
    public CancellationException(final String explanation) {
        super(explanation);
    }
}
