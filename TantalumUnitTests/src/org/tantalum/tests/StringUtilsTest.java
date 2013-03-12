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

import java.util.Vector;
import javax.microedition.lcdui.Font;

import org.tantalum.util.StringUtils;

import jmunit.framework.cldc11.*;
import org.tantalum.PlatformUtils;

/**
 * Unit tests for StringUtils
 *
 * @author phou
 */
public class StringUtilsTest extends TestCase {

    /**
     * Create a new test harness
     */
    public StringUtilsTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(4, "StringUtilsTest");

        PlatformUtils.getInstance().setProgram(this, 4, false); // Init debug
    }

    /**
     * Test invocation by number
     *
     * @param testNumber
     * @throws Throwable
     */
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testReadStringFromJAR();
                break;
            case 1:
                testReadBytesFromJAR();
                break;
            case 2:
                testTruncate();
                break;
            case 3:
                testSplitToLines();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testReadStringFromJAR method, of class StringUtils.
     *
     * @throws AssertionFailedException
     * @throws Exception
     */
    public void testReadStringFromJAR() throws AssertionFailedException, Exception {
        System.out.println("readStringFromJAR");
        String name_1 = "/test.txt";
        String expResult_1 = "Hello, this is test text";
        String result_1 = StringUtils.readStringFromJAR(name_1);
        assertEquals(expResult_1, result_1);
    }

    /**
     * Test of testReadBytesFromJAR method, of class StringUtils.
     *
     * @throws AssertionFailedException
     * @throws Exception
     */
    public void testReadBytesFromJAR() throws AssertionFailedException, Exception {
        System.out.println("readBytesFromJAR");
        byte[] result_1 = StringUtils.readBytesFromJAR("/rss.xml");
        assertEquals(66547, result_1.length);
    }

    /**
     * Test of testTruncate method, of class StringUtils.
     *
     * @throws AssertionFailedException
     */
    public void testTruncate() throws AssertionFailedException {
        System.out.println("truncate");
        String str_1 = "This is a a really long line of text--------------------------------------------------------------------------------------------------------------------------------";
        Font font_1 = Font.getDefaultFont();
        int maxWidth_1 = 240;
        String expResult_1 = "This is a a really long line of te...";
        String result_1 = StringUtils.truncate(str_1, font_1, maxWidth_1);
        System.out.println(expResult_1);
        System.out.println(result_1);
        assertEquals(expResult_1, result_1);
    }

    /**
     * Test of testSplitToLines method, of class StringUtils.
     *
     * @throws AssertionFailedException
     */
    public void testSplitToLines() throws AssertionFailedException {
        System.out.println("splitToLines");
        Vector vector_1 = new Vector();
        String text_1 = "This is a a really long line of text";
        Font font_1 = Font.getDefaultFont();
        int maxWidth_1 = 100;
        StringUtils.splitToLines(vector_1, text_1, font_1, maxWidth_1);
        this.assertEquals(3, vector_1.size());
    }
}
