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

    private interface WorkableResult extends Task {
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
            
            public boolean compute() {
                synchronized (mutex) {
                    o = "yes";
                    mutex.notifyAll();
                }
                
                return false;
            }

            public Object getResult() {
                return o;
            }
        };
        
        Worker.queue(wr);
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
        Worker.queue(null);
        Worker.queue(new Task() {

            public boolean compute() {
                return false;
            }            
        });
        Worker.queue(new Task() {

            public boolean compute() {
                return true;
            }            
        });
    }

    /**
     * Test of testQueueEDT method, of class Worker.
     */
    public void testQueueEDT() throws AssertionFailedException {
        System.out.println("queueEDT");
        final Object mutex = new Object();
        Result dgr = new RunnableResult() {            

            public void run() {
                setResult("done");
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
        } catch (Exception e) {
        }
        assertEquals("done", (String) dgr.getResult());
    }
}
