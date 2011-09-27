/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2.rms;

/**
 * Each StaticCache has a handler which handles conversions from network and RMS
 * binary form into the form used in memory and the cache. For example, image
 * format decompression or XML parsing.
 * 
 * @author phou
 */
public interface DataTypeHandler {
    public Object convertToUseForm(byte[] bytes);
}
