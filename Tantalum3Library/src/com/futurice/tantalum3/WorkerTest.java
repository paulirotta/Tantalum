/*
 * WorkerTest.java
 * JMUnit based test
 *
 * Created on 07-Feb-2012, 16:22:57
 */
package com.futurice.tantalum3;

import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * @author phou
 */
public class WorkerTest extends TestCase {

    public WorkerTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(3, "WorkerTest");
    }

    public void test(int testNumber) throws Throwable {
        PlatformUtils.setProgram(this);
        Worker.init(4);
        switch (testNumber) {
            case 0:
                testRun();
                break;
            case 1:
                testQueue();
                break;
            case 2:
                testQueueEDT();
                break;
            default:
                break;
        }
    }

    private interface WorkableResult extends Workable {
        Object getResult();
    }
    
    /**
     * Test of testRun method, of class Worker.
     */
    public void testRun() throws AssertionFailedException {
        System.out.println("run");
        final Object mutex = new Object();
        WorkableResult wr = new WorkableResult() {
            private Object o;
            
            public Object compute() {
                synchronized (mutex) {
                    o = "yes";
                    mutex.notifyAll();
                }
                
                return null;
            }

            public Object getResult() {
                return o;
            }
        };
        
        Worker.fork(wr);
        synchronized (mutex) {
            try {
                mutex.wait(1000);
            } catch (InterruptedException ex) {
            }
        }
        
        assertEquals("yes", (String) wr.getResult());
        assertEquals(4, Worker.getNumberOfWorkers());
    }

    /**
     * Test of testQueue method, of class Worker.
     */
    public void testQueue() throws AssertionFailedException {
        System.out.println("queue");
        Worker.fork(null);
        Worker.fork(new Workable() {

            public Object compute() {
                return null;
            }            
        });
        Worker.fork(new Workable() {

            public Object compute() {
                return this;
            }            
        });
    }

    /**
     * Test of testQueueEDT method, of class Worker.
     */
    public void testQueueEDT() throws AssertionFailedException {
        System.out.println("queueEDT");
        final Object mutex = new Object();
        Result dgr = new Closure() {            

            public void run() {
                set("done");
                synchronized(mutex) {
                    mutex.notifyAll();
                }
            }
        };
        
        PlatformUtils.runOnUiThread((Runnable) dgr);        
        try {
            synchronized(mutex) {
                mutex.wait(1000);
            }
            assertEquals("done", (String) dgr.get());
        } catch (Exception e) {
        }
    }

    /**
     * Test of testQueueEDT method, of class Worker.
     */
    public void testQueueEDTNoWait() throws AssertionFailedException {
        System.out.println("queueEDT");
        Result dgr = new Closure() {            

            public void run() {
                set("done");
            }
        };
        
        PlatformUtils.runOnUiThread((Runnable) dgr);        
        try {
            // Test should wait here until EDT executes
            assertEquals("done", (String) dgr.get());
        } catch (Exception e) {
        }
    }
}
