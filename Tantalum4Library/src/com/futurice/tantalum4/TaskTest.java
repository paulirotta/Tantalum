/*
 * TaskTest.java
 * JMUnit based test
 *
 * Created on 23-Nov-2012, 09:46:55
 */
package com.futurice.tantalum4;

import java.util.Vector;
import jmunit.framework.cldc11.*;

/**
 * @author phou
 */
public class TaskTest extends TestCase {

    public TaskTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(15, "TaskTest");

        PlatformUtils.setProgram(this);
        Worker.init(2);
    }

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
            default:
                break;
        }
    }

    /**
     * Test of testFork method, of class Task.
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
     */
    public void testCancel() throws AssertionFailedException {
        final Vector errors = new Vector();
        System.out.println("cancel");
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
                    errors.addElement("Should have canceled doInBackground 4");
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
        for (int i = 0; i < errors.size(); i++) {
            fail((String) errors.elementAt(i));
        }
    }

    /**
     * Test of testOnCanceled method, of class Task.
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
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        assertEquals(Task.EXEC_FINISHED, instanceA.getStatus());        
        assertEquals(Task.UI_RUN_FINISHED, instanceB.getStatus());        
    }
}
