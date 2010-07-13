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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

/**
 * This activity is called when the user wants to either
 * create a new die set, or edit an existing one
 */
public class CreateSet extends Activity 
{
	//constants used for dialogs
	private static final int NEW_DIE = 0;
	private static final int EDIT_DIE = 1;
	
	
	//gridView used to hold the the different dice
	private GridView content;
	
	//TextView used to display the modifier for the die set
	private TextView modifierView;
	
	
	//the adapter to hold the dice
	private DieAdapter adapter;
	
	//the die currently being edited
	private Die currentEdit;
	
	
	//the modifier for the current die set
	private int modifier;
	
	/**
	 * This method is called when the activity is first created to set the view for the
	 * activity as well as do any global variable initialization necessary for the application
	 */
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		//sets the view of the activity to that defined in main.xml
		setContentView(R.layout.create_die_set);
		
		//set the result of the activity to canceled 
		setResult(RESULT_CANCELED);
		
		
		//checks to see if there is any data that was saved from a previous instance of the application
		//this data only exists when the phone's configuration is changed and the application re-launched
		Bundle retained = (Bundle) getLastNonConfigurationInstance();	

		//if there was no previous data 
		if(retained == null)
			//check the intent that started the activity for data 
			retained = getIntent().getExtras();
		
		
		//if the bundle isn't null
		if(retained != null)
		{
			//get the modifier from the bundle
			modifier = retained.getInt("modifier", 0);
			
			
			//if the bundle contained data for the current edit, get it and set it
			//if not, create a new default die and put it in current edit
			currentEdit = (Die) ((retained.getParcelable("currentEdit") == null) ?
					new Die():retained.getParcelable("currentEdit"));

			
			//if the bundle contained data for the current die set load it in to a local variable
			//if not, create a new arrayList
			@SuppressWarnings("unchecked")
			ArrayList<Die> dice = (ArrayList<Die>) ((retained.getParcelableArrayList("currentDieSet") == null) ?
					new ArrayList<Die>():retained.getParcelableArrayList("currentDieSet"));
			
			//instantiate the dieAdapter with the arrayList of dice as the initial data set
			adapter = new DieAdapter(dice);
		}

		//if the bundle is null, initialize variables to their default values
		else
		{
			modifier = 0;
			currentEdit = new Die();
			adapter = new DieAdapter();				
		}
				
		//finds the gridView that will be holding the dice by its id
		content = (GridView)findViewById(R.id.create_die_set_content);
		
		//sets the adapter of the gridView to the adapter we instantiated earlier
		content.setAdapter(adapter);

		content.setOnItemClickListener(
        		new OnItemClickListener() 
        		{
        			public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
        			{
        				adapter.remove(position);
        				content.invalidateViews();
        			}
        		});
        
