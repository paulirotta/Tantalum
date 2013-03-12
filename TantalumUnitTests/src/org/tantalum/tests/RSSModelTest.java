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

import org.tantalum.net.xml.RSSModel;
import org.tantalum.util.StringUtils;
import org.xml.sax.SAXException;

/**
 * Unit tests for RSSModel
 *
 * @author phou
 */
public class RSSModelTest extends TestCase {

    byte[] xml;

    /**
     * RSSModel unit tests
     */
    public RSSModelTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(1, "RSSModelTest");

        PlatformUtils.getInstance().setProgram(this, 4, false); // Init debug
    }

    /**
     * Invoke unit tests by number
     *
     * @param testNumber
     * @throws Throwable
     */
    public void test(int testNumber) throws Throwable {
        xml = StringUtils.readBytesFromJAR("/rss.xml");
        switch (testNumber) {
            case 0:
                testParseElement();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testParseElement method, of class RSSModel.
     *
     * @throws AssertionFailedException
     */
    public void testParseElement() throws AssertionFailedException {
        System.out.println("parseElement");
        RSSModel instance = new RSSModel(100);
        try {
            instance.setXML(xml);
        } catch (Exception ex) {
            fail("Can not parse RSS: " + ex);
        }
        assertEquals("rss size", 86, instance.size());
        try {
            instance.setXML(null);
            fail("Should not attempt to parse null RSS");
        } catch (IllegalArgumentException e) {
            // Correct answer
        } catch (Exception ex) {
            fail("Can not parse null RSS: " + ex);
        }
        try {
            instance.setXML(new byte[0]);
            fail("Should not handle 0 byte RSS");
        } catch (IllegalArgumentException e) {
            // Correct answer
        } catch (Exception ex) {
            fail("Can not handle 0 byte RSS: " + ex);
        }
        try {
            instance.setXML(new byte[1]);
            fail("Should not handle 1 byte RSS");
        } catch (SAXException ex) {
            // Correct
        } catch (Exception e) {
            fail("Wrong exception on parse bad 1 byte RSS: " + e);
        }
    }
}
