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

import java.util.Hashtable;
import java.util.Vector;

/**
 * A Hashtable-style interface for persistent data.
 *
 * Each implementation is platform-specific (JME, Android, ...)
 *
 * @author phou
 */
public abstract class FlashCache {
    private static final Hashtable priorities = new Hashtable();

    /**
     * A unique local identifier for the cache.
     */
    public final char priority;

    /**
     * The priority must be unique in the application.
     *
     * Caches with lower priority are garbage collected to save space before
     * caches with higher priority.
     *
     * @param priority
     */
    public FlashCache(final char priority) {
        final Character c = new Character(priority);
        if (priorities.contains(c)) {
            throw new IllegalArgumentException("Duplicate FlashCache priority '" + priority + "' has already been created by your application. Keep a reference to this object or use a different priority");
        }
        priorities.put(c, c);
        
        this.priority = priority;
    }
    
    /**
     * Get the data object associated with the key from persistent memory
     *
     * @param key
     * @return
     * @throws FlashDatabaseException
     */
    public abstract byte[] getData(String key) throws FlashDatabaseException;

    /**
     * Store the data object to persistent memory
     *
     * @param key
     * @param bytes
     * @throws FlashFullException
     * @throws FlashDatabaseException
     */
    public abstract void putData(String key, byte[] bytes) throws FlashFullException, FlashDatabaseException;

    /**
     * Remove the data object from persistent memory
     *
     * @param key
     * @throws FlashDatabaseException
     */
    public abstract void removeData(String key) throws FlashDatabaseException;

    /**
     * Provide a list of all keys for objects stored in persistent memory
     *
     * @return
     * @throws FlashDatabaseException
     */
    public abstract Vector getKeys() throws FlashDatabaseException;
    
    /**
     * Remove all items from this flash cache
     * 
     */
    public abstract void clear();
}