        content.setOnItemLongClickListener(
        		new OnItemLongClickListener() 
        		{

					public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id)
					{
						currentEdit = (Die) adapter.getItem(position);
						showDialog(EDIT_DIE);						
						return true;
					}
        			
				});

        modifierView = (TextView)findViewById(R.id.modifier);
        updateModifierView();
        
        modifierView.setOnLongClickListener(
        		new OnLongClickListener()
        		{
					
					public boolean onLongClick(View v) 
					{
						modifier = 0;
						updateModifierView();
						return true;
					}
				});
        
		}
	
	public void onClick(View v)
	{
		switch(v.getId())
		{
			case R.id.new_die:
				showDialog(NEW_DIE);
				break;
			case R.id.save:		
				if(adapter.getDice().size() > 0)
				{
					Intent i = new Intent();
					i.putExtra("dieSetParcel", new DieSet(((EditText)findViewById(R.id.title)).getText().toString(),adapter.getDice(), modifier));
					setResult(RESULT_OK, i);
				}
				else
					Log.i("CreateSet:onClick()", "null set");
				finish();
				break;
				
			case R.id.plus_one:
				modifier ++;
				updateModifierView();
				break;
				
			case R.id.minus_one:
				modifier --;
				updateModifierView();
				break;
				
		}
		content.invalidateViews();
	}
	
	private void updateModifierView() 
	{
		if(modifier >= 0)
			modifierView.setText("+"+modifier);
		else
			modifierView.setText(""+modifier);
	}

	@Override
	protected Dialog onCreateDialog(int id) 
	{
		LayoutInflater factory = LayoutInflater.from(this);
	    final View dieDialogLayout = factory.inflate(R.layout.die_dialog, null);

	    final EditText coefficient = (EditText)dieDialogLayout.findViewById(R.id.coefficient);
	    final EditText value = (EditText)dieDialogLayout.findViewById(R.id.value);
	    
	    ((Button)dieDialogLayout.findViewById(R.id.coefficient_plus)).setOnClickListener(
	    		new OnClickListener() 
	    		{
					
					public void onClick(View v) 
					{
						int temp = Integer.parseInt(coefficient.getText().toString());
						temp ++;
						coefficient.setText(temp+"");
					}
				});

	    ((Button)dieDialogLayout.findViewById(R.id.coefficient_minus)).setOnClickListener(
	    		new OnClickListener() 
	    		{
					
					public void onClick(View v) 
					{
						int temp = Integer.parseInt(coefficient.getText().toString());
						
						if(temp > 1)
						{
							temp --;
							coefficient.setText(temp+"");
						}
					}
				});
	    
	    
	    ((Button)dieDialogLayout.findViewById(R.id.value_plus)).setOnClickListener(
	    		new OnClickListener() 
	    		{
					
					public void onClick(View v) 
					{
						int temp = Integer.parseInt(value.getText().toString());
						temp ++;
						value.setText(temp+"");
					}
				});

	    ((Button)dieDialogLayout.findViewById(R.id.value_minus)).setOnClickListener(
	    		new OnClickListener() 
	    		{
					
					public void onClick(View v) 
					{
						int temp = Integer.parseInt(value.getText().toString());
						
						if(temp > 2)
						{
							temp --;
							value.setText(temp+"");
						}
					}
				});
	    
	    AlertDialog dieDialog = new AlertDialog.Builder(this).create();
	    
	    switch(id)
	    {
	    	case NEW_DIE:
			    dieDialog = new AlertDialog.Builder(this)
			    	.setTitle(R.string.new_die_dialog_title)
			        .setView(dieDialogLayout)
			        .setPositiveButton("Ok", 
			        		new DialogInterface.OnClickListener() 
			                {
			                    public void onClick(DialogInterface dialog, int whichButton) 
			                    {
			                    	try
			                    	{
			                    		adapter.add(new Die(Integer.parseInt(((EditText)((Dialog)dialog).findViewById(R.id.coefficient)).getText().toString()),
		                    								Integer.parseInt(((EditText)((Dialog)dialog).findViewById(R.id.value)).getText().toString())));
			                    		content.invalidateViews();
			                    	}
			                    	catch (NumberFormatException e)
			                    	{
			                    		Log.e("Rib's Roller:CreateSet:onCreateDialog():onClick()", "NumberFormatException: "+e);
			                    	}
			                    }
			                })
			        .setNegativeButton("Cancel", 
			        		new DialogInterface.OnClickListener() 
			        		{
			                    public void onClick(DialogInterface dialog, int whichButton) 
			                    {
			                    	dialog.dismiss();
			                    }
			                })
			        .create();
		    	break;
		    	
	    	case EDIT_DIE:
			    dieDialog = new AlertDialog.Builder(this)
		    	.setTitle(R.string.edit_die_dialog_title)
		        .setView(dieDialogLayout)
		        .setPositiveButton("Ok", 
		        		new DialogInterface.OnClickListener() 
		                {
		                    public void onClick(DialogInterface dialog, int whichButton) 
		                    {
		                    	try
		                    	{
		                    		currentEdit.setCoefficient(Integer.parseInt(((EditText)((Dialog)dialog).findViewById(R.id.coefficient)).getText().toString()));
		                    		currentEdit.setValue(Integer.parseInt(((EditText)((Dialog)dialog).findViewById(R.id.value)).getText().toString()));
		                    		content.invalidateViews();
		                    	}
		                    	catch (NumberFormatException e)
		                    	{
		                    		Log.e("Rib's Roller:CreateSet:onCreateDialog():onClick()", "NumberFormatException: "+e);
		                    	}
		                    }
		                })
		        .setNegativeButton("Cancel", 
		        		new DialogInterface.OnClickListener() 
		        		{
		                    public void onClick(DialogInterface dialog, int whichButton) 
		                    {
		                    	dialog.dismiss();
		                    }
		                })
		        .create();
	    		break;
	    }
		
		return dieDialog;
	}

	protected void onPrepareDialog(int id, Dialog dialog)
	{
		switch(id)
		{
			case NEW_DIE:	    	    
		    	((EditText)dialog.findViewById(R.id.coefficient)).setText(1+"");
		    	((EditText)dialog.findViewById(R.id.value)).setText(6+"");
		    	break;
		    	
			case EDIT_DIE:
		    	((EditText)dialog.findViewById(R.id.coefficient)).setText(currentEdit.getCoefficient()+"");
		    	((EditText)dialog.findViewById(R.id.value)).setText(currentEdit.getValue()+"");
				break;
		}
		
		super.onPrepareDialog(id, dialog);
	}


	@Override
	public Object onRetainNonConfigurationInstance()
	{
		Bundle retain = new Bundle();
		retain.putInt("modifier", modifier);
		retain.putParcelable("currentEdit", currentEdit);		
		retain.putParcelableArrayList("currentDieSet", adapter.getDice());
		
		return retain;
	}





	public class DieAdapter extends BaseAdapter 
	{
	    private ArrayList<Die> dice;
	    
	    public DieAdapter()
	    {
	        dice = new ArrayList<Die>();
	    }
	    
	    @SuppressWarnings("unchecked")
		public DieAdapter(ArrayList<Die> dice)
	    {
	        this.dice = (ArrayList<Die>) dice.clone();
		}

		public ArrayList<Die> getDice()
	    {
	    	return this.dice;
	    }

	    public void remove(int position) 
	    {
	    	dice.remove(position);
		}
	    
	    public void add(Die d)
	    {
	    	dice.add(d);
	    }

		public int getCount() 
		{
	        return dice.size();
	    }

	    public Object getItem(int position) 
	    {
	        return dice.get(position);
	    }

	    public long getItemId(int position) 
	    {
	        return 0;
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
	            dieView = (TextView) convertView;
	        }

	        ((TextView)dieView.findViewById(R.id.die_item_text)).setText(dice.get(position).toString());	        
	        return dieView;
	    }
	}
	
}
