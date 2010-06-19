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

import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainWindow extends Activity
{
	private Random gen;
	private int result;
	private TextView[] trackers;
	private RotatingQueue rolls;

	private SharedPreferences preferences;
	private int[] buttonValues;
	private Button[] buttons;
	private int currentEdit;
	private Dialog changeValueDialog;
	private TextView value;	
	
	static final int CHANGE_VALUE_DIALOG = 1;
	static final String PREFERENCE_NAME = "Main Window"; 

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        buttonValues = new int[8]; //the values of the 7 buttons
        buttons = new Button[8]; // the 7 buttons themselves
        	
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
               
        result = -1; //result of a roll
        

        Bundle retained = (Bundle) getLastNonConfigurationInstance();       
        if(retained != null)
        {
        	//queue that takes care of the results displayed in the log
        	rolls = (retained.getStringArray("rolls") == null) ? null : new RotatingQueue(6, retained.getStringArray("rolls"),
    																						retained.getInt("head"), retained.getInt("tail"));
	        
        	//the array location of the current editing button
        	currentEdit = retained.getInt("currentEdit", -1);
        }
        else
        {
        	currentEdit = -1;
        	rolls = null;
        }        

        trackers = new TextView[2];//the two text views that display the log                 
        if(rolls == null) //if the program has just been launched, start with null log, else refresh the views
        {
        	setViews(false);
        	rolls = new RotatingQueue(6);
        }
        else
        	setViews(true);
        
        manageCustomValues();//retrieves custom values and sets onLongClick listeners
    }    
    
	private void manageCustomValues()//retrieves custom values and sets onLongClick listeners and refreshes the text on the buttons
	{
        
        buttonValues[0] = preferences.getInt(getString(R.string.d20), 20);
        buttonValues[1] = preferences.getInt("d_custom", 0);
        buttonValues[2] = preferences.getInt(getString(R.string.d4), 4);
        buttonValues[3] = preferences.getInt(getString(R.string.d6), 6);
        buttonValues[4] = preferences.getInt(getString(R.string.d8), 8);
        buttonValues[5] = preferences.getInt(getString(R.string.d10), 10);
        buttonValues[6] = preferences.getInt(getString(R.string.d12), 12);
        buttonValues[7] = preferences.getInt(getString(R.string.dPercent), 100);
  
        
        
        buttons[0] = (Button) findViewById(R.id.d20);           
        
        buttons[0].setOnLongClickListener(
        		new OnLongClickListener() 
        		{
			
					@Override
					public boolean onLongClick(View v) 
					{
						changeButtonValue(v);
						
						return true;
					}

				});
        
        buttons[2] = (Button) findViewById(R.id.d4);        
        buttons[2].setOnLongClickListener(
        		new OnLongClickListener() 
        		{
			
					@Override
					public boolean onLongClick(View v) 
					{
						changeButtonValue(v);
						
						return true;
					}

				});
        
        buttons[3] = (Button) findViewById(R.id.d6);        
        buttons[3].setOnLongClickListener(
        		new OnLongClickListener() 
        		{
			
					@Override
					public boolean onLongClick(View v) 
					{
						changeButtonValue(v);
						
						return true;
					}

				});
        
        buttons[4] = (Button) findViewById(R.id.d8);        
        buttons[4].setOnLongClickListener(
        		new OnLongClickListener() 
        		{
			
					@Override
					public boolean onLongClick(View v) 
					{
						changeButtonValue(v);
						
						return true;
					}

				});
        
        buttons[5] = (Button) findViewById(R.id.d10);        
        buttons[5].setOnLongClickListener(
        		new OnLongClickListener() 
        		{
			
					@Override
					public boolean onLongClick(View v) 
					{
						changeButtonValue(v);
						
						return true;
					}

				});
        
        buttons[6] = (Button) findViewById(R.id.d12);        
        buttons[6].setOnLongClickListener(
        		new OnLongClickListener() 
        		{
			
					@Override
					public boolean onLongClick(View v) 
					{
						changeButtonValue(v);
						
						return true;
					}

				});
        
        buttons[7] = (Button) findViewById(R.id.dPercent);        
        buttons[7].setOnLongClickListener(
        		new OnLongClickListener() 
        		{
			
					@Override
					public boolean onLongClick(View v) 
					{
						changeButtonValue(v);
						
						return true;
					}

				});
        
        refreshButtonLabels();
        
	}
		
	private void refreshButtonLabels() 
	{
        
        for(int i = 0; i < 8; i++)
        {
        	if(i == 1)
        		continue;
        	
        	buttons[i].setText("");
        	buttons[i].setText("d"+buttonValues[i]);
        }
        
	}
	
	private void changeButtonValue(View v) 
	{
		
    	switch (v.getId())
    	{
    		case R.id.d20:
    			currentEdit = 0;
    			break;
    		case R.id.d6:
    			currentEdit = 3;
    			break;
			case R.id.d4:
    			currentEdit = 2;
    			break;
    		case R.id.d8:
    			currentEdit = 4;
    			break;
    		case R.id.d10:
    			currentEdit = 5;
    			break;
    		case R.id.d12:
    			currentEdit = 6;
    			break;
    		case R.id.dPercent:
    			currentEdit = 7;
    			break;
    	}
    	
    	showDialog(CHANGE_VALUE_DIALOG);
	}
	
	protected Dialog onCreateDialog(int id) //creates value change dialog
	{	    	    	
		
		LayoutInflater factory = LayoutInflater.from(this);
	    final View changeValueDialogLayout = factory.inflate(R.layout.change_value_dialog, null);
	    	    
	    value = (TextView) changeValueDialogLayout.findViewById(R.id.value);
	    
	    changeValueDialog = new AlertDialog.Builder(this)
	        .setTitle(R.string.change_value_dialog_title)
	        .setView(changeValueDialogLayout)
	        .setPositiveButton("Ok", 
	        		new DialogInterface.OnClickListener() 
	                {
	                    public void onClick(DialogInterface dialog, int whichButton) 
	                    {
	                    	try
	                    	{
                    			int newValue = Integer.parseInt(value.getText().toString());	                    	
                    			setNewValue(newValue);
	                    	}
	                    	catch (NumberFormatException e)
	                    	{
	                    		Log.i("MainWindow:onCreateDialog():onClick()", "NumberFormatException: "+e);
	                    	}
	                    }
	                })
	        .setNegativeButton("Cancel", 
	        		new DialogInterface.OnClickListener() 
	        		{
	                    public void onClick(DialogInterface dialog, int whichButton) 
	                    {
	                    	//canceling does nothing!
	                    }
	                })
	        .create();
	    
	    	onPrepareDialog(CHANGE_VALUE_DIALOG, changeValueDialog);
		
	    return changeValueDialog;
    } 
		
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) //makes sure dialog displays correct value
	{
		super.onPrepareDialog(id, dialog);
		
	    value.setText(buttonValues[currentEdit]+"");

	}

	private void setNewValue(int value)
	{
    	if(value >= 2)
    	{
    		buttonValues[currentEdit] = value;
    	}
    	else
    	{
    		Toast.makeText(this, "Cannot randomly generate a number between 1 and "+value+".  Please try again.", Toast.LENGTH_LONG);
    	}
		
		refreshButtonLabels();
		
	}
		
	private void setViews(boolean updateViews) //refreshes the views on layout change and program initialization
    {
		setContentView(R.layout.alternate);
        
        trackers[0] = (TextView) findViewById(R.id.tracker1);
        trackers[1] = (TextView) findViewById(R.id.tracker2);
    
        if(updateViews)
        	rolls.updateView(trackers);
        
    }
	
	@Override
	protected void onResume() 
	{
        manageCustomValues();//retrieves custom values and sets onLongClick listeners
		super.onResume();
	}
	
	@Override
    protected void onStop() //saves the custom values of the buttons when the program is moved back from the front
    {
       super.onStop();

 
      SharedPreferences settings = getSharedPreferences(PREFERENCE_NAME, 0);
      SharedPreferences.Editor editor = settings.edit();

      editor.putString("BALL", "I AM A FUCKING BALL BITCH");
      editor.putInt(getString(R.string.d20), buttonValues[0]);
      editor.putInt(getString(R.string.d4), buttonValues[2]);
      editor.putInt(getString(R.string.d6), buttonValues[3]);
      editor.putInt(getString(R.string.d8), buttonValues[4]);
      editor.putInt(getString(R.string.d10), buttonValues[5]);
      editor.putInt(getString(R.string.d12), buttonValues[6]);
      editor.putInt(getString(R.string.dPercent), buttonValues[7]);
      
      editor.commit();    
    }
    	    
	public void clickHandler(View view) //Dice
    {		
    	gen = new Random();
    	result = -1;
    	String type = "";
    	
    	switch (view.getId())
    	{
    		case R.id.d20:
    			result = gen.nextInt(buttonValues[0])+1;
    			type = "d20";
    			break;
    		case R.id.d6:
    			result = gen.nextInt(buttonValues[3])+1;
    			type = "d6";
    			break;
			case R.id.d4:
    			result = gen.nextInt(buttonValues[2])+1;
    			type = "d4";
    			break;
    		case R.id.d8:
    			result = gen.nextInt(buttonValues[4])+1;
    			type = "d8";
    			break;
    		case R.id.d10:
    			result = gen.nextInt(buttonValues[5])+1;
    			type = "d10";
    			break;
    		case R.id.d12:
    			result = gen.nextInt(buttonValues[6])+1;
    			type = "d12";
    			break;
    		case R.id.dPercent:
    			result = gen.nextInt(buttonValues[7])+1;
    			type = "d100";
    			break;
    	}
    	
    	if(result >= 0)
    	{    		
    		rolls.add(type+": "+result);
    		rolls.updateView(trackers);
    	}    	
    }
    
    public void clickListener(View view) //Buttons
    {
    	switch(view.getId())
    	{
    		case R.id.clearbutton:
    			for(TextView i : trackers)
    				i.setText("");
    			rolls = new RotatingQueue(6);
    			break;
    		case R.id.quitbutton:
    			finish();
    			System.exit(0);
    	}
    }
  
    public void customDialogButtonsOnClick(View v)
    {
    	int temp = -1;
    	
    	switch(v.getId())
    	{
    		case R.id.plus:
    			temp = Integer.parseInt(value.getText().toString());
    			temp++;
				value.setText(""+temp);
    			break;
    		case R.id.minus:
    			temp = Integer.parseInt(value.getText().toString());
    			temp--;
				value.setText(""+temp);
    			break;
    	}
    }
    
    public Bundle onRetainNonConfigurationInstance() //retains queue for log retention on orientation change
    {
    	Bundle retain = new Bundle();
    	
    	retain.putStringArray("rolls", rolls.getLog());
    	retain.putInt("head", rolls.getHead());
    	retain.putInt("tail", rolls.getTail());
    	retain.putInt("currentEdit", currentEdit);
    	
    	return retain;
    	
    }
    
    public boolean onCreateOptionsMenu(Menu menu) 
    {               
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu, menu);
            return true;
    }


    public boolean onOptionsItemSelected(MenuItem item) 
    {
            switch (item.getItemId()) 
            {
            
            	case R.id.preferences:                   
                    Intent i = new Intent(this, Preferences.class);
                    startActivity(i);
                    break;
                    
            }                                                       
            return true;
    }

    
    
}