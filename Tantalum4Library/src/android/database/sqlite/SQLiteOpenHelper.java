/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package android.database.sqlite;

import android.content.Context;

/**
 *
 * @author phou
 */
public class SQLiteOpenHelper {

    public SQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
    }

    public SQLiteDatabase getWritableDatabase() throws SQLiteException {
        return null;
    }
}
