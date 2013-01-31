package android.database;

public abstract class Cursor {

	public abstract int	 getCount();
	
	public abstract boolean	 moveToFirst();
	public abstract byte[]	 getBlob(int columnIndex);
	public abstract void	 close();
	public abstract boolean	 moveToNext();
}
