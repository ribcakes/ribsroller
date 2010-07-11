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


public class MainWindow extends Activity
{
	
	private static final int CREATE_A_DIE_SET = 0;
	private static final int EDIT_DIE_SET = 1;
	private static final int DELETE_DIE_SET = 2;
	private static final int RESET_ALL_TO_DEFAULT = 3;

	private int itemGeneratedContext;
	private int maxRetained;
	
	private SharedPreferences preferences;
	
	private GridView dieLibrary;
	private DieAdapter dieAdapter;

	private ListView log;
	private LogAdapter<RollResult> logAdapter;
	
	private DieSetDbAdapter mDbHelper;
	
	private FocusedResult focusedResultContainer;
	private RollResult focusedResult;
	
	private SensorManager mSensorManager;
	private double mLastForce; 
	private double mLastNetForce;
	
	private final SensorEventListener mSensorListener = new SensorEventListener()
	{
			public void onAccuracyChanged(Sensor sensor, int accuracy) 
			{
				
			}

			public void onSensorChanged(SensorEvent event) 
			{
                if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                {
               		float[] values = event.values;
                	
                        double forceThreshHold = .1f;
                       
                        double totalForce = 0.0f;
                        totalForce += Math.pow(values[SensorManager.DATA_X]/SensorManager.GRAVITY_EARTH, 2.0);
                        totalForce += Math.pow(values[SensorManager.DATA_Y]/SensorManager.GRAVITY_EARTH, 2.0);
                        totalForce += Math.pow(values[SensorManager.DATA_Z]/SensorManager.GRAVITY_EARTH, 2.0);
                        totalForce = Math.sqrt(totalForce);
                        
                        double netForce = 0;
                        netForce = totalForce - mLastForce;

                        
                        if((Math.abs(mLastNetForce) > forceThreshHold) && (Math.abs(netForce) < forceThreshHold))
                        {
                    		makeResultFromFocused();
                        }

                        mLastNetForce = netForce;
                        mLastForce = totalForce;
                }
			}
	 

	  };

	  
  @Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		itemGeneratedContext = -1;
		
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        
        if(preferences.getBoolean("oH78ui", true))
        {
        	preferences.edit().clear().commit();
                	
        	SharedPreferences.Editor editor = preferences.edit();
        	editor.putBoolean("oH78ui", false);
        	editor.commit();
        }
        
        maxRetained = Integer.parseInt(preferences.getString(getString(R.string.max_retained), 12+""));
		
		dieAdapter = new DieAdapter();
		dieLibrary = (GridView)findViewById(R.id.die_library);		
		dieLibrary.setAdapter(dieAdapter);
		dieLibrary.setOnItemClickListener(
				new OnItemClickListener() 
				{

					public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
					{
						makeResult((DieSet) dieAdapter.getItem(position));
					}
					
				});
		

		focusedResultContainer = (FocusedResult)findViewById(R.id.focused_result_container);

		
		Bundle retained = (Bundle)getLastNonConfigurationInstance();
		if(retained != null)
		{
			if(retained.getParcelable("focusedResult") != null)
			{
				focusedResult = retained.getParcelable("focusedResult");
				focusedResultContainer.setFocused(focusedResult);
			}
			else
			{
				focusedResult = null;
			}
			
			if(retained.getParcelableArrayList("log") != null)
			{
				ArrayList<RollResult> temp = retained.getParcelableArrayList("log");		
				logAdapter = new LogAdapter<RollResult>(this, temp, retained.getInt("maximum"));
			}
			else
			{
				logAdapter = new LogAdapter<RollResult>(this);
			}
		}
		else
		{
			logAdapter = new LogAdapter<RollResult>(this);
			focusedResult = null;
		}
		
		
		log = (ListView)findViewById(R.id.log);		
		log.setAdapter(logAdapter);	
		log.setOnItemClickListener(
				new OnItemClickListener()
				{
					
					public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
					{
						focusedResultContainer.setFocused(logAdapter.getItem(position));
						focusedResult = logAdapter.getItem(position);
					}
					
				});
				
		registerForContextMenu(dieLibrary);
		
		mLastForce = 0.0f;
		mLastNetForce = 0.0f;
		
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

	    ((Button)findViewById(R.id.clear_button)).setOnLongClickListener(
	    		new View.OnLongClickListener() 
	    		{
					
					public boolean onLongClick(View v) 
					{
						showDialog(RESET_ALL_TO_DEFAULT);
						return true;
					}
				});
		
