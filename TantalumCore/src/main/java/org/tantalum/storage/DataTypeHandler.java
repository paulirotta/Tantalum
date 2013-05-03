/*
 Copyright (c) 2013, Paul Houghton and Futurice Oy
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
package org.tantalum.storage;

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
     * @param key - an parameter which may optionally help with some implementations
     * of <code>DataTypeHandler</code> and aids in debug messages if something
     * goes wrong at runtime.
     * @param bytes - the bytes received from the network or stored in a
     * <code>StaticCache</code> for persistence.
     * @return 
     */
    public Object convertToUseForm(Object key, byte[] bytes);
}
