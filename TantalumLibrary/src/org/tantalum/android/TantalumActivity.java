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
package org.tantalum.android;

import android.app.Activity;
import android.os.Bundle;
import org.tantalum.PlatformUtils;
import org.tantalum.Worker;

/**
 * Your main Activity in an Android application should extend TantalumActivity
 * to enable the library lifecycle.
 *
 * @author phou
 */
public abstract class TantalumActivity extends Activity {

    /**
     * A suggested number of background Worker threads for this platform. Use
     * this if you do not have a specific reason to increase or decrease it.
     * More threads means more concurrent operations such as HTTP GET, but can
     * increase context switching overhead and contention for scarce bandwidth
     * under poor network conditions. More threads is more useful on multi-core
     * hardware.
     *
     * It is a bit difficult to detect how many cores and Android phone has. It
     * is also not the place for the framework to do this automatically for you
     * as it may not be desired based on other application design concerns.
     * There are methods. See
     * http://stackoverflow.com/questions/7962155/how-can-you-detect-a-dual-core-cpu-on-an-android-device-from-code
     * and
     * http://makingmoneywithandroid.com/forum/showthread.php?tid=298&pid=1663#pid1663
     */
    protected static final int DEFAULT_NUMBER_OF_WORKER_THREADS = 8;

    /**
     * Your application's main Activity class should extend TantalumActivity and
     * call the super() constructor.
     *
     * @param numberOfWorkerThreads - Feel free to use
     * TantalumActivity.DEFAULT_NUMBER_OF_WORKER_THREADS
     */
    public TantalumActivity(int numberOfWorkerThreads) {
        super();

        PlatformUtils.getInstance().setProgram(this, numberOfWorkerThreads);
    }

    /**
     * Your Activity class should call super.onCreate() to initialize the
     * platform and create the background Worker thread pool.
     *
     * @param savedInstanceState
     */
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidCache.setContext(getApplicationContext());
    }

    /**
     * Your Activity class should call super.onDestroy() to give queued
     * background tasks such as write persistent data to the SQLite database
     * time to complete execution. This will automatically time out for you if
     * it takes more than 3 seconds to complete. The most common reason for a
     * slow response to Tantalum shutdown is one or more hung HTTP activities
     * which have not yet timed out.
     */
    protected void onDestroy() {
        Worker.shutdown(true); // Closed database and wait for similar orderly exit tasks

        super.onDestroy();
    }
}
