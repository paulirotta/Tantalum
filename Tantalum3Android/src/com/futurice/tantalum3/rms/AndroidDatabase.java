package com.futurice.tantalum3.rms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class AndroidDatabase extends SQLiteOpenHelper {

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

    /**
     * Your app must call this to set the context before the database is
     * initialized in Tantalum
     * 
     * @param c 
     */
    public static void setContext(Context c) {
        context = c;
    }

    public AndroidDatabase() {
        super(context, DB_NAME, null, DB_VERSION);
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

    public synchronized byte[] getData(String key) {
        // System.out.println(key);
        final String[] fields = new String[]{COL_DATA};
        final SQLiteDatabase db = this.getReadableDatabase();
        final Cursor cursor = db.query(TABLE_NAME, fields, COL_KEY + "=?",
                new String[]{String.valueOf(key)}, null, null, null, null);

        if (cursor == null || cursor.getCount() == 0) {
            db.close();
            return null;
        } else {
            cursor.moveToFirst();
//			System.out.println(cursor.getColumnNames());
//			System.out.println(cursor.getColumnCount());
            byte[] data = cursor.getBlob(0);
            db.close();

            return data;
        }
    }

    public synchronized void putData(String key, byte[] data) {
        final SQLiteDatabase db = this.getWritableDatabase();
        final ContentValues values = new ContentValues();
        
        values.put(COL_KEY, key);
        values.put(COL_DATA, data);

        db.insert(TABLE_NAME, null, values);
        db.close();
        //TODO Opening and closing the database might be safe, but slow. Check options.
    }

    public synchronized void removeData(String key) {
        final SQLiteDatabase db = this.getWritableDatabase();

        String where = COL_KEY + "==\"" + key + "\"";

        db.delete(TABLE_NAME, where, null);
        db.close();
    }
}