/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package android.database.sqlite;

import android.content.ContentValues;
import android.database.Cursor;

/**
 *
 * @author phou
 */
public final class SQLiteDatabase {

    public class CursorFactory {
    }
    
    public void execSQL(String sql) {
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return null;
    }
    
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return null;
    }
    
    public long insert(String table, String nullColumnHack, ContentValues values) {
        return 0;
    }
    
    public int delete(String table, String whereClause, String[] whereArgs) {
        return 0;
    }
    
    public void close() {
    }
}
