package com.ribcakes.android.projects.dnd1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DieSetDbAdapter
{

	private static final String TAG = "RibsRoller:DieSetDbAdapter:";
	
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	private final Context mContext;
	
	public static final String KEY_ROWID = "_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_TEXT_TO_PARSE = "textToParse";
	
	private static final String DATABASE_NAME = "library";
	private static final String DATABASE_TABLE = "dieSets";
	private static final int DATABASE_VERSION = 2;
	
	
	  private static class DatabaseHelper extends SQLiteOpenHelper 
	  {

	        DatabaseHelper(Context context) 
	        {
	            super(context, DATABASE_NAME, null, DATABASE_VERSION);
	        }

	        @Override
	        public void onCreate(SQLiteDatabase db)
	        {

	            db.execSQL("Create Table " + DATABASE_TABLE + " (" +
	            		KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
	            		KEY_TITLE + " TEXT NOT NULL, " +
	            		KEY_TEXT_TO_PARSE + " TEXT NOT NULL);");
	        }

	        @Override
	        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	        {
	            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
	            
	            db.execSQL("DROP TABLE IF EXISTS notes");
	            
	            onCreate(db);
	        }
	    }

	  	public DieSetDbAdapter(Context context)
	  	{
	  		this.mContext = context;
	  	}
	  
	    public DieSetDbAdapter open() throws SQLException 
	    {
	    	mDbHelper = new DatabaseHelper(mContext);	        
	        mDb = mDbHelper.getWritableDatabase();
	        	        
	        return this;
	    }
	    
	    public void close() 
	    {
	        mDbHelper.close();
	        mDb.close();
	    }
	    
	    public boolean isOpened()
	    {
	    	if(mDb != null)
	    		return mDb.isOpen();
	    	else
	    		return false;
	    }

	    public long addDieSet(DieSet d)
	    {
	    	ContentValues values = new ContentValues();
	    	values.put(KEY_TITLE, d.getTitle());
	    	values.put(KEY_TEXT_TO_PARSE, d.toDataBaseString());
	    	
	    	return mDb.insert(DATABASE_TABLE, null, values);
	    }
	    
	    public boolean deleteDieSet(long rowID)
	    {
	    	return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowID, null) > 0;
	    }
	    
	    public boolean deleteAllEntries()
	    {
	    	return mDb.delete(DATABASE_TABLE, null, null) > 0;
	    }
	    
	    public Cursor fetchAllDieSets()
	    {
	    	return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE, KEY_TEXT_TO_PARSE}, null, null, null, null, null);
	    }
	    
	    public Cursor fetchDieSet(long rowID) throws SQLException
	    {
	    	Cursor rtrn = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE, KEY_TEXT_TO_PARSE}, KEY_ROWID + "=" + rowID, null, null, null, null, null);
	    	
	    	if(rtrn != null)
	    		rtrn.moveToFirst();
	    	
	    	return rtrn;
	    }
	    
	    public boolean updateDieSet(long rowID, DieSet d)
	    {
	    	ContentValues values = new ContentValues();
	    	values.put(KEY_TITLE, d.getTitle());
	    	values.put(KEY_TEXT_TO_PARSE, d.toDataBaseString());
	    	
	    	return mDb.update(DATABASE_TABLE, values, KEY_ROWID + "=" + rowID, null) > 0;
	    }
	    
}
