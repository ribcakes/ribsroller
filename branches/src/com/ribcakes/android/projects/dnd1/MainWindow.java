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
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;


public class MainWindow extends Activity implements OnClickListener
{
	private Random gen;
	private int result;
	private TextView[] trackers;
	private rotatingQueue rolls;
	private TextView text;
	private AlertDialog alertDialog;
	private SharedPreferences preferences;
	private boolean dialogState;
	private int layoutState;
	
	static final int DIALOG_RESULT = 1;
	static final String PREFERENCE_NAME = "Main Window"; 
	static final String CHECKED_BOOLEAN_NAME ="Show Dialog";
	static final int IMAGES = 1;
	static final int BUTTONS = 2;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        layoutState = Integer.parseInt(preferences.getString(getString(R.string.layout_preference), "1"));

        
        result = -1;
        
        rolls = (rotatingQueue) getLastNonConfigurationInstance();        
        
        trackers = new TextView[2];

        setViews(false);
        
        trackers[0].setText("i work lul");
        
        if(rolls == null)
        	rolls = new rotatingQueue(6);
        else
    		rolls.updateView(trackers);
        
        dialogState = preferences.getBoolean(getString(R.string.checked), false);
        
                
    }
 
    private void setViews(boolean updateViews)
    {
        if(layoutState == BUTTONS)
        	setContentView(R.layout.alternate);
        else
        	setContentView(R.layout.main);
        
        trackers[0] = (TextView) findViewById(R.id.tracker1);
        trackers[1] = (TextView) findViewById(R.id.tracker2);
    
        if(updateViews)
        	rolls.updateView(trackers);
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
			
			Intent i = new Intent(this, com.ribcakes.android.projects.dnd1.Preferences.class);
			startActivity(i);
			
		}							
		return true;
	}
	
    protected void onResume()
    {    	
    	dialogState = preferences.getBoolean(getString(R.string.checked), true);
        layoutState = Integer.parseInt(preferences.getString(getString(R.string.layout_preference), "1"));
    	
    	setViews(true);
    	
    	super.onResume();
    }
	
	
	@Override
    protected void onStop()
    {
       super.onStop();

 
      SharedPreferences settings = getSharedPreferences(PREFERENCE_NAME, 0);
      SharedPreferences.Editor editor = settings.edit();
      editor.putBoolean(getString(R.string.checked), dialogState);
      editor.putString(getString(R.string.layout_preference), layoutState+"");

      editor.commit();
    }

    
    protected void onPrepareDialog(int id, Dialog dialog)
    {
    	text.setText("");
    	text.setText(result+"");
    }
    

    protected Dialog onCreateDialog(int id) 
    {    	    	

    	AlertDialog.Builder builder;
    	
    	
    	LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
    	View layout = inflater.inflate(R.layout.custom_dialog,
    	                               (ViewGroup) findViewById(R.id.layout_root));	
    	
    	text = (TextView) layout.findViewById(R.id.result);  
    			
    	builder = new AlertDialog.Builder(this);
    	builder.setView(layout);
    	
    	alertDialog = builder.create();
    	

		onPrepareDialog(DIALOG_RESULT, alertDialog);		
    	
		
		
		return alertDialog;    	    	    	    	    	   
    }    

    
    public void onClick(View v)
    {
    	alertDialog.dismiss();
    }
    
    
	public void clickHandler(View view)
    {
		Log.i("clickHandler", "made it in");
		
		
    	gen = new Random();
    	result = -1;
    	String type = "";
    	
    	switch (view.getId())
    	{
    		case R.id.d20:
    			result = gen.nextInt(20)+1;
    			type = "d20";
    			break;
    		case R.id.d6:
    			result = gen.nextInt(6)+1;
    			type = "d6";
    			break;
			case R.id.d4:
    			result = gen.nextInt(4)+1;
    			type = "d4";
    			break;
    		case R.id.d8:
    			result = gen.nextInt(8)+1;
    			type = "d8";
    			break;
    		case R.id.d10:
    			result = gen.nextInt(10)+1;
    			type = "d10";
    			break;
    		case R.id.d12:
    			result = gen.nextInt(12)+1;
    			type = "d12";
    			break;
    		case R.id.dPercent:
    			result = gen.nextInt(100)+1;
    			type = "d100";
    			break;
    	}
    	
    	Log.i("clickHandler", "Dialog State: "+dialogState);
    	Log.i("clickHandler", "Layout State: "+layoutState);
    	Log.i("clickHandler", "Result: "+result);
    	
    	
    	if(dialogState)
    	{
    		showDialog(DIALOG_RESULT);
    	}
    	
    	if(result >= 0)
    	{
    		Log.i("clickHandler", "in rolls");
    		
    		rolls.add(type+": "+result);
    		rolls.updateView(trackers);
    	}    	
    }
    
    public void clickListener(View view)
    {
    	switch(view.getId())
    	{
    		case R.id.clearbutton:
    			for(TextView i : trackers)
    				i.setText("");
    			rolls = new rotatingQueue(6);
    			break;
    		case R.id.quitbutton:
    			finish();
    			System.exit(0);
    	}
    }
    
    public rotatingQueue onRetainNonConfigurationInstance()
    {
    	return rolls;
    }
    
    
}