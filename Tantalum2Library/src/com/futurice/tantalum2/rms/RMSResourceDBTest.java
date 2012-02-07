/*
 * RMSResourceDBTest.java
 * JMUnit based test
 *
 * Created on 07-Feb-2012, 16:32:17
 */
package com.futurice.tantalum2.rms;

import java.util.Vector;
import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * @author phou
 */
public class RMSResourceDBTest extends TestCase {
    
    public RMSResourceDBTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(10, "RMSResourceDBTest");
    }    
    
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testClear();
                break;
            case 1:
                testGetInstance();
                break;
            case 2:
                testClearResources();
                break;
            case 3:
                testStoreResource();
                break;
            case 4:
                testListRecordStores();
                break;
            case 5:
                testGetResource();
                break;
            case 6:
                testShutdown();
                break;
            case 7:
                testGetNumRecords();
                break;
            case 8:
                testGetAllResources();
                break;
            case 9:
                testDeleteResource();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testClear method, of class RMSResourceDB.
     */
    public void testClear() throws AssertionFailedException {
        System.out.println("clear");
        RMSResourceDB instance = null;
        instance.clear();
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGetInstance method, of class RMSResourceDB.
     */
    public void testGetInstance() throws AssertionFailedException {
        System.out.println("getInstance");
        RMSResourceDB expResult_1 = null;
        RMSResourceDB result_1 = RMSResourceDB.getInstance();
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testClearResources method, of class RMSResourceDB.
     */
    public void testClearResources() throws AssertionFailedException {
        System.out.println("clearResources");
        RMSResourceDB instance = null;
        RMSResourceType type_1 = null;
        instance.clearResources(type_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testStoreResource method, of class RMSResourceDB.
     */
    public void testStoreResource() throws AssertionFailedException {
        System.out.println("storeResource");
        RMSResourceDB instance = null;
        RMSRecord resource_1 = null;
        RMSCallbackListener listener_1 = null;
        int expResult_1 = 0;
        int result_1 = instance.storeResource(resource_1, listener_1);
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testListRecordStores method, of class RMSResourceDB.
     */
    public void testListRecordStores() throws AssertionFailedException {
        System.out.println("listRecordStores");
        String[] expResult_1 = null;
        String[] result_1 = RMSResourceDB.listRecordStores();
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGetResource method, of class RMSResourceDB.
     */
    public void testGetResource() throws AssertionFailedException {
        System.out.println("getResource");
        RMSResourceDB instance = null;
        RMSResourceType type_1 = null;
        String id_1 = "";
        RMSRecord expResult_1 = null;
        RMSRecord result_1 = instance.getResource(type_1, id_1);
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testShutdown method, of class RMSResourceDB.
     */
    public void testShutdown() throws AssertionFailedException {
        System.out.println("shutdown");
        RMSResourceDB instance = null;
        instance.shutdown();
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGetNumRecords method, of class RMSResourceDB.
     */
    public void testGetNumRecords() throws AssertionFailedException {
        System.out.println("getNumRecords");
        RMSResourceDB instance = null;
        RMSResourceType type_1 = null;
        int expResult_1 = 0;
        int result_1 = instance.getNumRecords(type_1);
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGetAllResources method, of class RMSResourceDB.
     */
    public void testGetAllResources() throws AssertionFailedException {
        System.out.println("getAllResources");
        RMSResourceDB instance = null;
        RMSResourceType type_1 = null;
        Vector expResult_1 = null;
        Vector result_1 = instance.getAllResources(type_1);
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testDeleteResource method, of class RMSResourceDB.
     */
    public void testDeleteResource() throws AssertionFailedException {
        System.out.println("deleteResource");
        RMSResourceDB instance = null;
        RMSResourceType type_1 = null;
        String id_1 = "";
        instance.deleteResource(type_1, id_1);
        fail("The test case is a prototype.");
    }
}
