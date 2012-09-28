/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.rms;

import com.futurice.tantalum3.PlatformUtils;

/**
 *
 * @author phou
 */
public interface FlashCache {
    public byte[] getData(String key);
    
    public void putData(String key, byte[] bytes) throws PlatformUtils.FlashFullException;
    
    public void removeData(String key);
}
