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
import java.util.Vector;
import org.tantalum.CancellationException;
import org.tantalum.Task;
import org.tantalum.TimeoutException;
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

    /**
     * An application-unique local identifier for the cache
     *
     * Uniqueness is not internally checked or enforced. StaticCache does this,
     * so be aware that you should cooperate with StaticCache if used for other
     * purposes.
     */
    public final char priority;
    /**
     *
     */
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

    /**
     *
     * @param digest
     * @return
     * @throws FlashDatabaseException
     */
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
        L.i(this, "get(" + key + ")", "digest=" + StringUtils.byteArrayToHexString(CryptoUtils.getInstance().longToBytes(digest)));

        return get(digest);
    }

    /**
     * Get the object from flash memory
     *
     * @param digest
     * @return
     * @throws DigestException
     * @throws FlashDatabaseException
     */
    public abstract byte[] get(long digest) throws DigestException, FlashDatabaseException;

    /**
     * Store the data object to persistent memory
     *
     * @param key
     * @param bytes
     * @throws DigestException
     * @throws FlashFullException
     * @throws FlashDatabaseException
     */
    public abstract void put(String key, byte[] bytes) throws DigestException, FlashFullException, FlashDatabaseException;

    /**
     * Remove the data object from persistent memory
     *
     * @param key
     * @throws DigestException
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

    /**
     *
     * @param digest
     * @throws FlashDatabaseException
     */
    public abstract void removeData(long digest) throws FlashDatabaseException;

    /**
     * Return a list of all keys for objects stored in persistent memory
     *
     * @return
     * @throws FlashDatabaseException
     */
    public abstract long[] getDigests() throws FlashDatabaseException;

    /**
     * Remove all items from this flash cache
     *
     */
    public abstract void clear();

    /**
     * Find out how many bytes of space are available on the cache's storage
     * medium (phone or memory card)
     *
     * @return
     * @throws FlashDatabaseException
     */
    public abstract long getFreespace() throws FlashDatabaseException;

    /**
     * Run finalization tasks and then close all cache resources
     *
     * This this FlashCache is part of a StaticCache, this will be called
     * automatically on shutdown. Otherwise you may need to call this yourself
     * by queuing a Task.SHUTDOWN_PRIORITY task.
     */
    public void close() throws FlashDatabaseException {
        final Task[] t;
        synchronized (shutdownTasks) {
            t = new Task[shutdownTasks.size()];
            for (int i = 0; i < t.length; i++) {
                t[i] = (Task) shutdownTasks.elementAt(i);
                t[i].fork();
            }
            shutdownTasks.removeAllElements();
        }
        try {
            Task.joinAll(t);
        } catch (CancellationException ex) {
            //#debug
            L.e(this, "Canceled", "Cache shutdown tasks not completed", ex);
        } catch (TimeoutException ex) {
            //#debug
            L.e(this, "Timeout", "Cache shutdown tasks not completed", ex);
        }
    }
    
    /**
     * An action you would like to perform on every cache entry at application start
     * 
     */
    public interface StartupTask {

        /**
         * An operation you would like to run for each key in the cache at start
         * time.
         *
         * This is run during startup initialization when the cache is walked to
         * check integrity. You can use it to perform your own cache validation
         * and expiration tasks at application start.
         *
         * @param key
         */
        public abstract void execForEachKey(FlashCache flashCache, String key) throws DigestException, FlashDatabaseException;
    }
}