		createDatabase();
	}
	
	private void makeResult(DieSet item) 
	{
		if(item != null)
		{
			RollResult result = new RollResult(item);
			logAdapter.add(result);
			log.setAdapter(logAdapter);
					
			focusedResultContainer.setFocused(result);
			
			focusedResult = result;
		}
	}

	private void makeResultFromFocused()
	{
		if(focusedResult != null)
			makeResult(focusedResult.getRolled());
	}
	
	
	private void createDatabase()
	{
				
		mDbHelper = new DieSetDbAdapter(this);
		try
		{
			mDbHelper.open();
			fetchDieSetsFromDatabase();
		}
		catch (SQLException e)
		{
			Toast.makeText(this, "Database could not be opened/created properly.  Please clear application data to reset Database.", Toast.LENGTH_LONG);
			Log.e("Rib's Roller", "Database could not be opened/created properly.");
		}
	}

	private void fetchDieSetsFromDatabase()
	{
		Cursor dieCursor = mDbHelper.fetchAllDieSets();

		dieCursor.moveToFirst();
		
		DieSet helper = null;
		
		//only tries to import dice if the cursor is on the first row; cursor will only be before the first row if there is no content to import
		if(dieCursor.getCount() > 0)
		{
			do
			{
				helper = new DieSet(dieCursor.getLong(dieCursor.getColumnIndex(DieSetDbAdapter.KEY_ROWID)), dieCursor.getString(dieCursor.getColumnIndex(DieSetDbAdapter.KEY_TITLE)), dieCursor.getString(dieCursor.getColumnIndex(DieSetDbAdapter.KEY_TEXT_TO_PARSE)));
				dieAdapter.add(helper);

				runOnUiThread(invalidateContent);					
			}while(dieCursor.moveToNext());
			dieCursor.close();
		}
		else
		{
			dieCursor.close();
			fillDatabaseWithDefaultSets();			
		}
		
		
		runOnUiThread(invalidateContent);
	}
	
	private void addDieSetToDataBase(DieSet d)
	{
		d.setDatabaseRowID(mDbHelper.addDieSet(d));
	}

	private void removeDieSetFromDataBase()
	{
		mDbHelper.deleteDieSet(dieAdapter.getItem(itemGeneratedContext).getDatabaseRowID());
	}

	private void updateDataBaseEntry(DieSet d)
	{
		mDbHelper.updateDieSet(d.getDatabaseRowID(), d);
	}

	private void fillDatabaseWithDefaultSets()
	{
		dieAdapter.add(new DieSet(new Die(1, 4)));
		addDieSetToDataBase(new DieSet(new Die(1, 4)));
		
		dieAdapter.add(new DieSet(new Die(1, 6)));
		addDieSetToDataBase(new DieSet(new Die(1, 6)));
		
		dieAdapter.add(new DieSet(new Die(1, 8)));
		addDieSetToDataBase(new DieSet(new Die(1, 8)));
		
		dieAdapter.add(new DieSet(new Die(1, 10)));
		addDieSetToDataBase(new DieSet(new Die(1, 10)));
		
		dieAdapter.add(new DieSet(new Die(1, 12)));
		addDieSetToDataBase(new DieSet(new Die(1, 12)));
		
		dieAdapter.add(new DieSet(new Die(1, 20)));
		addDieSetToDataBase(new DieSet(new Die(1, 20)));
		
		dieAdapter.add(new DieSet(new Die(1, 100)));
		addDieSetToDataBase(new DieSet(new Die(1, 100)));
	}
	
	private void removeAllSetsFromDatabase()
	{
		mDbHelper.deleteAllEntries();
	}
	
	private Runnable invalidateContent = new Runnable() 
	{
		
		public void run() 
		{
			dieLibrary.invalidateViews();
		}
	};


	public void clickListener(View view) //Buttons
    {
    	switch(view.getId())
    	{
    		case R.id.clear_button:
    			logAdapter.removeAll();
    			focusedResultContainer.clearFocused();
    			break;
    			
    		case R.id.quit_button:
    			mDbHelper.close();
    			finish();
    			System.exit(0);
    			break;
    			
    		case R.id.focused_result_background:
    			makeResultFromFocused();
    			break;
    	}
    }


	@Override
	protected Dialog onCreateDialog(int id)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		LayoutInflater inflator = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View confirmDialog = inflator.inflate(R.layout.confirm_dialog, null);
		
		AlertDialog confirm = builder
		.setTitle("Reset Die Sets to Default")
		.setView(confirmDialog)
		.setPositiveButton("Ok", 
				new DialogInterface.OnClickListener() 
				{
					
					public void onClick(DialogInterface dialog, int which) 
					{
						dialog.dismiss();
						
						dieAdapter.clear();
						dieLibrary.invalidateViews();
		    			
						logAdapter.removeAll();
						focusedResultContainer.completelyClearFocused();
						focusedResult = null;
						
						removeAllSetsFromDatabase();
						fillDatabaseWithDefaultSets();
						
						dieLibrary.invalidateViews();
					}
				})
		.setNegativeButton("Cancel", 
				new DialogInterface.OnClickListener()
				{
					
					public void onClick(DialogInterface dialog, int which) 
					{
						dialog.dismiss();
					}
				})
		.create();
		
		return confirm;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
    {               
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu, menu);
            return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	Intent i = null;
            switch (item.getItemId()) 
            {
            
            	case R.id.preferences:                   
                    i = new Intent(this, Preferences.class);
                    startActivity(i);
                    break;
            	case R.id.create_a_set:
                    i = new Intent(this, CreateSet.class);
                    startActivityForResult(i, CREATE_A_DIE_SET);
            		break;
            	case R.id.reset:
            		showDialog(RESET_ALL_TO_DEFAULT);
                    break;
            }                                                       
            return true;
    }

 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
 	{
 		super.onCreateContextMenu(menu, v, menuInfo);
 		
 		itemGeneratedContext = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
 		
 		switch(v.getId())
 		{
 			case R.id.die_library:
 				menu.add(Menu.NONE, EDIT_DIE_SET, Menu.NONE, "Edit Die Set");
 				menu.add(Menu.NONE, DELETE_DIE_SET, Menu.NONE, "Delete Die Set");
 				break;
 		}
	}
 	
	public boolean onContextItemSelected(MenuItem item)
	{
		Intent i = null;
		switch(item.getItemId())
		{
			case EDIT_DIE_SET:
				i = new Intent(this, CreateSet.class);
				i.putExtra("modifier", dieAdapter.getItem(itemGeneratedContext).getModifier());
				i.putParcelableArrayListExtra("currentDieSet", dieAdapter.getItem(itemGeneratedContext).getDice());
				
				startActivityForResult(i, EDIT_DIE_SET);				
				break;
				
			case DELETE_DIE_SET:
				removeDieSetFromDataBase();
				
				if(dieAdapter.getItem(itemGeneratedContext).equals(focusedResult.getRolled()))
					focusedResultContainer.completelyClearFocused();
				
				dieAdapter.remove(itemGeneratedContext);
				dieLibrary.invalidateViews();
				break;
				
			default:
				return super.onContextItemSelected(item);
		}
		return false;
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
        if(mDbHelper != null && !mDbHelper.isOpened())
        	mDbHelper.open();
		
		switch(requestCode)
		{
			case CREATE_A_DIE_SET:
				switch(resultCode)
				{
					case RESULT_OK:
					DieSet temp = (DieSet)data.getParcelableExtra("dieSetParcel");
					
					if(temp != null)
					{
						dieAdapter.add(temp);					
						dieLibrary.invalidateViews();
						addDieSetToDataBase(temp);
					}
					break;
				}
				break;
				
			case EDIT_DIE_SET:
				switch (resultCode) 
				{
					case RESULT_OK:
						DieSet temp = (DieSet)data.getParcelableExtra("dieSetParcel");
						
						if(temp != null)
						{
					    	temp.setDatabaseRowID(dieAdapter.getItem(itemGeneratedContext).getDatabaseRowID());
					
							dieAdapter.setItem(itemGeneratedContext, temp);							
							dieLibrary.invalidateViews();
							updateDataBaseEntry(temp);
						}
						break;

				}
		}
		
	}
	
	protected void onResume() 
	{
        maxRetained = Integer.parseInt(preferences.getString(getString(R.string.max_retained), 12+""));
        
        logAdapter.resize();
        log.setAdapter(logAdapter);
        
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
 
        if(mDbHelper != null && !mDbHelper.isOpened())
        	mDbHelper.open();
        
		super.onResume();
	}	
	
    @Override
    protected void onPause()
    {
    	super.onPause();
           
        if(mDbHelper != null && mDbHelper.isOpened())
        	mDbHelper.close();
        
        mSensorManager.unregisterListener(mSensorListener);
    }
	
	protected void onStop()
	{
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(getString(R.string.max_retained), maxRetained+"");
		editor.commit();
		
        if(mDbHelper != null && mDbHelper.isOpened())
        	mDbHelper.close();
		
		super.onStop();
	}

	@Override
	public Bundle onRetainNonConfigurationInstance()
	{
		Bundle retain = new Bundle();
		
		retain.putParcelable("focusedResult", focusedResult);
		retain.putParcelableArrayList("log", logAdapter.getContent());
		retain.putInt("max", logAdapter.getMaximum());
		
		return retain;
	}


	public class DieAdapter extends BaseAdapter 
	{
	    private ArrayList<DieSet> dice;
	    
	    public DieAdapter()
	    {
	        dice = new ArrayList<DieSet>();
	    }

		public void clear()
		{
			dice.clear();
		}

		@SuppressWarnings("unchecked")
		public DieAdapter(ArrayList<DieSet> dice)
	    {
	        this.dice = (ArrayList<DieSet>) dice.clone();
		}

		public ArrayList<DieSet> getDice()
	    {
	    	return this.dice;
	    }

	    public void remove(int position) 
	    {
	    	dice.remove(position);
		}
	    
	    public void add(DieSet d)
	    {
	    	dice.add(d);
	    }
	    
	    public void add(int position, DieSet d)
	    {
	    	dice.add(position, d);
	    }
	    
	    public void add(Die d)
	    {
	    	ArrayList<Die> tempDice = new ArrayList<Die>();
	    	tempDice.add(d);
	    	
	    	DieSet temp = new DieSet("", tempDice, 0);
	    	dice.add(temp);
	    }

		public int getCount() 
		{
	        return dice.size();
	    }

	    public DieSet getItem(int position) 
	    {
	        return dice.get(position);
	    }
	    
	    public void setItem(int position, DieSet d)
	    {
	    	
	    	remove(position);
	    	add(position, d);
	    }

	    public ArrayList<DieSet> getContent()
	    {
	    	return this.dice;
	    }
	    
	    public long getItemId(int position) 
	    {
	        return position;
	    }

	    // create a new ImageView for each item referenced by the Adapter
	    public View getView(final int position, View convertView, ViewGroup parent)
	    {
	        View dieView;
	        if (convertView == null) 
	        {  // if it's not recycled, initialize some attributes

	            LayoutInflater inflator = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            dieView = inflator.inflate(R.layout.die_item, null);
             
	        }
	        else 
	        {
	            dieView = convertView;
	        }

	        ((TextView)dieView.findViewById(R.id.die_item_text)).setText(dice.get(position).toString());	        
	        return dieView;
	    }
	}

	@SuppressWarnings("hiding")
	public class LogAdapter<RollResult> extends ArrayAdapter<RollResult> 
	{
		private ArrayList<RollResult> content;
		private int maximum;

		public LogAdapter(Context c) 
		{
			super(c, R.layout.list_item);
			this.content = new ArrayList<RollResult>();
			this.content.ensureCapacity(maxRetained);
			this.maximum = maxRetained;
		}
		
		public LogAdapter(Context c, ArrayList<RollResult> content, int maximum)
		{
			super(c, R.layout.list_item);
			this.content = new ArrayList<RollResult>();
			this.content.ensureCapacity(maxRetained);
			addAll(content);
			this.maximum = maximum;
		}
	
		public RollResult getItem(int position)
		{
			return content.get(position);
		}
		
		public View getView(int position, View convertView, ViewGroup parent) 
		{			
	        View v = convertView;
	        if (v == null) 
	        {
	            LayoutInflater inflator = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = inflator.inflate(R.layout.list_item, null);
	        }
	        
	        com.ribcakes.android.projects.dnd1.RollResult r = null;
	        if(position < this.content.size())
	        	r = (com.ribcakes.android.projects.dnd1.RollResult) this.content.get(position);
	        
	        if (r != null) 
	        {
	        	((TextView)v.findViewById(R.id.list_item_title)).setText(r.getRolled().toString());
	        	((TextView)v.findViewById(R.id.list_item_result)).setText(r.getResult()+"");
	        }
	        return v;
		}

		public void add(RollResult r) 
		{
			this.content.add(0, r);
			
			if(this.content.size() == this.maximum+1  && this.maximum != 0)
				remove(this.content.get(this.content.size()-1));

			super.add(r);
		}

		public void addAll(ArrayList<RollResult> r)
		{
			for(int i = r.size()-1; i >= 0; i--)
			{
				add(r.get(i));
			}
		}
		
		@Override
		public void remove(RollResult object) 
		{
			this.content.remove(object);
			super.remove(object);
		}
		
		public void removeAll() 
		{
			int size = this.content.size();
			
			for(int i = 0; i < size; i++)
			{
				remove(this.content.get(0));
			}
		}
		
		public void resize()
		{
			if(maxRetained > this.maximum && this.maximum != 0)
			{
				this.content.ensureCapacity(maxRetained);
				this.maximum = maxRetained;
			}
			else if(maxRetained == 0)
			{
				this.maximum = maxRetained;
			}
			else
			{
				for(int i = this.content.size(); i > maxRetained; i--)
				{
					remove(this.content.get(maxRetained));
				}
				this.content.trimToSize();
				this.maximum = maxRetained;
			}
		}
		
		public ArrayList<RollResult> getContent()
		{
			return this.content;
		}
	
		public int getMaximum()
		{
			return this.maximum;
		}
	}
	
}