/*
 * RMSStoreTest.java
 * JMUnit based test
 *
 * Created on 07-Feb-2012, 16:32:37
 */
package com.futurice.tantalum2.rms;

import com.futurice.tantalum2.rms.RMSStore.RMSStoreCallbackListener;
import java.util.Vector;
import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * @author phou
 */
public class RMSStoreTest extends TestCase {
    
    public RMSStoreTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(7, "RMSStoreTest");
    }    
    
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testListRecordStores();
                break;
            case 1:
                testStoreRecord();
                break;
            case 2:
                testDeleteRecord();
                break;
            case 3:
                testDeleteRecordStore();
                break;
            case 4:
                testGetRecord();
                break;
            case 5:
                testShutdown();
                break;
            case 6:
                testGetRecords();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testListRecordStores method, of class RMSStore.
     */
    public void testListRecordStores() throws AssertionFailedException {
        System.out.println("listRecordStores");
        String[] expResult_1 = null;
        String[] result_1 = RMSStore.listRecordStores();
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testStoreRecord method, of class RMSStore.
     */
    public void testStoreRecord() throws AssertionFailedException {
        System.out.println("storeRecord");
        RMSStore instance = new RMSStore();
        String name_1 = "";
        int intId_1 = 0;
        byte[] record_1 = null;
        RMSStoreCallbackListener listener_1 = null;
        instance.storeRecord(name_1, intId_1, record_1, listener_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testDeleteRecord method, of class RMSStore.
     */
    public void testDeleteRecord() throws AssertionFailedException {
        System.out.println("deleteRecord");
        RMSStore instance = new RMSStore();
        String name_1 = "";
        int intId_1 = 0;
        instance.deleteRecord(name_1, intId_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testDeleteRecordStore method, of class RMSStore.
     */
    public void testDeleteRecordStore() throws AssertionFailedException {
        System.out.println("deleteRecordStore");
        RMSStore instance = new RMSStore();
        String name_1 = "";
        instance.deleteRecordStore(name_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGetRecord method, of class RMSStore.
     */
    public void testGetRecord() throws AssertionFailedException {
        System.out.println("getRecord");
        RMSStore instance = new RMSStore();
        String name_1 = "";
        int intId_1 = 0;
        byte[] expResult_1 = null;
        byte[] result_1 = instance.getRecord(name_1, intId_1);
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testShutdown method, of class RMSStore.
     */
    public void testShutdown() throws AssertionFailedException {
        System.out.println("shutdown");
        RMSStore instance = new RMSStore();
        instance.shutdown();
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGetRecords method, of class RMSStore.
     */
    public void testGetRecords() throws AssertionFailedException {
        System.out.println("getRecords");
        RMSStore instance = new RMSStore();
        String name_1 = "";
        Vector expResult_1 = null;
        Vector result_1 = instance.getRecords(name_1);
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }
}
