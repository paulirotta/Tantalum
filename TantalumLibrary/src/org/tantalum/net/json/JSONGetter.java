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
package org.tantalum.net.json;

import org.tantalum.net.HttpGetter;
import org.tantalum.util.L;

/**
 *
 * @author Paul Houghton
 */
public class JSONGetter extends HttpGetter {

    private final JSONModel jsonModel;

    /**
     * Request with HTTP GET a JSONModel object update
     * 
     * @param key
     * @param jsonModel 
     */
    public JSONGetter(final String key, final JSONModel jsonModel) {
        super(key);
        this.jsonModel = jsonModel;
    }

    /**
     * Execute the update of the JSONModel value on a background Worker thread
     * 
     * @param in
     * @return 
     */
    public Object exec(final Object in) {
        String value = null;
        try {
            value = new String((byte[]) super.exec(in), "UTF8").trim();
            if (value.startsWith("[")) {
                // Parser expects non-array base object- add one
                value = "{\"base:\"" + value + "}";
            }
            jsonModel.setJSON(value);
        } catch (Exception e) {
            //#debug
            L.e("JSONGetter HTTP response problem", key + " : " + value, e);
            cancel(false, "JSONGetter exception - " + key + " : " + e);
        }

        return jsonModel;
    }
}
