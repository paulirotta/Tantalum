package org.tantalum.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.IOException;
import java.util.Vector;
import org.tantalum.Workable;
import org.tantalum.Worker;
import org.tantalum.storage.FlashCache;
import org.tantalum.storage.FlashDatabaseException;
import org.tantalum.storage.FlashFullException;
import org.tantalum.util.L;

public final class AndroidCache extends SQLiteOpenHelper implements FlashCache {

    protected static final int DB_VERSION = 1;
    protected static final String DB_NAME = "TantalumRMS";
    protected static final String TABLE_NAME = "TantalumRMS_Table";
    protected static final String COL_ID = "id";
    protected static final String COL_KEY = "key";
    protected static final String COL_DATA = "data";
    protected static final String CREATE_DB = "CREATE TABLE IF NOT EXISTS "
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

    public AndroidCache() {
        super(context, DB_NAME, null, DB_VERSION);
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

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DB);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public synchronized byte[] getData(final String key) throws FlashDatabaseException {
        Cursor cursor = null;

        final String[] fields = new String[]{COL_DATA};
        L.i("db getData", "1");
        try {
            if (db == null) {
                db = getWritableDatabase();
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
            L.e("db not initialized, join then try again", "getData", e);
            return null;
        } catch (SQLiteException e) {
            L.e("db can not be initialized", "getData, key=" + key, e);
            throw new FlashDatabaseException("db init error on getData, key=" + key + " : " + e);
        } finally {
            L.i("db getData", "end");
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public synchronized void putData(final String key, final byte[] data) throws FlashFullException, FlashDatabaseException {
        final ContentValues values = new ContentValues();

        values.put(COL_KEY, key);
        values.put(COL_DATA, data);

        try {
            if (db == null) {
                db = getWritableDatabase();
            }
            db.insert(TABLE_NAME, null, values);
        } catch (SQLiteFullException e) {
            L.e("Android database full, attempting cleanup of old...", key, e);
            throw new FlashFullException("key = " + key + " : " + e);
        } catch (SQLiteException e) {
            L.e("Android database error when putting data", key, e);
            throw new FlashDatabaseException("key = " + key + " : " + e);
        }
    }

    public synchronized void removeData(final String key) throws FlashDatabaseException {
        final String where = COL_KEY + "==\"" + key + "\"";

        try {
            if (db == null) {
                db = getWritableDatabase();
            }
            db.delete(TABLE_NAME, where, null);
        } catch (SQLiteException e) {
            L.e("Can not access database on removeData()", key, e);
            throw new FlashDatabaseException("Can not remove data from database: " + e);
        }
    }

    public Vector getKeys() throws FlashDatabaseException {
        final Vector keys = new Vector();
        Cursor cursor = null;

        try {
            if (db == null) {
                db = getWritableDatabase();
            }
            cursor = db.query(TABLE_NAME, new String[]{COL_KEY}, "*",
                    null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    keys.addElement(new String(cursor.getBlob(0)));
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException e) {
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