/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package android.database.sqlite;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Stub class to aid in cross-platform build and obfuscation
 *
 * @author phou
 */
public class SQLiteDatabase {

    /**
     * 
     */
    public void close() {
        // Stub method to aid cross-platform compilation
    }

    /**
     * 
     * @param sql 
     */
    public void execSQL(String sql) {
        // Stub method to aid cross-platform compilation
    }

    /**
     * 
     */
    public static class CursorFactory {
    }

    /**
     * 
     * @param table
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param having
     * @param orderBy
     * @param limit
     * @return 
     */
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return null;
    }

    /**
     * 
     * @param table
     * @param nullColumnHack
     * @param values
     * @return 
     */
    public long insert(String table, String nullColumnHack, ContentValues values) {
        return 0;
    }

    /**
     * 
     * @param table
     * @param whereClause
     * @param whereArgs
     * @return 
     */
    public int delete(String table, String whereClause, String[] whereArgs) {
        return 0;
    }
}
