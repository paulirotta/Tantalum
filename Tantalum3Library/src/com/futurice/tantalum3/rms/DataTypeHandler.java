/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.rms;

/**
 * Each StaticCache has a handler which handles conversions from network and RMS
 * binary form into the form used in memory and the cache. For example, image
 * format decompression or XML parsing.
 * 
 * @author phou
 */
public interface DataTypeHandler {
    
    /**
     * Shift from binary to in-memory usable form. This may increase or decrease
     * the total heap memory load, but in both cases it allows the RAM version
     * to be rapidly returned without re-parsing the binary data.
     * 
     * Every StaticCache must be assigned a DataTypeHandler
     * 
     * @param bytes
     * @return 
     */
    public Object convertToUseForm(byte[] bytes);
}
