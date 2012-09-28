package com.futurice.tantalum3.rms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import com.futurice.tantalum3.PlatformUtils;
import com.futurice.tantalum3.Task;
import com.futurice.tantalum3.Workable;
import com.futurice.tantalum3.Worker;
import com.futurice.tantalum3.log.L;

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
    
    private Task initTask = new Task() {
        @Override
        public Object doInBackground(final Object in) {
            db = getWritableDatabase();
            Worker.forkShutdownTask(new Workable() {
                @Override
                public Object exec(final Object in2) {
                    db.close();
                    db = null;
                    
                    return in2;
                }
            });
            
            return in;
        }
    };

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
        Worker.fork(initTask);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    @Override
    public synchronized byte[] getData(final String key) {
        try {
            final String[] fields = new String[]{COL_DATA};
            final Cursor cursor = db.query(TABLE_NAME, fields, COL_KEY + "=?",
                    new String[]{String.valueOf(key)}, null, null, null, null);

            if (cursor == null || cursor.getCount() == 0) {
                return null;
            } else {
                cursor.moveToFirst();

                return cursor.getBlob(0);
            }
        } catch (NullPointerException e) {
            L.e("db not initialized, join then try again", "getData", e);
            try {
                initTask.join(10000);
            } catch (Exception ex) {
                L.e("db not initialized, join then try again problem", "getData", e);
            }

            return getData(key);
        }
    }

    @Override
    public synchronized void putData(final String key, final byte[] data) throws PlatformUtils.FlashFullException {
        final ContentValues values = new ContentValues();

        values.put(COL_KEY, key);
        values.put(COL_DATA, data);

        try {
            db.insert(TABLE_NAME, null, values);
        } catch (NullPointerException e) {
            L.e("db not initialized, join then try again", "putData", e);
            try {
                initTask.join(10000);
            } catch (Exception ex) {
                L.e("db not initialized, join then try again problem", "putData", e);
            }
            putData(key, data);
        } catch (SQLiteFullException e) {
            L.e("Android database full, attempting cleanup of old...", key, e);
            throw new PlatformUtils.FlashFullException();
        }
    }

    @Override
    public synchronized void removeData(final String key) {
        final String where = COL_KEY + "==\"" + key + "\"";

        try {
            db.delete(TABLE_NAME, where, null);
        } catch (NullPointerException e) {
            L.e("db not initialized, join then try again", "removeData", e);
            try {
                initTask.join(10000);
            } catch (Exception ex) {
                L.e("db not initialized, join then try again problem", "removeData", e);
            }
            removeData(key);
        }
    }
}