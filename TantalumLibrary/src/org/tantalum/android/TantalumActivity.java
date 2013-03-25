/*
 Copyright (c) 2013, Paul Houghton and Futurice Oy
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
package org.tantalum.android;

import android.app.Activity;
import org.tantalum.PlatformUtils;

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
    public TantalumActivity(final int numberOfWorkerThreads) {
        super();

        PlatformUtils.getInstance().setProgram(this, numberOfWorkerThreads, 0);
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
        PlatformUtils.getInstance().shutdown(true); // Closed database and wait for similar orderly exit tasks

        super.onDestroy();
    }
}
