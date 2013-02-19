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
import jmunit.framework.cldc11.*;
import org.tantalum.CancellationException;
import org.tantalum.ExecutionException;
import org.tantalum.PlatformUtils;
import org.tantalum.Task;
import org.tantalum.TimeoutException;
import org.tantalum.UITask;
import org.tantalum.util.L;

/**
 * Unit tests for the Task class.
 *
 * @author phou
 */
public class TaskTest extends TestCase {

    /**
     * Unit tests for Task
     */
    public TaskTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(18, "TaskTest");

        PlatformUtils.setNumberOfWorkers(2);
        PlatformUtils.setProgram(this);
    }

    /**
     * Run unit tests by number
     *
     * @param testNumber
     * @throws Throwable
     */
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testFork();
                break;
            case 1:
                testSetStatus();
                break;
            case 2:
                testExec();
                break;
            case 3:
                testJoin();
                break;
            case 4:
                testGet();
                break;
            case 5:
                testSetResult();
                break;
            case 6:
                testNotifyTaskForked();
                break;
            case 7:
                testGetResult();
                break;
            case 8:
                testToString();
                break;
            case 9:
                testJoinUI();
                break;
            case 10:
                testCancel();
                break;
            case 11:
                testOnCanceled();
                break;
            case 12:
                testDoInBackground();
                break;
            case 13:
                testChain();
                break;
            case 14:
                testGetStatus();
                break;
            case 15:
                testJoinAll();
                break;
            case 16:
                testJoinAllUI();
                break;
            case 17:
                testCancelSelf();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testFork method, of class Task.
     *
     * @throws AssertionFailedException
     */
    public void testFork() throws AssertionFailedException {
        System.out.println("fork");
        Task instance = new Task() {
            protected Object doInBackground(Object in) {
                return "run";
            }
        };
        Task result_1 = instance.fork();
        assertEquals(instance, result_1);
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        try {
            assertEquals("run", (String) instance.get());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Can not get() : " + ex);
        }
    }

    /**
     * Test of testSetStatus method, of class Task.
     *
     * @throws AssertionFailedException
     */
    public void testSetStatus() throws AssertionFailedException {
        System.out.println("setStatus");
        Task instance = new Task() {
            protected Object doInBackground(Object in) {
                return "run";
            }
        };
        int status_1 = Task.UI_RUN_FINISHED;
        instance.setStatus(status_1);
        assertEquals("set status RUN_FINISHED", status_1, instance.getStatus());
    }

    /**
     * Test of testExec method, of class Task.
     *
     * @throws AssertionFailedException
     */
    public void testExec() throws AssertionFailedException {
        System.out.println("exec");
        Task instance = new Task() {
            protected Object doInBackground(Object in) {
                return (String) in + " rabbit";
            }
        };
        String out = (String) instance.exec("white");
        assertEquals("white rabbit", out);
        assertEquals("status is EXEC_FINISHED", Task.EXEC_FINISHED, instance.getStatus());
    }

    /**
     * Test of testJoin method, of class Task.
     *
     * @throws AssertionFailedException
     * @throws Exception
     */
    public void testJoin() throws AssertionFailedException, Exception {
        System.out.println("join");
        Task instance = new Task() {
            protected Object doInBackground(Object in) {
                return "run";
            }
        };
        instance.fork();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        String result_1 = (String) instance.join(1000);
        assertEquals("run", result_1);

        Task instance_2 = new Task() {
            protected Object doInBackground(Object in) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                return "run2";
            }
        };
        instance_2.fork();
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        String result_2 = (String) instance_2.join(1000);
        assertEquals("run2", result_2);

        Task instance_3 = new Task() {
            protected Object doInBackground(Object in) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                return "run3";
            }
        };
        instance_3.fork();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        try {
            String result_3 = (String) instance_3.join(10);
            fail("Should throw timeout on too-short join of running task");
        } catch (Exception e) {
        }

        Task blocker = new Task() {
            protected Object doInBackground(Object in) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                return "blocker";
            }
        };
        Task blocker2 = new Task() {
            protected Object doInBackground(Object in) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                return "blocker2";
            }
        };
        Task instance_4 = new Task() {
            protected Object doInBackground(Object in) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                return "run4";
            }
        };
        blocker.fork();
        blocker2.fork();
        instance_4.fork();
        try {
            instance_4.join(10);
            fail("Should throw timeout on too-short join of queued task");
        } catch (Exception e) {
        }
    }

    /**
     * Test of testGet method, of class Task.
     *
     * @throws AssertionFailedException
     * @throws Exception
     */
    public void testGet() throws AssertionFailedException, Exception {
        System.out.println("get");
        Task instance = new Task() {
            protected Object doInBackground(Object in) {
                return "result";
            }
        };
        Object expResult_1 = "result";
        instance.fork();
        Object result_1 = instance.get();
        assertEquals(expResult_1, result_1);

        Task instance2 = new Task() {
            protected Object doInBackground(Object in) {
                return "result2";
            }
        };
        Object expResult_2 = "result2";
        Object result_2 = instance2.get();
        assertEquals(expResult_2, result_2);

        Task instance3 = new Task() {
            protected Object doInBackground(Object in) {
                return "result3";
            }
        };
        instance3.fork();
        Thread.sleep(100);
        Object expResult_3 = "result3";
        Object result_3 = instance3.get();
        assertEquals(expResult_3, result_3);
    }

    /**
     * Test of testSetResult method, of class Task.
     *
     * @throws AssertionFailedException
     */
    public void testSetResult() throws AssertionFailedException {
        System.out.println("setResult");
        Task instance = new Task() {
            protected Object doInBackground(Object in) {
                return "result";
            }
        };
        instance.setValue("prezult");
        assertEquals("prezult", instance.getValue());
        instance.fork();
        try {
            assertEquals("result", instance.get());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Can not testSetResult(): " + ex);
        }
        instance.setValue("zult");
        assertEquals("zult", instance.getValue());
        instance.setValue(null);
        assertNull(instance.getValue());
    }

    /**
     * Test of testNotifyTaskForked method, of class Task.
     *
     * @throws AssertionFailedException
     * @throws Exception
     */
    public void testNotifyTaskForked() throws AssertionFailedException, Exception {
        System.out.println("notifyTaskForked");
        final Task instance = new Task() {
            protected Object doInBackground(Object in) {
                return "result";
            }
        };
        final Task t2 = (new Task() {
            protected Object doInBackground(Object in) {
                synchronized (instance) {
                    try {
                        instance.wait(200);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        fail("Interrupt while waiting notirfyTaskForked notification semaphore");
                    }
                    assertEquals(Task.EXEC_PENDING, instance.getStatus());
                }
                return "result";
            }
        }).fork();
        Thread.sleep(100);
        assertEquals("Forked task instance is READY", Task.READY, instance.getStatus());
        instance.notifyTaskForked();
        assertEquals("Forked task instance is EXEC_PENDING", Task.EXEC_PENDING, instance.getStatus());
        t2.cancel(true);
    }

    /**
     * Test of testGetResult method, of class Task.
     *
     * @throws AssertionFailedException
     */
    public void testGetResult() throws AssertionFailedException {
        System.out.println("getResult");
        final Task instance = new Task("start") {
            protected Object doInBackground(Object in) {
                return in;
            }
        };
        assertEquals("start", instance.getValue());
        instance.exec("next");
        assertEquals("next", instance.getValue());
    }

    /**
     * Test of testToString method, of class Task.
     *
     * @throws AssertionFailedException
     */
    public void testToString() throws AssertionFailedException {
        System.out.println("toString");
        final Task instance = new Task("start") {
            protected Object doInBackground(Object in) {
                return in;
            }
        };
        String result_1 = instance.toString();
        this.assertTrue(result_1.length() > 5);
    }

    /**
     * Test of testJoinUI method, of class Task.
     *
     * @throws AssertionFailedException
     * @throws Exception
     */
    public void testJoinUI() throws AssertionFailedException, Exception {
        System.out.println("joinUI");
        final Task instance = new UITask("big") {
            protected Object doInBackground(Object in) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                return in + " bunny";
            }

            protected void onPostExecute(Object result) {
                setValue(result + " bounce");
            }
        };
        instance.fork();
        Thread.sleep(20);
        instance.joinUI(300);
        assertEquals("big bunny bounce", instance.joinUI(300));
    }

    /**
     * Test of testCancel method, of class Task.
     *
     * @throws AssertionFailedException
     */
    public void testCancel() throws AssertionFailedException {
        final Vector errors = new Vector();
        System.out.println("cancel");
        final Task testCancelRunsToEnd = new Task("I AM") {
            protected Object doInBackground(Object in) {
                return in + " DONE";
            }
        };
        final Task testCancelInstance = new Task("test_cancel_instance") {
            protected Object doInBackground(Object in) {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException ex) {
                    errors.addElement("testCancel() Task testCancelInstance should not have been interrupted");
                }
                return in;
            }
        };
        final Task testCancelInstance2 = new Task("test_cancel_instance2") {
            protected Object doInBackground(Object in) {
                try {
                    Thread.sleep(400);
                    errors.addElement("testCancel() Task instance2 should have been interrupted");
                } catch (InterruptedException ex) {
                }
                return in;
            }
        };
        final Task testCancelInstance3 = new Task("test_cancel_instance3") {
            protected Object doInBackground(Object in) {
                return in;
            }
        };
        testCancelInstance.fork();
        testCancelInstance2.fork();
        testCancelInstance3.fork();
        testCancelRunsToEnd.fork();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        assertEquals("Cancel queued but not started task", true, testCancelInstance3.cancel(false));
        testCancelInstance2.cancel(true);
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        assertEquals("Soft cancel running task", false, testCancelInstance.cancel(false));
        assertEquals("Hard cancel running task", true, testCancelInstance2.cancel(true));
        try {
            testCancelInstance.get(); // Cleanup for next test
        } catch (Exception ex) {
            fail("join cleanup between tests: " + ex);
            ex.printStackTrace();
        }

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        final Task instance4 = new Task("instance4") {
            protected Object doInBackground(Object in) {
                try {
                    Thread.sleep(200);
                    errors.addElement("testCancel() Task instance4 should have been interrupted 4");
                } catch (InterruptedException ex) {
                }
                return in;
            }
        };
        final Task instance5 = new Task("instance5") {
            protected Object doInBackground(Object in) {
                try {
                    instance4.join(400);
                    errors.addElement("Should have canceled doInBackground 5");
                } catch (CancellationException ex) {
                } catch (Exception ex) {
                    errors.addElement("Exception on doInBackground cancel: " + ex);
                }

                return in;
            }
        };
        instance4.fork();
        instance5.fork();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        instance4.cancel(true);
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        assertEquals("I AM DONE", testCancelRunsToEnd.getValue());
        assertEquals(false, testCancelRunsToEnd.cancel(true));
        assertEquals(false, testCancelRunsToEnd.cancel(false));
        for (int i = 0; i < errors.size(); i++) {
            fail((String) errors.elementAt(i));
        }
    }

    /**
     * Test of testOnCanceled method, of class Task.
     *
     * @throws AssertionFailedException
     */
    public void testOnCanceled() throws AssertionFailedException {
        System.out.println("onCanceled");
        final Vector v = new Vector();
        final Task instance = new Task() {
            protected Object doInBackground(Object in) {
                try {
                    Thread.sleep(200);
                    fail("testOnCanceled() Task instance should have been interrupted");
                } catch (InterruptedException ex) {
                }
                return in;
            }

            protected void onCanceled() {
                v.addElement("canceled");
            }
        };
        instance.fork();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        instance.cancel(true);
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        assertEquals("canceled", (String) v.firstElement());
    }

    /**
     * Test of testDoInBackground method, of class Task.
     *
     * @throws AssertionFailedException
     */
    public void testDoInBackground() throws AssertionFailedException {
        System.out.println("doInBackground");
        final Task instance = new Task("ca") {
            protected Object doInBackground(Object in) {
                return (String) in + "tty";
            }
        };
        instance.exec("be");
        try {
            assertEquals("betty", (String) instance.get());
        } catch (Exception ex) {
            fail("Could not testDoInBackground: " + ex);
        }
    }

    /**
     * Test of testChain method, of class Task.
     *
     * @throws AssertionFailedException
     */
    public void testChain() throws AssertionFailedException {
        System.out.println("chain");
        final Task instance = new Task("1") {
            protected Object doInBackground(Object in) {
                return (String) in + "2";
            }
        };
        final Task instance2 = new Task("bad") {
            protected Object doInBackground(Object in) {
                return in + "3";
            }
        };
        final Task instance3 = new Task("ugly") {
            protected Object doInBackground(Object in) {
                return in + "4";
            }
        };
        try {
            assertEquals("bad", (String) instance2.getValue());
            instance.chain(instance2).chain(instance3);
            instance.fork().join(200);
            assertEquals("12", (String) instance.get());
            assertEquals("123", (String) instance2.get());
            assertEquals("1234", (String) instance3.get());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Can not chain: " + ex);
        }
    }

    /**
     * Test of testGetStatus method, of class Task.
     *
     * @throws AssertionFailedException
     */
    public void testGetStatus() throws AssertionFailedException {
        System.out.println("getStatus");
        final Task instanceA = new Task() {
            protected Object doInBackground(Object in) {
                return in;
            }
        };
        final Task instanceB = new UITask() {
            protected void onPostExecute(Object result) {
                ;
            }
        };
        final Task instance2 = new Task() {
            protected Object doInBackground(Object in) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                }
                return in;
            }
        };
        final Task instance3 = new Task() {
            protected Object doInBackground(Object in) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                }
                return in;
            }
        };
        assertEquals(Task.READY, instanceA.getStatus());
        instance2.fork();
        instance3.fork();
        instanceA.fork();
        instanceB.fork();
        assertEquals(Task.EXEC_PENDING, instanceA.getStatus());
        instance2.cancel(true);
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        assertEquals(Task.EXEC_FINISHED, instanceA.getStatus());
        assertEquals(Task.UI_RUN_FINISHED, instanceB.getStatus());
    }

    /**
     * Test joinAll().
     *
     * @throws AssertionFailedException
     */
    public void testJoinAll() throws AssertionFailedException {
        System.out.println("joinAll");
        final Task task1 = new Task("1") {
            protected Object doInBackground(Object in) {
                return (String) in + "2";
            }
        };
        final Task task2 = new Task("3") {
            protected Object doInBackground(Object in) {
                return in + "4";
            }
        };
        final Task task3 = new Task("A") {
            protected Object doInBackground(Object in) {
                return (String) in + "2";
            }
        };
        final Task task4 = new Task("B") {
            protected Object doInBackground(Object in) {
                return in + "3";
            }
        };
        final Task task5a = new Task("fail_a") {
            protected Object doInBackground(Object in) {
                this.cancel(false);

                return in + "fail";
            }
        };
        final Task task5b = new Task("fail_b") {
            protected Object doInBackground(Object in) {
                this.cancel(false);

                return in + "fail";
            }
        };
        final Task task6 = new Task("slow") {
            protected Object doInBackground(Object in) {
                double j = Double.MIN_VALUE;
                for (int i = 0; i < 1000000; i++) {
                    j = j + i * 1.01;
                }

                return "" + j;
            }
        };

        try {
            task3.fork();
            Task[] tasks = {task1, task2};
            Task.joinAll(tasks, 102);
            assertEquals("1234", (String) task1.getValue() + (String) task2.getValue());

            Task[] moreTasks = {task3, task4};
            Task.joinAll(moreTasks, 102);
            assertEquals("A2B3", (String) task3.getValue() + (String) task4.getValue());

            Task[] exceptionTasks1 = {task1, task2, task3, task4, task5a};
            try {
                Task.joinAll(exceptionTasks1, 104);
                fail("joinAll() exceptoinTasks1 should have thrown an CancellationException, but did not");
            } catch (CancellationException e) {
                // Correct execution path
            }

            Task[] exceptionTasks2 = {task1, task2, task3, task4, task5b};
            try {
                Task.joinAll(exceptionTasks2, 105);
                fail("joinAll() exceptoinTasks2 should have thrown an CancellationException, but did not");
            } catch (CancellationException e) {
                // Correct execution path
            }

            Task[] slowTasks = {task1, task6, task3};
            try {
                Task.joinAll(slowTasks, 9);
                fail("joinAll() should have thrown a TimeoutException, but did not");
            } catch (TimeoutException e) {
                // Correct execution path                
            }

            try {
                Task.joinAll(null, 10);
                fail("joinAll() should have thrown an IllegalArgumentException for null, but did not");
            } catch (IllegalArgumentException e) {
                // Correct execution path                
            }


            try {
                Task.joinAll(tasks, -1);
                fail("joinAll() should have thrown an IllegalArgumentException for negative timeout, but did not");
            } catch (IllegalArgumentException e) {
                // Correct execution path                
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Can not joinAll: " + ex);
        }
    }

    /**
     * Test joinAll().
     *
     * @throws AssertionFailedException
     */
    public void testJoinAllUI() throws AssertionFailedException {
        System.out.println("joinAllUI");
        final UITask task1 = new UITask("1") {
            protected Object doInBackground(Object in) {
                return (String) in + "2";
            }

            protected void onPostExecute(Object result) {
                setValue(result + "UI");
            }
        };
        final UITask task2 = new UITask("3") {
            protected Object doInBackground(Object in) {
                return in + "4";
            }

            protected void onPostExecute(Object result) {
                setValue(result + "UI");
            }
        };
        final UITask task3 = new UITask("A") {
            protected Object doInBackground(Object in) {
                return (String) in + "2";
            }

            protected void onPostExecute(Object result) {
                setValue(result + "UI");
            }
        };
        final Task task4 = new Task("B") {
            protected Object doInBackground(Object in) {
                return in + "3";
            }
        };
        final UITask task5 = new UITask("fail") {
            protected Object doInBackground(Object in) {
                return in;
            }

            protected void onPostExecute(Object result) {
                L.i("UI thread BEFORE call to cancel", "" + result);
                this.cancel(true);
                L.i("UI thread AFTER call to cancel", "" + result);
            }
        };
        final UITask task6 = new UITask("slow") {
            protected Object doInBackground(Object in) {
                return in;
            }

            protected void onPostExecute(Object result) {
                try {
                    Thread.sleep(101);
                } catch (Exception e) {
                }
            }
        };

        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    task3.fork();
                    Task[] tasks = {task1, task2};
                    Task.joinAllUI(tasks, 505);
                    assertEquals("12UI34UI", (String) task1.getValue() + (String) task2.getValue());

                    Task[] moreTasks = {task3, task4};
                    Task.joinAllUI(moreTasks, 106);
                    assertEquals("A2UIB3", (String) task3.getValue() + (String) task4.getValue());

                    Task[] exceptionTasks = {task1, task2, task3, task4, task5};
                    try {
                        Task.joinAllUI(exceptionTasks, 107);
                        // Correct execution path
                    } catch (CancellationException e) {
                        fail("joinAllUI() should not throw an CancellationException from the UI thread, but it did not");
                    }

                    Task[] slowTasks = {task1, task6, task3};
                    try {
                        Task.joinAllUI(slowTasks, 11);
                        fail("joinAllUI() should have thrown a TimeoutException, but did not");
                    } catch (TimeoutException e) {
                        // Correct execution path                
                    }

                    try {
                        Task.joinAllUI(null, 12);
                        fail("joinAllUI() should have thrown an IllegalArgumentException for null, but did not");
                    } catch (IllegalArgumentException e) {
                        // Correct execution path                
                    }


                    try {
                        Task.joinAllUI(tasks, -2);
                        fail("joinAllUI() should have thrown an IllegalArgumentException for negative timeout, but did not");
                    } catch (IllegalArgumentException e) {
                        // Correct execution path                
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    fail("Can not joinAllUI: " + ex);
                }
            }
        };

        /*
         * We can't run UI tests from the UI thread, so make a clean thread
         * 
         * FIXME With this crap test framework, we can't test threading stuff.
         * It seems we would need to wait() for the test to finish, but waiting
         * for the UI thread would ruin some of the tests.
         */
        runnable.run();
