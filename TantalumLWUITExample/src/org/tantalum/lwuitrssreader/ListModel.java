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
package org.tantalum.lwuitrssreader;

import com.sun.lwuit.events.DataChangedListener;
import com.sun.lwuit.list.DefaultListModel;
import org.tantalum.net.xml.RSSModel;
import org.tantalum.storage.DataTypeHandler;
import org.tantalum.util.L;
import org.xml.sax.SAXException;

/**
 *
 * @author tsaa
 */
public class ListModel extends DefaultListModel implements DataChangedListener, DataTypeHandler {

    private ListForm listForm;
    private final LiveUpdateRSSModel rssModel = new LiveUpdateRSSModel(60);

    public ListModel(ListForm listForm) {
        this.listForm = listForm;
        addDataChangedListener(this);
    }

    public void dataChanged(int type, int index) {
        listForm.repaint();
    }

    public void repaint() {
        listForm.repaint();
    }

    public Object convertToUseForm(byte[] bytes) {
        try {
            if (bytes.length > 0) {
                rssModel.setXML(bytes);
            }

            return this;
        } catch (NullPointerException e) {
            L.e("Null bytes when to RSSModel", "", e);
        } catch (Exception e) {
            L.e("Error converting bytes to RSSModel", "", e);
        }
        return null;
    }

    private class LiveUpdateRSSModel extends RSSModel {

        public LiveUpdateRSSModel(final int maxSize) {
            super(maxSize);
        }

        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            if (currentItem != null && qName.equals("item")) {
                ListModel.this.addItem(currentItem);
            }

            super.endElement(uri, localName, qName);
        }
    }
}
