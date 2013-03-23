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
package org.tantalum.android;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.Vector;
import org.tantalum.PlatformUtils;
import org.tantalum.Task;
import org.tantalum.storage.FlashCache;
import org.tantalum.storage.FlashDatabaseException;
import org.tantalum.storage.FlashFullException;
import org.tantalum.util.L;

/**
 * Android implementation of cross-platform persistent storage using an SQLite
 * database. All web services you access over the net using a StaticWebCache
 * will be automatically stored here for faster access and offline use.
 *
 * You should not access this class directly. Use
 * <code>PlatformUtils.getInstance()...</code> instead.
 *
 * @author phou
 */
public final class AndroidCache extends FlashCache {

    private SQLiteOpenHelper helper;
    /**
     * Database version number
     */
    private static final int DB_VERSION = 2;
    /**
     * Database name
     */
    private static final String databaseName = "Tantalum";
    /**
     * Database table name
     */
    private final String TABLE_NAME = "Tantalum_Table" + +priority;
    /**
     * Database id column tag
     */
    private static final String COL_ID = "id";
    /**
     * Database key column tag
     */
    private static final String COL_KEY = "key";
    /**
     * Database data column tag
     */
    private static final String COL_DATA = "data";
    /**
     * Android object used for associated the database with an application
     */
    private static Context context = null;
    /**
     * SQL to create the database
     */
    private final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + "(" + COL_ID + " INTEGER PRIMARY KEY, " + COL_KEY
            + " TEXT NOT NULL, " + COL_DATA + " BLOB NOT NULL)";
    private final String CLEAR_TABLE = "DROP TABLE " + TABLE_NAME;
    private SQLiteDatabase db = null;

    /**
     * Create a new AndroidCache. You should not call this method directly, but
     * rather request the cache from
     * <code>PlatformUtils.getInstance().getFlashCache(priority)</code>
     *
     * @param priority
     */
    public AndroidCache(final char priority) {
        super(priority);

        final Context c;
        synchronized (AndroidCache.class) {
            if (context == null) {
                context = ((Activity) PlatformUtils.getInstance().getProgram()).getApplicationContext();
            }
            c = context;
        }
        helper = new SQLiteOpenHelper(c, databaseName, null, DB_VERSION);
        (new Task() {
            public Object exec(final Object in2) {
                if (db != null) {
                    db.close();
                }
                db = null;

                return in2;
            }
        }).fork(Task.SHUTDOWN_PRIORITY);
    }

    /**
     * Execute SQL to create the database
     *
     * @param db
     */
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    /**
     * Execute SQL to update the database
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Get the stored byte[] associated with the specified key
     *
     * @param key
     * @return
     * @throws FlashDatabaseException
     */
    public synchronized byte[] getData(final String key) throws FlashDatabaseException {
        Cursor cursor = null;

        final String[] fields = new String[]{COL_DATA};
        L.i("db getData", "1");
        try {
            if (db == null) {
                db = helper.getWritableDatabase();
            }
            L.i("db getData", "2");
            cursor = db.query(TABLE_NAME, fields, COL_KEY + "=?",
                    new String[]{String.valueOf(key)}, null, null, null, null);
            L.i("db getData", "3");

            if (cursor == null || cursor.getCount() == 0) {
                return null;
            } else {
                cursor.moveToFirst();

                return cursor.getBlob(0);
            }
        } catch (NullPointerException e) {
            L.e("db not initialized, join() then try again", "getData", e);
            return null;
        } catch (Exception e) {
            L.e("db can not be initialized", "getData, key=" + key, e);
            throw new FlashDatabaseException("db init error on getData, key=" + key + " : " + e);
        } finally {
            L.i("db getData", "end");
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Store the specified byte[] to flash memory for later retrieval by key
     *
     * @param key
     * @param data
     * @throws FlashFullException
     * @throws FlashDatabaseException
     */
    public synchronized void putData(final String key, final byte[] data) throws FlashFullException, FlashDatabaseException {
        final ContentValues values = new ContentValues();

        values.put(COL_KEY, key);
        values.put(COL_DATA, data);

        try {
            if (db == null) {
                db = helper.getWritableDatabase();
            }
            db.insert(TABLE_NAME, null, values);
        } catch (Exception e) {
            try {
                if (Class.forName("android.database.sqlite.SQLiteFullException").isAssignableFrom(e.getClass())) {
                    throw new FlashFullException("Android database full, attempting cleanup of old..." + key + " : " + e);
                }
            } catch (ClassNotFoundException e2) {
                L.e("Introspection error", "android.database.sqlite.SQLiteFullException", e2);

            }
            L.e("Android database error when putting data", key, e);
            throw new FlashDatabaseException("key = " + key + " : " + e);
        }
    }

    /**
     * Remove the byte[] associated with this key from the database
     *
     * @param key
     * @throws FlashDatabaseException
     */
    public synchronized void removeData(final String key) throws FlashDatabaseException {
        final String where = COL_KEY + "==\"" + key + "\"";

        try {
            if (db == null) {
                db = helper.getWritableDatabase();
            }
            db.delete(TABLE_NAME, where, null);
        } catch (Exception e) {
            L.e("Can not access database on removeData()", key, e);
            throw new FlashDatabaseException("Can not remove data from database: " + e);
        }
    }

    /**
     * Get a list of all the keys for data available in this database
     *
     * @return
     * @throws FlashDatabaseException
     */
    public synchronized Vector getKeys() throws FlashDatabaseException {
        final Vector keys = new Vector();
        Cursor cursor = null;

        try {
            if (db == null) {
                db = helper.getWritableDatabase();
            }
            cursor = db.query(TABLE_NAME, new String[]{COL_KEY}, "*",
                    null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    keys.addElement(new String(cursor.getBlob(0)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            L.e("Can not access database on getKeys()", "", e);
            throw new FlashDatabaseException("Can not acccess database on getKeys() : " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return keys;
    }

    /**
     * Remove all elements from this cache. Since there is a different table for
     * each cache, other caches still contain values.
     *
     */
    public synchronized void clear() {
        db.execSQL(CLEAR_TABLE);
    }
}