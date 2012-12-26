/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.storage;

import java.util.Vector;

/**
 * A Hashtable-style interface for persistent data.
 * 
 * Each implementation is platform-specific (J2ME, Android, ...)
 * 
 * @author phou
 */
public interface FlashCache {
    /**
     * Get the data object associated with the key from persistent memory
     * 
     * @param key
     * @return 
     */
    public byte[] getData(String key) throws FlashDatabaseException;

    /**
     * Store the data object to persistent memory
     * 
     * @param key
     * @param bytes
     * @throws FlashFullException 
     */
    public void putData(String key, byte[] bytes) throws FlashFullException, FlashDatabaseException;

    /**
     * Remove the data object from persistent memory
     * 
     * @param key 
     */
    public void removeData(String key) throws FlashDatabaseException;

    /**
     * Provide a list of all keys for objects stored in persistent memory
     * 
     * @return 
     */
    public Vector getKeys() throws FlashDatabaseException;
}
