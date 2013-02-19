package android.database.sqlite;

import android.content.ContentValues;
import android.database.Cursor;

public class SQLiteDatabase {

    public static interface CursorFactory {
    }

    public void close() {
    }

    public void execSQL(String sql) {
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return null;
    }

    public int delete(String table, String whereClause, String[] whereArgs) {

        return 0;
    }

    public long insert(String table, String nullColumnHack, ContentValues values) {
        return 0;
    }
}
