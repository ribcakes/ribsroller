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

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteFullException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * This is the main window that the application runs out of.
 * It is what the application returns to after changing screens.
 * It handles the display of all die sets and results from rolling them.
 *
 */
public class MainWindow extends Activity
{
	//constants that are used to launch a new activity or create a dialog box
	private static final int CREATE_A_DIE_SET = 0;
	private static final int EDIT_DIE_SET = 1;
	private static final int DELETE_DIE_SET = 2;
	private static final int RESET_ALL_TO_DEFAULT = 3;

	//this variable is used to keep track of which item the context menu was invoked on 
	//so that the application can edit or delete the correct dieSet
	private int itemGeneratedContext;
	
	//this variable is used when the user goes into the settings of the application.
	//it holds the user desired length of the log of rolls so that the adapter can be
	//resized appropriately
	private int maxRetained;
	
	
	//this is used to store the length of the log along with all other simple data.
	//it is an object built into the Android OS used for storing primitive data types
	private SharedPreferences preferences;
	
	
	//this is the view that holds all of the die sets
	private GridView dieLibrary;
	
	//this is the adapter that holds all of the die set objects so that they can be rolled
	private DieSetAdapter dieAdapter;

	
	//this is the view that holds all of the results for the rolls
	private ListView log;
	
	//this is the adapter that holds all of the roll result objects so that they can be displayed
	//and acted upon
	private LogAdapter<RollResult> logAdapter;
	
	
	//this is the database adapter used to store and retrieve the user created custom die sets
	private DieSetDbAdapter mDbHelper;
	
	
	//this is the view that holds the focused result; the pane at the top of the display that shows
	//the detailed results of a roll
	private FocusedResult focusedResultContainer;
	
	//this variable holds the result that is currently loaded into the focusedResultContainer
	private RollResult focusedResult;
	
	
	//this is used to get values from the sensors built into the phone
	private SensorManager mSensorManager;
	
	//this is used to hold the last force the phone reported
	private double mLastForce; 
	
	//this is used to hold the last net force that was calculated
	private double mLastNetForce;

	//this is the threshold value that the method looks for to determine
	//whether or not to trigger a roll
	private double mForceThreshold;
	
	private boolean mAccelerometerEnabled;
	
	private static final int[] mSensorDelays = 
		{SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_DELAY_GAME, 
			SensorManager.SENSOR_DELAY_UI, SensorManager.SENSOR_DELAY_NORMAL}; 
	
	private int mSensorDelay;
	
