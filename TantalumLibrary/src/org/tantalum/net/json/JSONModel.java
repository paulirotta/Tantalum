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
 * The point of JSONModel is, like XMLModel, to allow you to create a local Object of type
 * JSONModel which may last the entire life of your application. You can then
 * refer to values from JSONModel and know that it always contains the latest,
 * best JSONObject received from the server or loaded from the local cache.
 * 
 * You can update the value in JSONModel by passing it as an object when you
 * call JSONGetter(url, myJSONMOdel). If you would like to execute some code such
 * as updating the user interface after the JSONModel is updated asynchonously,
 * you should chain() that continuation code (a Task or UITask) to the JSONGetter.
 * 
 * @author Paul Houghton
 */
public class JSONModel {

    private JSONObject jsonObject = new JSONObject();

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
