/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package android.database.sqlite;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * This class is used at build time, but it is never used since the real Android
 * implementation appears before this stub in your class path.
 *
 * @author phou
 */
public class SQLiteDatabase {

    public void close() {
        // Stub method to aid cross-platform compilation
    }

    public void execSQL(String sql) {
        // Stub method to aid cross-platform compilation
    }

    public static class CursorFactory {
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return null;
    }

    public long insert(String table, String nullColumnHack, ContentValues values) {
        return 0;
    }
    
    public int delete(String table, String whereClause, String[] whereArgs) {
        return 0;
    }
}
