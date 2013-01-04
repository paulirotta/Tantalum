/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package android.database;

/**
 *
 * @author phou
 */
public interface Cursor {
    public int getCount();
    
    public boolean moveToFirst();
    
    public boolean moveToNext();

    public byte[] getBlob(int columnIndex);
    
    public void close();
}
