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

import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 * A data model (as in MVC) that keeps the data in JSON format
 *
 * This model is thread safe and internally synchronized. You can synchronize
 * off this object to safely access multiple fields at once without interference
 * from model updates which may come in from other background threads.
 *
 * @author Paul Houghton
 */
public class JSONModel {

    private JSONObject jsonObject = new JSONObject();

    /**
     * Null constructor is an empty placeholder
     */
    public JSONModel() {
    }

    /**
     * Update the value of the model
     *
     * @param json
     * @throws JSONException
     */
    public synchronized void setJSON(final String json) throws JSONException {
        jsonObject = new JSONObject(json);
    }

    /**
     * Get a boolean by key
     *
     * @param key
     * @return
     * @throws JSONException
     */
    public synchronized boolean getBoolean(final String key) throws JSONException {
        return jsonObject.getBoolean(key);
    }

    /**
     * Get a String by key
     *
     * @param key
     * @return
     * @throws JSONException
     */
    public synchronized String getString(String key) throws JSONException {
        return jsonObject.getString(key);
    }

    /**
     * Get a double by key
     *
     * @param key
     * @return
     * @throws JSONException
     */
    public synchronized double getDouble(String key) throws JSONException {
        return jsonObject.getDouble(key);
    }

    /**
     * Get an int by key
     *
     * @param key
     * @return
     * @throws JSONException
     */
    public synchronized int getInt(String key) throws JSONException {
        return jsonObject.getInt(key);
    }

    /**
     * Get a long by key
     *
     * @param key
     * @return
     * @throws JSONException
     */
    public synchronized long getLong(String key) throws JSONException {
        return jsonObject.getLong(key);
    }
}
