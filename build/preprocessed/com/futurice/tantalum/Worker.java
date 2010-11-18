/*
   Copyright 2010 Paul Eugene Houghton

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.futurice.tantalum;

import java.util.Vector;

/*
 * Tantalum Mobile Toolset
 * https://projects.forum.nokia.com/Tantalum
 *
 * Special thanks to http://www.futurice.com for support of this project
 * Project lead: paul.houghton@futurice.com
 *
 * Copyright 2010 Paul Eugene Houghton
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
public class Worker implements Runnable {

    private static final int NUMBER_OF_WORKERS = 2;
    private static final Vector q = new Vector();
    private static int count = 0;

    private Worker() {
        new Thread(this, "Worker" + count).start();
    }

    public static void queue(final Runnable runnable) {
        synchronized (q) {
            q.addElement(runnable);
            q.notifyAll();
            while (++count < NUMBER_OF_WORKERS) {
                new Worker();
            }
        }
    }

    public void run() {
        try {
            Runnable runnable = null;

            while (true) {
                synchronized (q) {
                    if (q.size() > 0) {
                        runnable = (Runnable) q.elementAt(0);
                        q.removeElementAt(0);
                    } else {
                        q.wait();
                    }
                }
                if (runnable != null) {
                    runnable.run();
                    runnable = null;
                }
            }
        } catch (Throwable t) {
            Log.logThrowable(t, "Worker error");
        }
    }
}
