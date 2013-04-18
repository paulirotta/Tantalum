package android.database;

public abstract class Cursor {

    public abstract int getCount();

    public abstract boolean moveToFirst();

    public abstract byte[] getBlob(int columnIndex);

    public abstract double getDouble(int columnIndex);

    public abstract float getFloat(int columnIndex);

    public abstract int getInt(int columnIndex);

    public abstract long getLong(int columnIndex);

    public abstract int getPosition();

    public abstract short getShort(int columnIndex);

    public abstract String getString(int columnIndex);

    public abstract int getType(int columnIndex);

    public abstract void close();

    public abstract boolean moveToNext();
}
