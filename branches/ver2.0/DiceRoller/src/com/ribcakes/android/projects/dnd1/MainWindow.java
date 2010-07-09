package com.ribcakes.android.projects.dnd1;

/**
 * 
 * @author Brian Stambaugh
 *
 * Copyright 2010 Brian Stambaugh
 * This program is distributed under the terms of the GNU General Public License.
 * 
 *  This file is part of Rib's Roller.
 *
 *   Rib's Roller is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Rib's Roller is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Rib's Roller.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  
 *  
 */

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class MainWindow extends Activity
{
	
	private static final int CREATE_A_DIE_SET = 0;
	private static final int EDIT_DIE_SET = 1;
	private static final int DELETE_DIE_SET = 2;

	private static final String TAG = "Rib's Roller:MainWindow:";

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
	
	private final SensorEventListener mSensorListener = new SensorEventListener()
	{
			public void onAccuracyChanged(Sensor sensor, int accuracy) 
			{
				
			}

			public void onSensorChanged(SensorEvent event) 
			{
                if(event.sensor.getType() == SensorManager.SENSOR_ACCELEROMETER)
                {
               		float[] values = event.values;
                	
                        double forceThreshHold = .15f;
                       
                        double totalForce = 0.0f;
                        totalForce += Math.pow(values[SensorManager.DATA_X]/SensorManager.GRAVITY_EARTH, 2.0);
                        totalForce += Math.pow(values[SensorManager.DATA_Y]/SensorManager.GRAVITY_EARTH, 2.0);
                        totalForce += Math.pow(values[SensorManager.DATA_Z]/SensorManager.GRAVITY_EARTH, 2.0);
                        totalForce = Math.sqrt(totalForce);
                        
                        double netForce = 0;
                        netForce = totalForce - mLastForce;

                		Log.i(TAG+"mSensorListener", "net force: "+netForce);
                       
                        
                        if(netForce > forceThreshHold)
                        {
                    		Log.i(TAG+"mSensorListener", "sufficient force");
                    		makeResultFromFocused();
                        }
                       
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
		
		logAdapter = new LogAdapter<RollResult>(this);
		log = (ListView)findViewById(R.id.log);		
		Log.i(TAG+"onCreate()", "isFocusable: "+dieLibrary.isFocusable());
		log.setAdapter(logAdapter);	
		log.setOnItemClickListener(
				new OnItemClickListener()
				{
					
					public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
					{
						focusedResultContainer.setFocused(logAdapter.getItem(position));
					}
					
				});
		
		focusedResultContainer = (FocusedResult)findViewById(R.id.focused_result_container);
		focusedResult = null;
		
		registerForContextMenu(dieLibrary);
		
		mLastForce = 0.0f;
		
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);


		
		Thread thread = 
			new Thread("Import Database")
			{
				public void run()
				{
					createDatabase();
				}
			};
		thread.start();
		
		

		dieLibrary.setFocusable(true);
		dieLibrary.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
		Log.i(TAG+"onCreate()", "isFocusable: "+dieLibrary.isFocusable());
		
	
		dieLibrary.requestFocus();
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
		}
		catch (SQLException e)
		{
			Toast.makeText(this, "Database could not be opened/created properly.  Please clear application data to reset Database.", Toast.LENGTH_LONG);
		}
		fetchDieSetsFromDatabase();
	}

	private void fetchDieSetsFromDatabase()
	{
		Cursor dieCursor = mDbHelper.fetchAllDieSets();
		startManagingCursor(dieCursor);

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
		}
		else
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
        
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        
		super.onResume();
	}	
	
    @Override
    protected void onPause()
    {
            super.onPause();
           
            mSensorManager.unregisterListener(mSensorListener);
    }
	
	protected void onStop()
	{
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(getString(R.string.max_retained), maxRetained+"");
		editor.commit();
		
		super.onStop();
	}


	public class DieAdapter extends BaseAdapter 
	{
	    private ArrayList<DieSet> dice;
	    
	    public DieAdapter()
	    {
	        dice = new ArrayList<DieSet>();
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
	            dieView = inflator.inflate(R.layout.die_library_item, null);
             
	        }
	        else 
	        {
	            dieView = (TextView) convertView;
	        }

	        ((TextView)dieView.findViewById(R.id.die_library_item_text)).setText(dice.get(position).toString());	        
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
		
		
	}
	
}