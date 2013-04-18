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

import java.io.UnsupportedEncodingException;
import java.security.DigestException;
import java.util.Hashtable;
import java.util.Vector;
import org.tantalum.Task;
import org.tantalum.util.CryptoUtils;
import org.tantalum.util.L;
import org.tantalum.util.StringUtils;

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
    protected final Vector shutdownTasks = new Vector();

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
     * Add a
     * <code>Task</code> which will be run before the cache closes.
     *
     * This is normally useful to save in-memory data during shutdown.
     *
     * The minimum run priority for a cache shutdown
     * <code>Task</code> is
     * <code>Task.NORMAL_PRIORITY</code>
     *
     * Note that there is a limited amount of time between when the phone tells
     * the application to close, and when it must close. This varies by phone,
     * but about 3 seconds is typical. Thus like
     * <code>Task.SHUTDOWN_PRIORITY</code>, a cache shutdown
     * <code>Task</code> should not take long to complete or it may block other
     * Tasks from completing.
     *
     * @param shutdownTask
     */
    public final void addShutdownTask(final Task shutdownTask) {
        if (shutdownTask.getForkPriority() < Task.NORMAL_PRIORITY) {
            throw new IllegalArgumentException("FlashCache shutdown Tasks must have a minimum priority of Task.NORMAL_PRIORITY");
        }
        shutdownTasks.addElement(shutdownTask);
    }

    public abstract String getKey(final long digest) throws FlashDatabaseException;

    /**
     * Calculate a shorter, byte[] key for use in the hashtable. This reduces
     * total memory consumption since the String keys can be quite long.
     *
     * A cryptographic has function such as MD5 is usually used to implement
     * this. A trivial implementation would be to convert the String into a byte
     * array.
     *
     * @param key
     * @return
     * @throws UnsupportedEncodingException
     * @throws DigestException
     * @throws FlashDatabaseException
     */
    public final byte[] get(final String key) throws DigestException, FlashDatabaseException {
        if (key == null) {
            throw new IllegalArgumentException("You attempted to get a null digest from the cache");
        }

        final long digest;
        try {
            digest = CryptoUtils.getInstance().toDigest(key);
        } catch (UnsupportedEncodingException ex) {
            //#debug
            L.e(this, "get() can not decode", "key = " + key, ex);
            throw new FlashDatabaseException("get() can not decode key: " + key + " - " + ex);
        }

        //#debug
        L.i(this, "get(" + key + ")", "digest=" + StringUtils.toHex(CryptoUtils.getInstance().longToBytes(digest)));

        return get(digest);
    }

    /**
     * Get the object from flash memory
     *
     * @param digest
     * @return
     * @throws FlashDatabaseException
     */
    public abstract byte[] get(long digest) throws DigestException, FlashDatabaseException;

    /**
     * Store the data object to persistent memory
     *
     * @param key
     * @param bytes
     * @throws DigestException
     * @throws UnsupportedEncodingException
     * @throws FlashFullException
     * @throws FlashDatabaseException
     */
    public abstract void put(String key, byte[] bytes) throws DigestException, FlashFullException, FlashDatabaseException;

    /**
     * Remove the data object from persistent memory
     *
     * @param key
     * @throws DigestException
     * @throws UnsupportedEncodingException
     * @throws FlashDatabaseException
     */
    public final void removeData(final String key) throws DigestException, FlashDatabaseException {
        if (key == null) {
            throw new IllegalArgumentException("You attempted to remove a null string key from the cache");
        }
        try {
            removeData(CryptoUtils.getInstance().toDigest(key));
        } catch (UnsupportedEncodingException ex) {
            //#debug
            L.e(this, "removeData() can not decode", "key = " + key, ex);
            throw new FlashDatabaseException("removeData() can not decode key: " + key + " - " + ex);
        }
    }

    public abstract void removeData(long digest) throws FlashDatabaseException;

    /**
     * Return a list of all keys for objects stored in persistent memory
     *
     * @return
     * @throws DigestException
     * @throws UnsupportedEncodingException
     * @throws FlashDatabaseException
     */
    public abstract long[] getDigests() throws DigestException, FlashDatabaseException;

    /**
     * Remove all items from this flash cache
     *
     */
    public abstract void clear();
}