	//this is the listener that listens for a change in the phone's sensors
	private SensorEventListener mSensorListener = new SensorEventListener()
	{
			public void onAccuracyChanged(Sensor sensor, int accuracy) 
			{
				
			}

			//this is called when the value of a sensor is changed
			public void onSensorChanged(SensorEvent event) 
			{								
				//we are only concerned with the accelerometer
                if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && mAccelerometerEnabled)
                {
                	//this variable gets the new values from the sensor
               		float[] values = event.values;
                	

                   
                    //this block calculates the total force that the phone is experiencing 
                    //as a fraction of the force of gravity
                    double totalForce = 0.0f;
                    totalForce += Math.pow(values[SensorManager.DATA_X]/SensorManager.GRAVITY_EARTH, 2.0);
                    totalForce += Math.pow(values[SensorManager.DATA_Y]/SensorManager.GRAVITY_EARTH, 2.0);
                    totalForce += Math.pow(values[SensorManager.DATA_Z]/SensorManager.GRAVITY_EARTH, 2.0);
                    totalForce = Math.sqrt(totalForce);
                    
                    //this determines the net force that the phone has experienced 
                    double netForce = 0;
                    netForce = totalForce - mLastForce;

                    //this checks to see if the previous net force was greater than the threshold and that
                    //the current force is less than the threshold to indicate that the phone has been 
                    //shaken in some way
                    if((Math.abs(mLastNetForce) > mForceThreshold) && (Math.abs(netForce) < mForceThreshold))
                    {
                    	//rolls the die that is currently being focused
                		makeResultFromFocused();
                    }

                    //updates the variables for the next event
                    mLastNetForce = netForce;
                    mLastForce = totalForce;
                }
			}
	 
	  };

	  
	/**
	 * This method is called when the activity is first created to set the view for the
	 * activity as well as do any global variable initialization necessary for the application
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		//sets the view of the activity to that defined in main.xml
		setContentView(R.layout.main);

		
		//initializes the variable
		itemGeneratedContext = -1;

		
		//gets the default shared preferences associated with this application
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        
        
        //for new installations, clears all the old preferences that may be present
        //in the phone so that there are no Class Cast exceptions from old preferences
        //stored in different types than the ones used in this version of the application
        if(preferences.getBoolean("jK4&jN", true))
        {
        	//clears all the preferences
        	preferences.edit().clear().commit();
                	
        	//gets an editor for the preferences and adds a boolean 
        	preferences.edit().putBoolean("jK4&jN", false).commit();        	
        }
        
        
        //load in the user set value for the log length; if there is no value for some reason,
        //the variable is set to 12, the default
        maxRetained = Integer.parseInt(preferences.getString(getString(R.string.max_retained), 12+""));
		
        mAccelerometerEnabled = preferences.getBoolean(getString(R.string.accel), true);

        mForceThreshold = Double.parseDouble(preferences.getString(getString(R.string.accel_sensitivity), ".1"));
                
        mSensorDelay = Integer.parseInt(preferences.getString(getString(R.string.accel_rate), "3"));
        
        //instantiates the adapter that will be used to hold the die sets in the view pane 
		dieAdapter = new DieSetAdapter();
		
		//finds the gridView that will be holding the dice by using its id and sets it to a 
		//global variable
		dieLibrary = (GridView)findViewById(R.id.die_library);		
		
		//sets the adapter of the gridView to the dieAdapter we instantiated earlier
		dieLibrary.setAdapter(dieAdapter);
		
		//sets a click listener for the items in the gridView
		dieLibrary.setOnItemClickListener(
				new OnItemClickListener() 
				{
					//called when one of the die set buttons is clicked
					public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
					{
						//makes a new result using the die set that was clicked
						makeResult((DieSet) dieAdapter.getItem(position));
					}
					
				});

		//this is an automated method belonging to the activity class that automatically makes
		//it so that every item in the view generates a context menu when it is longClicked
		registerForContextMenu(dieLibrary);
		
		//finds the focusedResult (layout widget) that will hold our focused result (global variable)
		focusedResultContainer = (FocusedResult)findViewById(R.id.focused_result_container);

		
		//checks to see if there is any data that was saved from a previous instance of the application
		//this data only exists when the phone's configuration is changed and the application re-launched
		Bundle retained = (Bundle)getLastNonConfigurationInstance();
		
		//if the data exists, aka there was a previous state
		if(retained != null)
		{
			//it looks for the old focused result
			if(retained.getParcelable("focusedResult") != null)
			{
				//if it finds something it inflates it from a parcel
				focusedResult = retained.getParcelable("focusedResult");
				
				//and sets the view to hold it 
				focusedResultContainer.setFocused(focusedResult);
			}
			else
			{
				//otherwise it is set as null
				focusedResult = null;
			}
			
			//it then looks to see if there was an old log
			if(retained.getParcelableArrayList("log") != null)
			{
				//if it finds one, it puts it in a local variable
				ArrayList<RollResult> temp = retained.getParcelableArrayList("log");		
				
				//and then uses it to instantiate the logAdapter
				logAdapter = new LogAdapter<RollResult>(this, temp, retained.getInt("maximum"));
			}
			else
			{
				//otherwise it just creates an empty log
				logAdapter = new LogAdapter<RollResult>(this);
			}
		}
		//if there is no previous state 
		else
		{
			//and empty log is created
			logAdapter = new LogAdapter<RollResult>(this);
			
			//and the focused result is set to null
			focusedResult = null;
		}
		
		
		//gets the listView that will be our log using its id
		log = (ListView)findViewById(R.id.log);		
		
		//sets the logAdapter we instantiated earlier to be its adapter
		log.setAdapter(logAdapter);	
		
		//sets a click listener for the items in the listView
		log.setOnItemClickListener(
				new OnItemClickListener()
				{
					//called when one of the items in the log is clicked
					public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
					{
						//changes the focusedResult view to the data stored in that result
						focusedResultContainer.setFocused(logAdapter.getItem(position));
						
						//sets the focused result as the item that was clicked
						focusedResult = logAdapter.getItem(position);
					}
					
				});

		
		//initializes the force variables
		mLastForce = 0.0f;
		mLastNetForce = 0.0f;
		
		
		//gets the system service associated with the sensors and store that in our global variable
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		
		//attaches the listener that we declared earlier to this manager, with the sensor that we will be listening to,
		//and the speed at which that sensor will update the application with its values 
	    mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), mSensorDelays[mSensorDelay]);

	    //finds the clear button with its id and sets a longClickListener to be associated with it
	    ((Button)findViewById(R.id.clear_button)).setOnLongClickListener(
	    		new View.OnLongClickListener() 
	    		{
					//called when the clear button is longClicked
					public boolean onLongClick(View v) 
					{
						//shows the die set reset dialog
						showDialog(RESET_ALL_TO_DEFAULT);
						
						//returns true to indicate that the long click was handled and that the 
						//onClick method for this view does not need to be run
						return true;
					}
				});
		
	    //see createDatabase
		createDatabase();
	}
	
	
	/**
	 * Takes a dieSet as a parameter and rolls the dice in the set to create a new result.
	 * The new result is then added to the log, and loaded into the focusedResult view
	 * @param item the die set to roll
	 */
	private void makeResult(DieSet item) 
	{
		//if the dieSet passed to the method wasn't null
		if(item != null)
		{
			//create a new rollResult with that dieSet
			RollResult result = new RollResult(item);
			
			//add the result to the log
			logAdapter.insert(result, 0);
			
			//refresh the log
			log.setAdapter(logAdapter);
					
			//set the new result as the focused result
			focusedResultContainer.setFocused(result);
			
			//store it in the global variable
			focusedResult = result;
		}
	}

	/**
	 * Generates a new result using the dieSet that was rolled to produce the current 
	 * focused result if a result is currently focused
	 */
	private void makeResultFromFocused()
	{
		//checks to make sure that a result is focused before trying to access a variable 
		//that may be null
		if(focusedResult != null)
			makeResult(focusedResult.getRolled());
	}
	
	
	/**
	 * This method creates a new database adapter that will be used to interface with the
	 * database.  
	 */
	private void createDatabase()
	{	
		//instantiates the adapter
		mDbHelper = new DieSetDbAdapter(this);
		try
		{
			//tries to open the database associated with the adapter, if one doesn't exist
			//a new one is created
			mDbHelper.open();
			
			//fetches all the die sets from the database and puts them into the dieAdapter
			fetchDieSetsFromDatabase();
		}
		
		//if there is a problem
		catch (SQLException e)
		{
			//notify the user
			Toast.makeText(this, "Database could not be opened/created properly.  Please clear application data to reset Database.", Toast.LENGTH_LONG);

			//and print the error in the log
			Log.e("Rib's Roller", "Database could not be opened/created properly."+e);
		}
	}

	/**
	 * Fetches all the die sets from the database. If it is found that there are no die sets
	 * in the database, the default sets are loaded into the adapter and added to the database.
	 */
	private void fetchDieSetsFromDatabase()
	{
		//fetches all the rows in the database and puts them into a cursor
		Cursor dieCursor = mDbHelper.fetchAllDieSets();

		//moves the cursor to the first row in the table
		dieCursor.moveToFirst();
		
		//creates a helper dieSet that will be used to add items to the dieAdapter
		DieSet helper = null;
		
		//checks to see if there are any die sets in the database;
		//if there are no die sets in the database, the count of the cursor is 0
		if(dieCursor.getCount() > 0)
		{
			//loops through the cursor to import all the die sets
			do
			{
				//instantiates the helper die set with the rowID, title, and die set found in the current row
				helper = new DieSet(dieCursor.getLong(dieCursor.getColumnIndex(DieSetDbAdapter.KEY_ROWID)), dieCursor.getString(dieCursor.getColumnIndex(DieSetDbAdapter.KEY_TITLE)), dieCursor.getString(dieCursor.getColumnIndex(DieSetDbAdapter.KEY_TEXT_TO_PARSE)));
				
				//the die set is then added to the adapter
				dieAdapter.add(helper);

			//checks to make sure that the cursor can move to the next row, if it can, it does so and returns true
			//if not, the loop stops
			}while(dieCursor.moveToNext());

			//closes the cursor, releasing all of its resources
			dieCursor.close();
		}
		//if there are no die sets in the database
		else
		{
			//close the cursor
			dieCursor.close();
			
			//and fill the database with the default sets
			fillDatabaseWithDefaultSets();			
		}
		
		//after all die sets have been added, invalidate the gridView forcing a redraw of its contents
		dieLibrary.invalidateViews();
	}
	
	/**
	 * Takes a die set parameter adds it to the database, and updates its rowID
	 * @param d the die set to add to the database
	 */
	private void addDieSetToDataBase(DieSet d)
	{
		try
		{
			d.setDatabaseRowID(mDbHelper.addDieSet(d));
		}
		catch(SQLiteFullException e)
		{
			Toast.makeText(this, "WARNING: Die Set could not be added." +
					"  There is no available space for the databse to expand.", Toast.LENGTH_LONG);
		}
	}

	/**
	 * Removes an item from the database defined by the rowID of the die set to generate the 
	 * context menu
	 */
	private void removeDieSetFromDataBase()
	{
		mDbHelper.deleteDieSet(dieAdapter.getItem(itemGeneratedContext).getDatabaseRowID());
	}

	/**
	 * Takes a die set as a parameter and using its rowID updates its database reference to 
	 * reflect its new values
	 * @param d the die set to be updated
	 */
	private void updateDataBaseEntry(DieSet d)
	{
		mDbHelper.updateDieSet(d.getDatabaseRowID(), d);
	}

	/**
	 * Adds the default set of dice to the database. The default set is defined as the following:
	 *  1d4, 1d6, 1d8, 1d10, 1d12, 1d20, and 1d100
	 */
	private void fillDatabaseWithDefaultSets()
	{
		
		addDieSetToDataBase(new DieSet(new Die(1, 4)));	
		addDieSetToDataBase(new DieSet(new Die(1, 6)));		
		addDieSetToDataBase(new DieSet(new Die(1, 8)));		
		addDieSetToDataBase(new DieSet(new Die(1, 10)));		
		addDieSetToDataBase(new DieSet(new Die(1, 12)));		
		addDieSetToDataBase(new DieSet(new Die(1, 20)));		
		addDieSetToDataBase(new DieSet(new Die(1, 100)));
		
		fetchDieSetsFromDatabase();
	}
	
	/**
	 * Removes all the entries in the database 
	 */
	private void removeAllSetsFromDatabase()
	{
		mDbHelper.deleteAllEntries();
	}

	
	/**
	 * This method is called when a view directly associated with this method in xml is clicked 
	 * @param view the view that was clicked
	 */
	public void clickListener(View view) 
    {
		//determines what to do based on the id of the calling view
    	switch(view.getId())
    	{
    		//if the clear button is clicked
    		case R.id.clear_button:

    			//remove all the items from the log
    			logAdapter.clear();
    			
    			//and clear the focusedResult so that only the title is still there
    			focusedResultContainer.clearFocused();
    			break;
    			
			//if the quit button is clicked
    		case R.id.quit_button:
    			
    			//close the database
    			mDbHelper.close();
    			
    			//finish the activity
    			finish();
    			
    			//and exit the virtual machine
    			System.exit(0);
    			break;
    			
			//if the focusedResult is clicked
    		case R.id.focused_result_background:
    			
    			//generate a new result from it
    			makeResultFromFocused();
    			break;
    	}
    }


	/**
	 * Called when showDialog(int) is called to create the dialog based on the int passed there
	 * @param id the id of the dialog to be created
	 * @return the constructed dialog
	 */
	@Override
	protected Dialog onCreateDialog(int id)
	{
		//instantiates the builder that will be used to construct the dialogs
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		//instantiates the inflater used to inflate any custom layouts
		LayoutInflater inflator = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
		//determine which dialog to create 
		switch(id)
		{
			//if the dialog is to confirm that the user wants to reset all die sets to default
			case RESET_ALL_TO_DEFAULT:
				
				//inflates the custom layout so we can use it in the dialog we are building
				View confirmDialog = inflator.inflate(R.layout.confirm_dialog, null);
				
				//creates the dialog to return
				AlertDialog confirm = builder
				
				//sets the dialog's title
				.setTitle("Reset Die Sets to Default")
				
				//sets the dialog's middle layout view
				.setView(confirmDialog)
				
				//sets the dialog's positive button text and listener
				.setPositiveButton("Ok", 
						new DialogInterface.OnClickListener() 
						{
							//called when then positive button is clicked
							public void onClick(DialogInterface dialog, int which) 
							{
								//dismisses the dialog; removes it from the user's view
								dialog.dismiss();
								
								
								//clear all the items from the dieAdapter
								dieAdapter.clear();
								
								//force the gridView to redraw
								dieLibrary.invalidateViews();
				    			
								
								//clear all the items from the log
								logAdapter.clear();
								
								
								//clear the focusedResult of all information including title
								focusedResultContainer.completelyClearFocused();
								
								//sets the focused result to null
								focusedResult = null;
								
								
								//removes all the die sets from the database
								removeAllSetsFromDatabase();
								
								//fills the database with the default die sets
								fillDatabaseWithDefaultSets();
								
								
								//forces the gridView to redraw
								dieLibrary.invalidateViews();
							}
						})
			
				//sets the negative button's text and listener		
				.setNegativeButton("Cancel", 
						new DialogInterface.OnClickListener()
						{
							//called when the negative button is clicked
							public void onClick(DialogInterface dialog, int which) 
							{
								//removes the dialog from the user's view
								dialog.dismiss();
							}
						})

				//take the defined attributes and make them into a Dialog object
				.create();
				
				return confirm;
				
				//if the id doesn't match any of the cases
			default:
				return super.onCreateDialog(id);
		}
	}
	
	/**
	 * Called when the hardware menu button is clicked to construct the options menu
	 * @param menu The options menu in which you place your items.
	 * @return whether or not the menu should be displayed.
	 */
	public boolean onCreateOptionsMenu(Menu menu) 
    {               
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu, menu);
            return true;
    }

	/**
	 * Called when an item in the options menu is clicked
	 * @param item the item that was selected
	 * @return whether or not to close the menu after the action has been completed
	 */
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	//declare an intent object that can be used in every case
    	Intent i = null;
    	
            switch (item.getItemId()) 
            {
            	//if the preferences item was clicked
            	case R.id.preferences:               
            		
            		//instantiate the intent with the current context and the activity to be run
                    i = new Intent(this, Preferences.class);
                    
                    //launch the intent
                    startActivity(i);
                    break;
        
                 //if the create a die set item was clicked
            	case R.id.create_a_set:

            		//instantiate the intent with the current context and the activity to be run
            		i = new Intent(this, CreateSet.class);
                    
            		//launch the intent and wait for a response from the activity when it closes
            		startActivityForResult(i, CREATE_A_DIE_SET);
            		break;
            	
        		//if the reset all die sets item was clicked
            	case R.id.reset:
            		
            		//show the confirmation dialog
            		showDialog(RESET_ALL_TO_DEFAULT);
                    break;
            }                                                       
            return true;
    }

    /**
     * Called when an item registered for a context menu is longClicked
     * @param menu 	The context menu that is being built
     * @param v 	The view for which the context menu is being built
     * @param menuInfo 	Extra information about the item for which the context menu should be shown. This information will vary depending on the class of v. 
     */
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
 	{
 		super.onCreateContextMenu(menu, v, menuInfo);
 		
 		switch(v.getId())
 		{
 			case R.id.die_library:
 		 		
 		 		//takes the position of the item in the gridView that created the context menu and stores it
 		 		//in a global variable for later use
 		 		itemGeneratedContext = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
 		 		
 		 		//adds items to the context menu
 				menu.add(Menu.NONE, EDIT_DIE_SET, Menu.NONE, "Edit Die Set");
 				menu.add(Menu.NONE, DELETE_DIE_SET, Menu.NONE, "Delete Die Set");
 				break;
 		}
	}

 	/**
 	 * Called when an item from a context is selected
 	 * @param item 	The context menu item that was selected
 	 */
 	public boolean onContextItemSelected(MenuItem item)
	{
    	//declare an intent object that can be used in every case
    	Intent i = null;
    	
    	switch(item.getItemId())
		{
			//if the edit die set item was selected
			case EDIT_DIE_SET:

        		//instantiate the intent with the current context and the activity to be run
				i = new Intent(this, CreateSet.class);
				
				//put extras into the intent so the CreateSet activity can correctly display the
				//value of the die set to be modified
				i.putExtra("modifier", dieAdapter.getItem(itemGeneratedContext).getModifier());
				i.putExtra("title", dieAdapter.getItem(itemGeneratedContext).getTitle());
				i.putParcelableArrayListExtra("currentDieSet", dieAdapter.getItem(itemGeneratedContext).getDice());
				
        		//launch the intent and wait for a response from the activity when it closes
				startActivityForResult(i, EDIT_DIE_SET);				
				break;
				
				//if the delete die set item was selected
			case DELETE_DIE_SET:
				
				//remove the reference to the die set from the database
				removeDieSetFromDataBase();
				
				
				//if the focusedResult isn't null, and the item being removed is the item that generated the currently
				//focused result, completely clear the focused result
				if(focusedResult != null && dieAdapter.getItem(itemGeneratedContext).equals(focusedResult.getRolled()))
				{
					//completely clears the focusedResult
					focusedResultContainer.completelyClearFocused();
					
					//sets the focused result to null
					focusedResult = null;
				}
				
				
				//remove the item from the gridView
				dieAdapter.remove(itemGeneratedContext);
				
				//force a redraw of the gridView
				dieLibrary.invalidateViews();
				break;
				
			default:
				return super.onContextItemSelected(item);
		}
		return false;
	}
	
	/**
	 * Called when an activity started with startActivityForResult ends
	 * @param requestCode 	The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
	 * @param resultCode 	The integer result code returned by the child activity through its setResult().
	 * @param data 	An Intent, which can return result data to the caller.
	 */
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		//if the database isn't open, OPEN IT
		//because this method is called before onResume(), the database must be opened here
		//or risk actions trying to edit/access it while it's closed
        if(mDbHelper != null && !mDbHelper.isOpened())
        	mDbHelper.open();
		
		switch(requestCode)
		{
			//if the activity returning was started with the create a die set code
			case CREATE_A_DIE_SET:
				switch(resultCode)
				{
					//and the activity ended successfully
					case RESULT_OK:

						//creates a temporary variable to hold the data coming back from
						//the activity
						DieSet temp = (DieSet)data.getParcelableExtra("dieSetParcel");
						
						
						//if the activity actually returned a die set
						if(temp != null)
						{
							//add it to the die adapter
							dieAdapter.add(temp);		
							
							//force a redraw on the gridView
							dieLibrary.invalidateViews();
							
							//and add it to the database
							addDieSetToDataBase(temp);
						}
						break;
				}
				break;
			
			//if the activity returning was started with the edit a die set code
			case EDIT_DIE_SET:
				switch (resultCode) 
				{
					//and the activity ended successfully
					case RESULT_OK:
						
						//take the data coming back and store it in a temporary variable
						DieSet temp = (DieSet)data.getParcelableExtra("dieSetParcel");
						
						//make sure that there is actually data there
						if(temp != null)
						{
							//take the rowID of the old item and put it into the new item
					    	temp.setDatabaseRowID(dieAdapter.getItem(itemGeneratedContext).getDatabaseRowID());
					
					    	
					    	//updates the item in the die adapter
							dieAdapter.setItem(itemGeneratedContext, temp);							
							
							//force a redraw on the gridView
							dieLibrary.invalidateViews();
							
							//update the entry in the database
							updateDataBaseEntry(temp);
						}
						break;

				}
		}
		
	}
	
 	/**
 	 * Called when the activity goes into the background
 	 */
    @Override
    protected void onPause()
    {
		super.onPause();
    	
    	//if the database exists, and is open, close it
        if(mDbHelper != null && mDbHelper.isOpened())
        	mDbHelper.close();
        
        //unregisters the sensor listener so it does not receive 
        //information while the application is in the background
        mSensorManager.unregisterListener(mSensorListener);     
    }
    
	/**
	 * Called after onPause() in order to resume the activity
	 */
 	protected void onResume() 
	{
		super.onResume();

		//re-acquire the value of maxRetained from the shared preferences in case it was changed in the
 		//preferences activity
        maxRetained = Integer.parseInt(preferences.getString(getString(R.string.max_retained), 12+""));
        mAccelerometerEnabled = preferences.getBoolean(getString(R.string.accel), true);
        mForceThreshold = Double.parseDouble(preferences.getString(getString(R.string.accel_sensitivity), ".1"));
        mSensorDelay = Integer.parseInt(preferences.getString(getString(R.string.accel_rate), "3"));
        
        //resize the log if required
        logAdapter.resize();
        
        //update the logAdapter
        log.setAdapter(logAdapter);
 
    
        //re-register the listener on the phone's sensors
		mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), mSensorDelays[mSensorDelay]);
       
		        
        //if the database adapter exists, and isn't already open, open it
        if(mDbHelper != null && !mDbHelper.isOpened())
        	mDbHelper.open();       
	}	
    
 	/**
 	 * Called when the activity is no longer visible to the user
 	 */
	protected void onStop()
	{
		super.onStop();

		//get an instance of a preference editor for our preferences
		SharedPreferences.Editor editor = preferences.edit();
		
		//place our log length variable into the preferences, overriding if it already exists
		editor.putString(getString(R.string.max_retained), maxRetained+"");
		editor.putBoolean(getString(R.string.accel), mAccelerometerEnabled);
		editor.putString(getString(R.string.accel_sensitivity), mForceThreshold+"");
		editor.putString(getString(R.string.accel_rate), mSensorDelay+"");

		//commit our changes
		editor.commit();
		
		//if the database adapter exists and is open, close it
        if(mDbHelper != null && mDbHelper.isOpened())
        	mDbHelper.close();
		
        //unregisters the sensor listener so it does not receive 
        //information while the application is in the background
        mSensorManager.unregisterListener(mSensorListener);
	}
	
	/**
	 * Called when the activity is being destroyed as part of a configuration change
	 * @return an object representing the activity's previous state 	
	 */
	@Override
	public Bundle onRetainNonConfigurationInstance()
	{
		//declare our object to be returned
		Bundle retain = new Bundle();
		
		//place our focused result into it, regardless if it exists or not
		retain.putParcelable("focusedResult", focusedResult);
		
		//place the log into the bundle
		retain.putParcelableArrayList("log", logAdapter.getContent());
		
		//put the log's maximum into the bundle
		retain.putInt("max", logAdapter.getMaximum());
		
		return retain;
	}


	
	/**
	 * 
	 * @author Brian Stambaugh
	 *
	 * Class used to render the die sets properly in the gridView
	 */
	public class DieSetAdapter extends BaseAdapter 
	{
		//variable to keep track of the die sets
	    private ArrayList<DieSet> dice;
	    
	    /**
	     * Public Constructor - Default Constructor
	     */
	    public DieSetAdapter()
	    {
	        dice = new ArrayList<DieSet>();
	    }

	    /**
	     * Public Constructor
	     * @param dice	The content for the adapter to start with
	     */
		@SuppressWarnings("unchecked")
		public DieSetAdapter(ArrayList<DieSet> dice)
	    {
	        this.dice = (ArrayList<DieSet>) dice.clone();
		}

		/**
		 * Adds the provided die set to the adapter
		 * @param d the die set to be added to the adapter
		 */
	    public void add(DieSet d)
	    {
	    	dice.add(d);
	    }
		
	    /**
	     * Removes the die set at the provided position
	     * @param position the position of the die set to be removed
	     */
	    public void remove(int position) 
	    {
	    	dice.remove(position);
		}
	    
	    /**
	     * Replaces the die set at the provided position with the
	     * provided object
	     * @param position	the position to be replaced
	     * @param d		the object to be inserted in that position
	     */
	    public void setItem(int position, DieSet d)
	    {
	    	dice.set(position, d);
	    }
	    
		 /**
		  * Removes all the die sets from the adapter
		  */
		public void clear()
		{
			dice.clear();
		}
	    
	    /**
	     * Returns the arrayList keeping all the die sets
	     * @return the arrayList keeping all the die sets
	     */
		public ArrayList<DieSet> getDice()
	    {
	    	return this.dice;
	    }


		/**
		 * Returns the number of items in this adapter
		 * used by system to draw contents
		 */
		public int getCount() 
		{
	        return dice.size();
	    }

		/**
		 * Returns the item at the provided position
		 */
	    public DieSet getItem(int position) 
	    {
	        return dice.get(position);
	    }
	    
	    /**
	     * not implemented ??
	     */
	    public long getItemId(int position) 
	    {
	        return position;
	    }

	    /**
	     * Called to create a view for the given position, from a reused view, with a view group parent
	     * @param position	the position of the item in the array to create a view for
	     * @param convertView	view that was previously created to be reused for another position
	     */
	    public View getView(int position, View convertView, ViewGroup parent)
	    {
	    	//declare the view
	        View dieView;
	        
	        //if we don't have a recycled view	    
	        if (convertView == null) 
	        {
	        	//get a layout inflater
	            LayoutInflater inflator = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            
	            //and inflate an xml to be our view
	            dieView = inflator.inflate(R.layout.die_item, null);
             
	        }

	        //if it's recycled
	        else 
	        {
	        	//set the view to be returned as the recycled view
	            dieView = convertView;
	        }

	        //set the text of the view to be the die set at position position
	        ((TextView)dieView.findViewById(R.id.die_item_text)).setText(dice.get(position).toString());	        
	        return dieView;
	    }
	}

	/**
	 * 
	 * @author Brian Stambaugh
	 * Class used to render the log items properly
	 */
	@SuppressWarnings("hiding")
	public class LogAdapter<RollResult> extends ArrayAdapter<RollResult> 
	{	
		//int to keep track of the size of the log
		private int maximum;

		/**
		 * Public Constructor
		 * @param c the current context
		 */
		public LogAdapter(Context c) 
		{
			super(c, R.layout.list_item);
			
			this.maximum = maxRetained;
		}
		
		/**
		 * Public Constructor
		 * @param c the current context
		 * @param content the list of rollResults to add to the adapter
		 * @param maximum the maximum size of the arrayList
		 */
		public LogAdapter(Context c, ArrayList<RollResult> content, int maximum)
		{
			super(c, R.layout.list_item);
	
			addAll(content);

			this.maximum = maximum;
		}
		
		/**
		 * Inserts the given object at the given index.
		 * Overridden so that log length can be maintained
		 * @param object	the object to insert
		 * @param index		where to insert the object	
		 */
		@Override
		public void insert(RollResult object, int index) 
		{
			//if the size of the array is equal the maximum allowed length
			//and that length isn't 0 (infinite length), remove the last item 
			//in the list
			if(this.getCount() == this.maximum+1 && this.maximum != 0)
				remove(getItem(this.getCount()-1));
			
			super.insert(object, index);
		}

		/**
		 * Adds all the objects in the provided list to the adapter
		 * @param r the list of objects to be added
		 */
		public void addAll(ArrayList<RollResult> r)
		{
			for(int i = r.size()-1; i >= 0; i--)
			{
				insert(r.get(i), 0);
			}
		}
		
		/**
		 * Trims the adapter if the new maximum length is smaller than the old
		 */
		public void resize()
		{
			//if the new is larger
			if(maxRetained > this.maximum)
			{
				//just update the variable and do nothing
				this.maximum = maxRetained;
			}
			
			//if it's smaller
			else
			{
				//remove all items above that index
				for(int i = this.getCount(); i > maxRetained; i--)
				{
					remove(this.getItem(maxRetained));
				}

				//and update the variable
				this.maximum = maxRetained;
			}
		}
	
		/**
		 * Returns the maximum length of the log
		 * @return the maximum length of the log
		 */
		public int getMaximum()
		{
			return this.maximum;
		}
		
		/**
		 * Returns an ArrayList containing all of the items in the adapter
		 * @return all of the items in the adapter
		 */
		public ArrayList<RollResult> getContent()
		{
			//create a helper object
			ArrayList<RollResult> temp = new ArrayList<RollResult>();
			
			//add every item in the adapter to it
			for(int i = 0; i < this.getCount(); i++)
			{
				temp.add(this.getItem(i));
			}
			
			//return it
			return temp;
		}

	    /**
	     * Called to create a view for the given position, from a reused view, with a view group parent
	     * @param position	the position of the item in the array to create a view for
	     * @param convertView	view that was previously created to be reused for another position
	     */
		public View getView(int position, View convertView, ViewGroup parent) 
		{			
	    	//declare the view
	        View v;
	        
	        //if we don't have a recycled view	    
	        if (convertView == null) 
	        {
	        	//get a layout inflater
	            LayoutInflater inflator = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	            //and inflate an xml to be our view
	            v = inflator.inflate(R.layout.list_item, null);
	        }

	        //if it's recycled
	        else
	        {
	        	//set the view to be returned as the recycled view
	        	v = convertView;
	        }
	        
	        //set the text of the view to be the result at position position   
	        ((TextView)v.findViewById(R.id.list_item_title)).setText(((com.ribcakes.android.projects.dnd1.RollResult) getItem(position)).getRolled().toString());
        	((TextView)v.findViewById(R.id.list_item_result)).setText(((com.ribcakes.android.projects.dnd1.RollResult) getItem(position)).getResult()+"");
        	
        	return v;
		}
	}
	
}