//        Thread thread = new Thread(runnable);
//        thread.start();
    }

    /**
     * Test joinAll().
     *
     * @throws AssertionFailedException
     */
    public void testCancelSelf() throws AssertionFailedException {
        System.out.println("cancelSelf");
        final UITask task1a = new UITask("1") {
            protected Object doInBackground(Object in) {
                return (String) in + "2";
            }

            protected void onPostExecute(Object result) {
                setValue(result + "UI");
                // TEST FOR LOGIC ERROR- you can not cancel() after background execution completes
                cancel(true);
            }
        };
        final UITask task1b = new UITask("3") {
            protected Object doInBackground(Object in) {
                return (String) in + "4";
            }

            protected void onPostExecute(Object result) {
                // TEST FOR LOGIC ERROR- you can not cancel() after background execution completes
                setValue(result + "UI");
                cancel(true);
            }
        };
        final UITask task2 = new UITask("5") {
            protected Object doInBackground(Object in) {
                cancel(true);

                return in + "6";
            }

            protected void onPostExecute(Object result) {
                setValue(result + "UI");
            }
        };
        final UITask task3a = new UITask("7") {
            protected Object doInBackground(Object in) {
                try {
                    Thread.sleep(200);
                } catch (Exception e) {
                }
                return (String) in + "8";
            }

            protected void onPostExecute(Object result) {
                // TEST FOR LOGIC ERROR- you can not cancel() after background execution completes
                cancel(false);
                setValue(result + "UI");
            }
        };
        final UITask task3b = new UITask("9") {
            protected Object doInBackground(Object in) {
                try {
                    Thread.sleep(200);
                } catch (Exception e) {
                }
                return (String) in + "A";
            }

            protected void onPostExecute(Object result) {
                // TEST FOR LOGIC ERROR- you can not cancel() after background execution completes
                cancel(false);
                setValue(result + "UI");
            }
        };
        final Task task4a = new Task("B") {
            protected Object doInBackground(Object in) {
                cancel(false);

                return in + "C";
            }
        };
        final Task task4b = new Task("D") {
            protected Object doInBackground(Object in) {
                cancel(false);

                return in + "E";
            }
        };

        try {
            task1a.fork().join();
            assertEquals("task1a was not EXEC_FINISHED", Task.EXEC_FINISHED, task1a.getStatus());

            task1b.fork().joinUI();
            assertEquals("task1b was not UI_RUN_FINISHED", Task.UI_RUN_FINISHED, task1b.getStatus());

            task2.fork().join();
            assertEquals("task2 was not CANCELED", Task.CANCELED, task2.getStatus());

            task3a.fork().join();
            assertNotEquals("task3a was not CANCELED", Task.EXEC_FINISHED, task3a.getStatus());

            task3b.fork().joinUI();
            assertNotEquals("task3b was not CANCELED", Task.UI_RUN_FINISHED, task3b.getStatus());

            task4a.fork().join();
            assertEquals("task4a was not CANCELED", Task.CANCELED, task4a.getStatus());

            task4b.fork().joinUI();
            assertEquals("task4b was not CANCELED", Task.CANCELED, task4b.getStatus());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Can not testCancelSelf: " + ex);
        }
    }
}
