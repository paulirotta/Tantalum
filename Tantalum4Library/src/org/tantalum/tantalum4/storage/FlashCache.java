/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.tantalum4.storage;

/**
 *
 * @author phou
 */
public interface FlashCache {
    public byte[] getData(String key);
    
    public void putData(String key, byte[] bytes) throws FlashFullException;
    
    public void removeData(String key);
}
