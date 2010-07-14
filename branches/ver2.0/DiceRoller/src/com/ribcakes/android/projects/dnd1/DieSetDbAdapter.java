package com.ribcakes.android.projects.dnd1;

/**
 * 
 * @author Brian Stambaugh
 *
 * Copyright 2010 Brian Stambaugh
 * This application is distributed under the terms of the Artistic License 2.0.
 * 
 *  This file is part of Rib's Roller.
 *
 *   Rib's Roller is free software: you can redistribute it and/or 
 *   modify it under the terms of the Artistic License 2.0.
 *
 *   Rib's Roller is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 *
 *  
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * This class is used to interface with the SQLite database 
 * used to store user data.  Each die set has a title, and 
 * a string representing its constituent dice
 */
public class DieSetDbAdapter
{	
	//used to interface directly with the database
	private DatabaseHelper mDbHelper;
	
	//a reference to the database
	private SQLiteDatabase mDb;
	
	
	//the context of the activity accessing/creating the database 
	private final Context mContext;
	
	//id's of the columns in the database
	public static final String KEY_ROWID = "_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_TEXT_TO_PARSE = "textToParse";
	
	//constants used in the creation of the database
	private static final String DATABASE_NAME = "library";
	private static final String DATABASE_TABLE = "dieSets";
	private static final int DATABASE_VERSION = 2;
	
		/**
		 * 
		 * @author Brian Stambaugh
		 *	
		 * This class helps open, create, and upgrade the database file.
		 */
		private static class DatabaseHelper extends SQLiteOpenHelper 
		{
		
			/**
			 * Default Constructor
			 * @param context	the context of the activity creating/accessing the database
			 */
	        DatabaseHelper(Context context) 
	        {
	            super(context, DATABASE_NAME, null, DATABASE_VERSION);
	        }
	        
	        /**
	         * Creates a new table in the database if the specified table 
	         * doesn't already exist
	         * @param db The database to create the table in 
	         */
	        @Override
	        public void onCreate(SQLiteDatabase db)
	        {

	            db.execSQL("Create Table " + DATABASE_TABLE + " (" +
	            		KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
	            		KEY_TITLE + " TEXT NOT NULL, " +
	            		KEY_TEXT_TO_PARSE + " TEXT NOT NULL);");
	        }

	        /**
	         * Upgrades the database
	         * @param db	The database to be upgraded
	         * @param oldVersion the old version of the database
	         * @param newVersion the new version of the database
	         */
	        @Override
	        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	        {
	            Log.w("Rib's Roller", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
	            
	            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
	            
	            onCreate(db);
	        }
	    }

		public DieSetDbAdapter(Context context)
	  	{
	  		this.mContext = context;
	  	}
	  
		/**
		 * Opens the database so that it can be read and edited
		 * by way of the calling adapter
		 * @return returns this for method chaining
		 * @throws SQLException
		 */
	    public DieSetDbAdapter open() throws SQLException 
	    {
	    	mDbHelper = new DatabaseHelper(mContext);	        
	        mDb = mDbHelper.getWritableDatabase();
	        	        
	        return this;
	    }
	    
	    /**
	     * Closes the database
	     */
	    public void close() 
	    {
	        mDbHelper.close();
	        mDb.close();
	    }
	    
	    /**
	     * Checks to see if the database is already open
	     * @return boolean representation of the state of the database
	     */
	    public boolean isOpened()
	    {
	    	if(mDb != null)
	    		return mDb.isOpen();
	    	else
	    		return false;
	    }

	    /**
	     * Adds the provided die set to the database
	     * @param d	the die set to be added to the database
	     * @return	the rowID of the row that the die set was placed in, -1 if the insert was unsuccessful
	     */
	    public long addDieSet(DieSet d)
	    {
	    	ContentValues values = new ContentValues();
	    	values.put(KEY_TITLE, d.getTitle());
	    	values.put(KEY_TEXT_TO_PARSE, d.toDataBaseString());
	    	
	    	return mDb.insert(DATABASE_TABLE, null, values);
	    }
	    
	    /**
	     * Deletes the die set at the provided rowID
	     * @param rowID 	the ID of the row to be deleted
	     * @return	the success of the operation
	     */
	    public boolean deleteDieSet(long rowID)
	    {
	    	return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowID, null) > 0;
	    }
	    
	    /**
	     * Deletes all of the rows in the database
	     * @return the success of the operation
	     */
	    public boolean deleteAllEntries()
	    {
	    	return mDb.delete(DATABASE_TABLE, null, null) > 0;
	    }
	    
	    /**
	     * Returns a cursor holding a reference to all the die sets
	     * in the database
	     * @return a cursor pointing to all the die sets in the database
	     */
	    public Cursor fetchAllDieSets()
	    {
	    	return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE, KEY_TEXT_TO_PARSE}, null, null, null, null, null);
	    }
	    
	    /**
	     * Returns a cursor holding the die set at the provided rowID
	     * @param rowID	the location of the die set to be returned
	     * @return	a cursor holding the die set at the provided rowID
	     * @throws SQLException
	     */
	    public Cursor fetchDieSet(long rowID) throws SQLException
	    {
	    	Cursor rtrn = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE, KEY_TEXT_TO_PARSE}, KEY_ROWID + "=" + rowID, null, null, null, null, null);
	    	
	    	//moves to the first item in the cursor if it exists
	    	if(rtrn != null)
	    		rtrn.moveToFirst();
	    	
	    	return rtrn;
	    }
	    
	    /**
	     * Updates the die set at the provided rowID to the provided die set
	     * @param rowID	the row of the die set to be updated
	     * @param d	the updated die set
	     * @return	the success of the operation
	     */
	    public boolean updateDieSet(long rowID, DieSet d)
	    {
	    	ContentValues values = new ContentValues();
	    	values.put(KEY_TITLE, d.getTitle());
	    	values.put(KEY_TEXT_TO_PARSE, d.toDataBaseString());
	    	
	    	return mDb.update(DATABASE_TABLE, values, KEY_ROWID + "=" + rowID, null) > 0;
	    }
	    
}
