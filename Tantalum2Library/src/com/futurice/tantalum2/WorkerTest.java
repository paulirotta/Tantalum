/*
 * WorkerTest.java
 * JMUnit based test
 *
 * Created on 07-Feb-2012, 16:22:57
 */
package com.futurice.tantalum2;

import javax.microedition.midlet.MIDlet;
import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * @author phou
 */
public class WorkerTest extends TestCase {

    public WorkerTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(4, "WorkerTest");
    }

    public void test(int testNumber) throws Throwable {
        Worker.init(this, 2);
        switch (testNumber) {
            case 0:
                testRun();
                break;
            case 1:
                testQueue();
                break;
            case 2:
                testGetMIDlet();
                break;
            case 3:
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
            
            public boolean work() {
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
    }

    /**
     * Test of testQueue method, of class Worker.
     */
    public void testQueue() throws AssertionFailedException {
        System.out.println("queue");
        Worker.queue(null);
        Worker.queue(new Workable() {

            public boolean work() {
                return false;
            }            
        });
        Worker.queue(new Workable() {

            public boolean work() {
                return true;
            }            
        });
    }

    /**
     * Test of testGetMIDlet method, of class Worker.
     */
    public void testGetMIDlet() throws AssertionFailedException {
        System.out.println("getMIDlet");
        MIDlet expResult_1 = this;
        MIDlet result_1 = Worker.getMIDlet();
        assertEquals(expResult_1, result_1);
    }

    /**
     * Test of testQueueEDT method, of class Worker.
     */
    public void testQueueEDT() throws AssertionFailedException {
        System.out.println("queueEDT");
        final Object mutex = new Object();
        DefaultResult dgr = new DefaultRunnableResult() {            

            public void run() {
                setResult("done");
                synchronized(mutex) {
                    mutex.notifyAll();
                }
            }
        };
        
        Worker.queueEDT(dgr);        
        try {
            synchronized(mutex) {
                mutex.wait(1000);
            }
        } catch (Exception e) {
        }
        assertEquals("done", (String) dgr.getResult());
    }
}
