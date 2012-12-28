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
package org.tantalum.net;

/**
 * HTTP POST a message to a given URL
 *
 * @author phou
 */
public class HttpPoster extends HttpGetter {

    /**
     * HTTP POST a message to a given URL
     *
     * Make sure you call setMessage(byte[]) to specify what you want to POST or
     * you will get an IllegalArgumentException
     *
     * @param key - The url we will HTTP POST to, plus optional lines of text to
     * create a unique hashcode for caching this value locally.
     */
    public HttpPoster(final String key) {
        super(key);
    }

    /**
     * Create an HTTP POST operation
     * 
     * @param key
     * @param message 
     */
    public HttpPoster(final String key, final byte[] message) {
        super(key, message);
    }

    /**
     * Set the message to be HTTP POSTed to the server
     * 
     * @param message
     * @return 
     */
    public HttpPoster setMessage(final byte[] message) {
        if (message == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " was passed null message- meaningless POST or PUT operation: " + key);
        }
        this.postMessage = new byte[message.length];
        System.arraycopy(message, 0, this.postMessage, 0, message.length);

        return this;
    }
}
