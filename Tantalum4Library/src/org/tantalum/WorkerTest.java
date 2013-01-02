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

import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * Unit tests for the Worker class
 * 
 * @author phou
 */
public class WorkerTest extends TestCase {

    /**
     * Unit tests for the Worker class
     * 
     */
    public WorkerTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(2, "WorkerTest");
        PlatformUtils.setProgram(this);
    }

    /**
     * Perform unit tests by number
     * 
     * @param testNumber
     * @throws Throwable 
     */
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testRun();
                break;
            case 1:
                testQueue();
                break;
            default:
                break;
        }
    }

    private interface WorkableResult extends Workable {
        Object getResult();
    }
    
    /**
     * Test of testRun method, of class Worker.
     * 
     * @throws AssertionFailedException 
     */
    public void testRun() throws AssertionFailedException {
        System.out.println("run");
        final Object mutex = new Object();
        WorkableResult wr = new WorkableResult() {
            private Object o;
            
            public Object exec(final Object in) {
                synchronized (mutex) {
                    o = "yes";
                    mutex.notifyAll();
                }
                
                return in;
            }

            public Object getResult() {
                return o;
            }
        };
        
        Worker.fork(wr);
        synchronized (mutex) {
            try {
                mutex.wait(1000);
            } catch (InterruptedException ex) {
            }
        }
        
        assertEquals("yes", (String) wr.getResult());
        assertEquals(4, Worker.getNumberOfWorkers());
    }

    /**
     * Test of testQueue method, of class Worker.
     * 
     * @throws AssertionFailedException 
     */
    public void testQueue() throws AssertionFailedException {
        System.out.println("queue");
        Worker.fork(null);
        Worker.fork(new Workable() {

            public Object exec(final Object in) {
                return in;
            }            
        });
        Worker.fork(new Workable() {

            public Object exec(final Object in) {
                return in;
            }            
        });
    }
}
