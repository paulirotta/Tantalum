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
package org.tantalum.tests;


import org.junit.Test;
import org.tantalum.*;
import org.tantalum.util.L;

import java.util.Vector;

import static org.junit.Assert.*;

/**
 * Unit tests for the Task class.
 *
 * @author phou
 */
public class TaskTest extends MockedStaticInitializers {
    @Test
    public void testAnonInnerClassNameOverride() {
        Task instance = new Task() {
            protected Object exec(Object in) {
                return "run";
            }
        }.setClassName("TestInnerClassName");
        
        assertEquals("Inner class name override is correct: " + instance.getClassName(), "org.tantalum.tests.TaskTest$1TestInnerClassName", instance.getClassName());
    }

    /**
     * Test of testFork method, of class Task.
     */
    @Test
    public void testFork() {
        System.out.println("fork");
        Task instance = new Task() {
            protected Object exec(Object in) {
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
     * Test of taskGetReturnsTheExecValue method, of class Task.
     */
    @Test
    public void taskGetReturnsTheExecValue() throws CancellationException, TimeoutException {
        Task instance2 = new Task("white") {
            protected Object exec(Object in) {
                System.out.println("Executing task 2");
                return (String) in + " rabbit";
            }
        };

        String out = (String) instance2.fork().get();
        assertEquals("white rabbit", out);
    }

    @Test(expected = TimeoutException.class)
    public void taskTimesOutIfJoinedBeforeFork() throws CancellationException, TimeoutException {
        Task instance = new Task("white") {
            protected Object exec(Object in) {
                System.out.println("Executing task 1");
                return (String) in + " rabbit";
            }
        };
        instance.join(400);
        fail("join() or get() to a Task that was not fork()ed should timeout");

        // FIXME: We don't seem to set the value to finished, bug in code, or in test?
        //assertEquals("status is FINISHED", Task.FINISHED, instance.getStatus());
    }

    /**
     * Test of testJoin method, of class Task.
     *
     * @throws Exception
     */
//    @Test
    public void testJoin() throws Exception {
        System.out.println("join");
        Task instance = new Task() {
            protected Object exec(Object in) {
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
            protected Object exec(Object in) {
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
            protected Object exec(Object in) {
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
            protected Object exec(Object in) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                return "blocker";
            }
        };
        Task blocker2 = new Task() {
            protected Object exec(Object in) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                return "blocker2";
            }
        };
        Task instance_4 = new Task() {
            protected Object exec(Object in) {
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
     * @throws Exception
     */
//    @Test
    public void testGet() throws Exception {
        System.out.println("get");
        Task instance = new Task() {
            protected Object exec(Object in) {
                return "result";
            }
        };
        Object expResult_1 = "result";
        instance.fork();
        Object result_1 = instance.get();
        assertEquals(expResult_1, result_1);

        Task instance2 = new Task() {
            protected Object exec(Object in) {
                return "result2";
            }
        };
        Object expResult_2 = "result2";
        Object result_2 = instance2.get();
        assertEquals(expResult_2, result_2);

        Task instance3 = new Task() {
            protected Object exec(Object in) {
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
     */
//    @Test
    public void testSetResult() {
        System.out.println("setResult");
        try {
            Task instance = new Task() {
                protected Object exec(Object in) {
                    return "result";
                }
            };
            Task instance2 = new Task() {
                protected Object exec(Object in) {
                    return "result";
                }
            };
            Task instance3 = new Task() {
                protected Object exec(Object in) {
                    return "result";
                }
            };
            instance.set("prezult");
            assertEquals("prezult", instance.get());
            instance2.fork();
            try {
                assertEquals("result", instance2.get());
            } catch (Exception ex) {
                ex.printStackTrace();
                fail("Can not testSetResult(): " + ex);
            }
            instance3.set("zult");
            assertEquals("zult", instance3.get());
            instance.set(null);
            assertNull(instance3.get());
        } catch (Exception e) {
            fail("Exception in testSetResult(): " + e);
        }
    }

    /**
     * Test of testNotifyTaskForked method, of class Task.
     *
     * @throws Exception
     */
//    @Test
    public void testNotifyTaskForked() throws InterruptedException, Exception {
        System.out.println("notifyTaskForked");
        final Task instance = new Task() {
            protected Object exec(Object in) {
                return "result";
            }
        };
        final Task t2 = (new Task() {
            protected Object exec(Object in) {
                synchronized (instance) {
                    try {
                        instance.wait(200);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        fail("Interrupt while waiting notirfyTaskForked notification semaphore");
                    }
                    assertEquals(Task.PENDING, instance.getStatus());
                }
                return "result";
            }
        }).fork();
        Thread.sleep(100);
        assertEquals("Forked task instance is PENDING", Task.PENDING, instance.getStatus());
        t2.cancel(true, "testing");
    }

    /**
     * Test of testGetResult method, of class Task.
     */
//    @Test
    public void testGetResult() {
        System.out.println("getResult");
        final Task instance = new Task("start") {
            protected Object exec(Object in) {
                return in;
            }
        };
        final Task instance2 = new Task("start") {
            protected Object exec(Object in) {
                return in;
            }
        };
        try {
            assertEquals("start", instance.get());
            instance2.set("next");
            assertEquals("next", instance2.get());
        } catch (Exception e) {
            fail("Exception in testGetResult: " + e);
        }
    }

    /**
     * Test of testToString method, of class Task.
     */
//    @Test
    public void testToString() {
        System.out.println("toString");
        final Task instance = new Task("start") {
            protected Object exec(Object in) {
                return in;
            }
        };
        String result_1 = instance.toString();
        assertTrue(result_1.length() > 5);
    }

    /**
     * Test of testCancel method, of class Task.
     */
//    @Test
    public void testCancel() {
        final Vector errors = new Vector();
        System.out.println("cancel");
        final Task testCancelRunsToEnd = new Task("I AM") {
            protected Object exec(Object in) {
                return in + " DONE";
            }
        };
        final Task testCancelInstance = new Task("test_cancel_instance") {
            protected Object exec(Object in) {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException ex) {
                    errors.addElement("testCancel() Task testCancelInstance should not have been interrupted");
                }
                return in;
            }
        };
        final Task testCancelInstance2 = new Task("test_cancel_instance2") {
            protected Object exec(Object in) {
                try {
                    Thread.sleep(400);
                    errors.addElement("testCancel() Task instance2 should have been interrupted");
                } catch (InterruptedException ex) {
                }
                return in;
            }
        };
        final Task testCancelInstance3 = new Task("test_cancel_instance3") {
            protected Object exec(Object in) {
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
        assertEquals("Cancel queued but not started task", true, testCancelInstance3.cancel(false, "testing"));
        testCancelInstance2.cancel(true, "testing");
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        assertEquals("Soft cancel running task", false, testCancelInstance.cancel(false, "testing"));
        assertEquals("Hard cancel running task", true, testCancelInstance2.cancel(true, "testing"));
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
            protected Object exec(Object in) {
                try {
                    Thread.sleep(200);
                    errors.addElement("testCancel() Task instance4 should have been interrupted 4");
                } catch (InterruptedException ex) {
                }
                return in;
            }
        };
        final Task instance5 = new Task("instance5") {
            @Override
            protected Object exec(Object in) {
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
        instance4.cancel(true, "testing");
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        try {
            assertEquals("I AM DONE", testCancelRunsToEnd.get());
            assertEquals(false, testCancelRunsToEnd.cancel(true, "testing"));
            assertEquals(false, testCancelRunsToEnd.cancel(false, "testing"));
            for (int i = 0; i < errors.size(); i++) {
                fail((String) errors.elementAt(i));
            }
        } catch (Exception e) {
            fail("Problem in testCancel: " + e);
        }
    }

    /**
     * Test of testOnCanceled method, of class Task.
     */
    //@Test
    public void onCanceledTest() {
        System.out.println("onCanceled");
        final Vector v = new Vector();
        v.addElement("Dummy");
        final Task instance = new Task() {
            @Override
            protected Object exec(Object in) {
                try {
                    Thread.sleep(200);
                    fail("testOnCanceled() Task instance should have been interrupted");
                } catch (InterruptedException ex) {
                }
                return in;
            }

            @Override
            protected void onCanceled(String reason) {
                v.insertElementAt("canceled-" + reason, 0);
            }
        };
        instance.fork();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        instance.cancel(true, "testing");
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        assertEquals("canceled-testing", (String) v.firstElement());
    }

    /**
     * Test of testDoInBackground method, of class Task.
     */
    @Test
    public void doInBackgroundTest() {
        System.out.println("doInBackground");
        final Task instance = new Task("ca") {
            @Override
            protected Object exec(Object in) {
                return (String) in + "tty";
            }
        };
        instance.set("be");
        try {
            assertEquals("betty", (String) instance.fork().get());
        } catch (Exception ex) {
            fail("Could not testDoInBackground: " + ex);
        }
    }

    /**
     * Test of testChain method, of class Task.
     */
//    @Test
    public void testChain() {
        System.out.println("chain");
        final Task instance = new Task("1") {
            protected Object exec(Object in) {
                return (String) in + "2";
            }
        };
        final Task instance2 = new Task("bad") {
            protected Object exec(Object in) {
                return in + "3";
            }
        };
        final Task instance3 = new Task("ugly") {
            protected Object exec(Object in) {
                return in + "4";
            }
        };
        final Task instanceA = new Task("A") {
            @Override
            protected Object exec(Object in) {
                return (String) in + "B";
            }
        };
        final Task instance5 = new Task("5") {
            @Override
            protected Object exec(Object in) {
                return (String) in + "6";
            }
        };
        final Task instance6 = new Task("BAD") {
            @Override
            protected Object exec(Object in) {
                return in + "7";
            }
        };
        try {
            instance.chain(instance2).chain(instance3);
            instance.fork().join(200);
            assertEquals("12", (String) instance.get());
            assertEquals("123", (String) instance2.get());
            assertEquals("1234", (String) instance3.get());

            instanceA.chain(instance5);
            instanceA.chain(null);
            instanceA.chain(instance6);
            instanceA.fork();
            assertEquals("AB", (String) instanceA.get());
            assertEquals("AB67", (String) instance6.get());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Can not chain: " + ex);
        }
    }

    /**
     * Test of testGetStatus method, of class Task.
     */
//    @Test
    public void testGetStatus() {
        System.out.println("getStatus");
        final Task instanceA = new Task() {
            protected Object exec(Object in) {
                return in;
            }
        };
        final Task instance2 = new Task() {
            protected Object exec(Object in) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                }
                return in;
            }
        };
        final Task instance3 = new Task() {
            protected Object exec(Object in) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                }
                return in;
            }
        };
        assertEquals(Task.PENDING, instanceA.getStatus());
        instance2.fork();
        instance3.fork();
        instanceA.fork();
        assertEquals(Task.PENDING, instanceA.getStatus());
        instance2.cancel(true, "testing");
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        assertEquals(Task.FINISHED, instanceA.getStatus());
    }

    /**
     * Test joinAll().
     */
//    @Test
    public void testJoinAll() {
        System.out.println("joinAll");
        final Task task1 = new Task("1") {
            protected Object exec(Object in) {
                return (String) in + "2";
            }
        };
        final Task task2 = new Task("3") {
            protected Object exec(Object in) {
                return in + "4";
            }
        };
        final Task task3 = new Task("A") {
            protected Object exec(Object in) {
                return (String) in + "2";
            }
        };
        final Task task4 = new Task("B") {
            protected Object exec(Object in) {
                return in + "3";
            }
        };
        final Task task5a = new Task("fail_a") {
            protected Object exec(Object in) {
                this.cancel(false, "testing");

                return in + "fail";
            }
        };
        final Task task5b = new Task("fail_b") {
            protected Object exec(Object in) {
                this.cancel(false, "testing");

                return in + "fail";
            }
        };
        final Task task6 = new Task("slow") {
            protected Object exec(Object in) {
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
            assertEquals("1234", (String) task1.get() + (String) task2.get());

            Task[] moreTasks = {task3, task4};
            Task.joinAll(moreTasks, 102);
            assertEquals("A2B3", (String) task3.get() + (String) task4.get());

            Task[] exceptionTasks1 = {task1, task2, task3, task4, task5a};
            try {
                Task.joinAll(exceptionTasks1, 104);
                // Correct execution path
            } catch (CancellationException e) {
                fail("joinAll() exceptoinTasks1 should not have thrown an CancellationException, but did");
            }

            Task[] exceptionTasks2 = {task1, task2, task3, task4, task5b};
            try {
                Task.joinAll(exceptionTasks2, 105);
                // Correct execution path
            } catch (CancellationException e) {
                fail("joinAll() exceptoinTasks2 should have thrown an CancellationException, but did not");
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
     */
//    @Test
    public void testCancelSelf() {
        System.out.println("cancelSelf");
        final Task task1a = new Task("1") {
            @Override
            protected Object exec(Object in) {
                return (String) in + "2";
            }

            @Override
            public void run() {
                try {
                    // TEST FOR LOGIC ERROR- you can not set() after background execution completes
                    set("UI");
                    fail("set() after run should have been stopped");
                } catch (Exception e) {
                }
            }
        }.setRunOnUIThreadWhenFinished(true);
        final Task task1b = new Task("3") {
            protected Object exec(Object in) {
                return (String) in + "4";
            }

            public void run(Object result) {
                // TEST FOR LOGIC ERROR- you can not cancel() after background execution completes
                try {
                    cancel(true, "testing");
                    fail("cancel() after run should have been stopped");
                } catch (Exception e) {
                }
            }
        };
        final Task task2 = new Task("5") {
            @Override
            protected Object exec(Object in) {
                cancel(true, "testing");

                return in + "6";
            }
        };
        final Task task3 = new Task("B") {
            protected Object exec(Object in) {
                cancel(false, "testing");

                return in + "C";
            }
        };
        final Task task4 = new Task("D") {
            protected Object exec(Object in) {
                cancel(false, "testing");

                return in + "E";
            }
        };

        try {
            task1a.fork(Task.FASTLANE_PRIORITY);
            Thread.sleep(300);
            assertEquals("task1a was FINISHED", Task.FINISHED, task1a.getStatus());
            assertEquals("task1a result was 12", "12", task1a.get());

            task1b.fork(Task.NORMAL_PRIORITY | Task.FASTLANE_PRIORITY).join();
            assertEquals("task1b was not EXEC_FINISHED", Task.FINISHED, task1b.getStatus());

            task2.fork().join();
            assertEquals("task2 should not have been CANCELED- you can not cancel() yourself", Task.FINISHED, task2.getStatus());

            task3.fork().join();
            assertNotEquals("task4a should not have been CANCELED", Task.CANCELED, task3.getStatus());

            try {
                task4.fork().join();
                fail("Task 4b- join() should throw ClassCastException if Task (not UITask)");
            } catch (ClassCastException e) {
                // Normal execution path
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Can not testCancelSelf: " + ex);
        }
    }

    //    @Test
    public void testCancelThread() {
        System.out.println("cancelSelf");
        final Task task1 = new Task("1") {
            @Override
            protected Object exec(Object in) {
                assertNotEquals("doInBackground() blue must not run on UI thread", true, PlatformUtils.getInstance().isUIThread());
                cancel(false, "Test cancel thread");
                return (String) in + "2";
            }

            @Override
            protected void onCanceled(final String reason) {
                assertEquals("onCanceled() blue must run on UI thread", true, PlatformUtils.getInstance().isUIThread());
                try {
                    set("blue");
                    fail("Can not set(\"blue\") in onCanceled()");
                } catch (Exception e) {
                }
            }
        };

        try {
            task1.fork();
            Thread.sleep(200);
            assertEquals("fork()ed task setStatus(Task.CANCELED) did not run onCanceled()", "blue", task1.get());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Can not testCancelThread()" + e);
        }
    }

    public void taskChainDelayTest() {
        final long t = System.currentTimeMillis();

        Task sleeper3sec = new Task() {
            protected Object exec(Object in) {
                try {
                    L.i("sleeper3", "start sleep");
                    Thread.sleep(3000);
                    L.i("sleeper3", "end sleep");
                } catch (Exception e) {
                    L.e("Problem with sleeper4", "", e);
                }

                return in;
            }
        };
        Task sleeper3chain = new Task() {
            protected Object exec(Object in) {
                L.i("getter3sec", "completed");
                return new Long(System.currentTimeMillis() - t);
            }
        };
        Task sleeper4sec = new Task() {
            protected Object exec(Object in) {
                try {
                    L.i("sleeper4", "start sleep");
                    Thread.sleep(4000);
                    L.i("sleeper4", "end sleep");
                } catch (Exception e) {
                    L.e("Problem with sleeper3", "", e);
                }

                return in;
            }
        };
        Task sleeper4chain = new Task() {
            protected Object exec(Object in) {
                L.i("getter10sec", "completed");
                in = new Long(System.currentTimeMillis() - t);

                return in;
            }
        };

        sleeper3sec.chain(sleeper3chain);
        sleeper4sec.chain(sleeper4chain);
        sleeper4sec.fork();
        sleeper3sec.fork();
        Task[] sleepers = {sleeper4sec, sleeper3sec};
        try {
            Task.joinAll(sleepers);
            assertTrue("joinAll() waiting >4sec", System.currentTimeMillis() - t >= 4000);
            Object long4 = sleeper4chain.get();
            Object long3 = sleeper3chain.get();
            assertTrue("sleeper4chain should be non-null", long4 != null);
            assertTrue("sleeper3chain should be non-null", long3 != null);
            assertEquals("sleeper4 should return Long", Long.class, long4.getClass());
            assertEquals("sleeper3 should return Long", Long.class, long3.getClass());
            final long runtime4 = ((Long) long4).longValue();
            final long runtime3 = ((Long) long3).longValue();
            assertTrue("4sec chain delay task " + runtime4 + " should be slower than 3sec " + runtime3 + " delay task", runtime3 < runtime4);
            assertTrue("4sec chain delay task " + runtime4 + " should be nearly 1 sec after 3sec " + runtime3 + " delay task", Math.abs(runtime4 - runtime3 - 1000) < 100);
        } catch (Exception e) {
            fail("Problem running taskChainDelayTest: " + e);
        }
    }
}
