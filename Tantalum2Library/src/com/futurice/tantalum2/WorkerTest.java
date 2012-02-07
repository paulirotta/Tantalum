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
        super(5,"WorkerTest");
    }            

    public void test(int testNumber) throws Throwable {
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
            case 4:
                testInit();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testRun method, of class Worker.
     */
    public void testRun() throws AssertionFailedException {
        System.out.println("run");
        Worker instance = null;
        instance.run();
        fail("The test case is a prototype.");
    }

    /**
     * Test of testQueue method, of class Worker.
     */
    public void testQueue() throws AssertionFailedException {
        System.out.println("queue");
        Workable workable_1 = null;
        Worker.queue(workable_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGetMIDlet method, of class Worker.
     */
    public void testGetMIDlet() throws AssertionFailedException {
        System.out.println("getMIDlet");
        MIDlet expResult_1 = null;
        MIDlet result_1 = Worker.getMIDlet();
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testQueueEDT method, of class Worker.
     */
    public void testQueueEDT() throws AssertionFailedException {
        System.out.println("queueEDT");
        Object runnable_1 = null;
        Worker.queueEDT(runnable_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testInit method, of class Worker.
     */
    public void testInit() throws AssertionFailedException {
        System.out.println("init");
        MIDlet midlet_1 = null;
        int numberOfWorkers_1 = 0;
        Worker.init(midlet_1, numberOfWorkers_1);
        fail("The test case is a prototype.");
    }
}
