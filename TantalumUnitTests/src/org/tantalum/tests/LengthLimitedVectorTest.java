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
package org.tantalum.tests;

import org.tantalum.util.LengthLimitedVector;

import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * LengthLimitedVector unit tests
 *
 * @author phou
 */
public class LengthLimitedVectorTest extends TestCase {

    private boolean tooLong = false;

    /**
     * Create a new test structure
     */
    public LengthLimitedVectorTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(2, "LengthLimitedLRUVectorTest");
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
                testAddElement();
                break;
            case 1:
                testLengthExceeded();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testAddElement method, of class LengthLimitedVector.
     *
     * @throws AssertionFailedException
     */
    public void testAddElement() throws AssertionFailedException {
        System.out.println("addElement");
        LengthLimitedVector instance = new LengthLimitedVector(3) {
            protected void lengthExceeded(Object extra) {
                tooLong = true;
            }
        };
        instance.addElement("a");
        instance.addElement("b");
        instance.addElement("c");
        instance.addElement("d");
        assertEquals("too long test", true, tooLong);
    }

    /**
     * Test of testLengthExceeded method, of class LengthLimitedVector.
     *
     * @throws AssertionFailedException
     */
    public void testLengthExceeded() throws AssertionFailedException {
        System.out.println("lengthExceeded");
        LengthLimitedVector instance = new LengthLimitedVector(3) {
            protected void lengthExceeded(Object o) {
            }
        };
        instance.addElement("a");
        instance.addElement("b");
        instance.addElement("c");
        assertEquals("Full length", 3, instance.size());
        instance.addElement("d");
        assertEquals("Max length", 3, instance.size());
        assertEquals("LRU after length exceeded", "b", instance.firstElement());
    }
}
