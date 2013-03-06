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

import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;
import org.tantalum.PlatformUtils;
import org.tantalum.util.PoolingWeakHashCache;

/**
 * Unit tests for PoolingWeakHashCache
 * 
 * @author phou
 */
public class PoolingWeakHashCacheTest extends TestCase {

    /**
     * Create a unit test harness
     */
    public PoolingWeakHashCacheTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(3, "PoolingWeakHashCacheTest");
        PlatformUtils.getInstance().setProgram(this, 4);
    }

    /**
     * Invoke tests by number
     * 
     * @param testNumber
     * @throws Throwable 
     */
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testClear();
                break;
            case 1:
                testRemove();
                break;
            case 2:
                testGetFromPool();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testClear method, of class PoolingWeakHashCache.
     * 
     * @throws AssertionFailedException 
     */
    public void testClear() throws AssertionFailedException {
        System.out.println("clear");
        PoolingWeakHashCache instance = new PoolingWeakHashCache();
        instance.put("key", "value");
        instance.remove("key");
        instance.clear();
        assertNull("Get from empty pool", instance.getFromPool());
    }

    /**
     * Test of testRemove method, of class PoolingWeakHashCache.
     * 
     * @throws AssertionFailedException 
     */
    public void testRemove() throws AssertionFailedException {
        System.out.println("remove");
        PoolingWeakHashCache instance = new PoolingWeakHashCache();
        instance.put("key", "value");
        instance.remove("key");
        instance.remove("other key");
        instance.remove(null);
    }

    /**
     * Test of testGetFromPool method, of class PoolingWeakHashCache.
     * 
     * @throws AssertionFailedException 
     */
    public void testGetFromPool() throws AssertionFailedException {
        System.out.println("getFromPool");
        PoolingWeakHashCache instance = new PoolingWeakHashCache();
        instance.put("key", "value");
        instance.put("key2", "value2");
        instance.remove("key");
        assertNotNull("Get from not-empty pool", instance.getFromPool());
        assertNull("Pool should be empty", instance.getFromPool());
    }
}
