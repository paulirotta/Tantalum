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

import jmunit.framework.cldc11.*;
import org.tantalum.PlatformUtils;

/**
 * Unit tests for WeakHashCache
 * 
 * @author phou
 */
public class WeakHashCacheTest extends TestCase {

    /**
     * Create the test harness
     */
    public WeakHashCacheTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(5, "WeakHashCacheTest");
        PlatformUtils.setProgram(this);
    }

    /**
     * Invoke unit tests by number
     * 
     * @param testNumber
     * @throws Throwable 
     */
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testRemove();
                break;
            case 1:
                testPut();
                break;
            case 2:
                testGet();
                break;
            case 3:
                testContainsKey();
                break;
            case 4:
                testSize();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testRemove method, of class WeakHashCache.
     * 
     * @throws AssertionFailedException 
     */
    public void testRemove() throws AssertionFailedException {
        System.out.println("remove");
        WeakHashCache instance = new WeakHashCache();
        String key1 = "";
        String key2 = "a";
        instance.remove(null);
        instance.remove(key1);
        instance.put(key1, "fruit");
        instance.put(key2, "cake");
        instance.remove(key1);
        instance.remove(key2);
        assertEquals(instance.size(), 0);
    }

    /**
     * Test of testPut method, of class WeakHashCache.
     * 
     * @throws AssertionFailedException 
     */
    public void testPut() throws AssertionFailedException {
        System.out.println("put");
        WeakHashCache instance = new WeakHashCache();
        String key1 = "";
        String key2 = "a";
        String expectedVal1 = "fruit";
        instance.put(key1, expectedVal1);
        instance.put(key2, "cake");
        try {
            instance.put(null, "dog");
            this.fail("Did not catch null key put to WeakHashCache");
        } catch (Exception e) {
        }
        assertEquals(instance.size(), 2);
        instance.put(key2, null);
        assertEquals(instance.size(), 1);
        for (int i = 0; i < 10000; i++) {
            instance.put("keykeykey" + i, "valuevaluevalue" + i);
        }
        Object val1 = instance.get(key1);
        if (val1 != null) {
            assertEquals(val1, expectedVal1);
        }
    }

    /**
     * Test of testGet method, of class WeakHashCache.
     * 
     * @throws AssertionFailedException 
     */
    public void testGet() throws AssertionFailedException {
        System.out.println("get");
        WeakHashCache instance = new WeakHashCache();
        String key1 = "11";
        String key2 = "22";
        Object value1 = "chocolate";
        Object value2 = "cake";
        Object result_1 = instance.get(key1);
        assertNull("get null", result_1);
        instance.put(key1, value1);
        instance.put(key2, value2);
        assertEquals("get val 1", value1, instance.get(key1));
        assertEquals("get val 2", value2, instance.get(key2));
    }

    /**
     * Test of testContainsKey method, of class WeakHashCache.
     * 
     * @throws AssertionFailedException 
     */
    public void testContainsKey() throws AssertionFailedException {
        System.out.println("containsKey");
        WeakHashCache instance = new WeakHashCache();
        String key1 = "11";
        String key2 = "22";
        Object value1 = "chocolate";
        Object value2 = "cake";
        instance.put(key1, value1);
        instance.put(key2, value2);
        assertEquals(false, instance.containsKey("red"));
        assertEquals(true, instance.containsKey(key1));
        try {
            instance.containsKey(null);
            fail("Did not throw exception on containsKey(null)");
        } catch (Exception e) {
        }
    }

    /**
     * Test of testSize method, of class WeakHashCache.
     * 
     * @throws AssertionFailedException 
     */
    public void testSize() throws AssertionFailedException {
        System.out.println("size");
        WeakHashCache instance = new WeakHashCache();
        final int length = 10000;
        for (int i = 0; i < length; i++) {
            instance.put("keykeykey" + i, "valuevaluevalue" + i);
        }
        assertEquals(length, instance.size());
    }
}
