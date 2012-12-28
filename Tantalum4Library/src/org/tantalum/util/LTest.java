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
package org.tantalum.util;

import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;
import org.tantalum.PlatformUtils;


/**
 * Log utility unit tests
 * 
 * @author phou
 */
public class LTest extends TestCase {
    
    /**
     * Create a new unit tester
     */
    public LTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(1, "LogTest");
    }    

    /**
     * Invoke test by number
     * 
     * @param testNumber
     * @throws Throwable 
     */
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testLog();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testLog method, of class L.
     * 
     * @throws AssertionFailedException 
     */
    public void testLog() throws AssertionFailedException {
        System.out.println("log");
        L instance = PlatformUtils.getLog();
        String tag_1 = "";
        String message_1 = "";
        instance.i(tag_1, message_1);
        String tag_2 = "";
        String message_2 = "";
        Throwable th_2 = new Exception("Test Exception");
        instance.e(tag_2, message_2, th_2);
    }
}
