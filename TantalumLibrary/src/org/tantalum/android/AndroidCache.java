/*
 Copyright © 2012 Paul Houghton and Futurice on behalf of the Tantalum Project.
 All rights reserved.

 Tantalum software shall be used to make the world a better place for everyone.

 This software is licensed for use under the Apache 2 open source software license,
 http://www.apache.org/licenses/LICENSE-2.0.html

 You are kindly requested to return your improvements to this library to the
 open source community at http://projects.developer.nokia.com/Tantalum

 The above copyright and license notice notice shall be included in all copies
 or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package org.tantalum.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.Vector;
import org.tantalum.Workable;
import org.tantalum.Worker;
import org.tantalum.storage.FlashCache;
import org.tantalum.storage.FlashDatabaseException;
import org.tantalum.storage.FlashFullException;
import org.tantalum.util.L;

/**
 * Android implementation of cross-platform persistent storage using an SQLite
 * database. All web services you access over the net using a StaticWebCache
 * will be automatically stored here for faster access and offline use.
 *
 * @author phou
 */
public final class AndroidCache implements FlashCache {

    private final SQLiteOpenHelper helper;
    /**
     * Database version number
     */
    private static final int DB_VERSION = 2;
    /**
     * Database name
     */
    private static final String DB_NAME = "Tantalum";
    /**
     * Database table name
     */
    private static final String TABLE_NAME = "Tantalum_Table";
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
     * SQL to create the database
     */
    private static final String CREATE_DB = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + "(" + COL_ID + " INTEGER PRIMARY KEY, " + COL_KEY
            + " TEXT NOT NULL, " + COL_DATA + " BLOB NOT NULL)";
    private static Context context;
    private volatile SQLiteDatabase db = null;

    /**
     * Your app must call this to set the context before the database is
     * initialized in Tantalum
     *
     * @param c
     */
    public static void setContext(final Context c) {
        context = c;
    }

    /**
     * Create the persistent memory database singleton
     *
     */
    public AndroidCache() {
        helper = new SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION);
        Worker.forkShutdownTask(new Workable() {
            public Object exec(final Object in2) {
                if (db != null) {
                    db.close();
                }
                db = null;

                return in2;
            }
        });
    }

    /**
     * Execute SQL to create the database
     *
     * @param db
     */
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DB);
    }

    /**
     * Execute SQL to update the database
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
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
    public Vector getKeys() throws FlashDatabaseException {
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
}