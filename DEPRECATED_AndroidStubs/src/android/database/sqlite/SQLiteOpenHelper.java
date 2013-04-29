/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package android.database.sqlite;

import android.content.Context;

/**
 * Empty stub class to aid in cross-platform build and obfuscation
 * 
 * To prevent this compilation stub from interfering with the actual Android
 * class, make sure Tantalum is at the end of your project's class path
 *
 * @author phou
 */
public class SQLiteOpenHelper {

    /**
     * 
     * @param context
     * @param name
     * @param factory
     * @param version 
     */
    public SQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
    }

    /**
     * 
     * @return
     * @throws SQLiteException 
     */
    public SQLiteDatabase getWritableDatabase() throws SQLiteException {
        return null;
    }
}
