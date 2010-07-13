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
		
	private EditText title;
	
	
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
		
		
		title = ((EditText)findViewById(R.id.title));
		
		
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
			
			title.setText(retained.getString("title"));
		}

		//if the bundle is null, initialize variables to their default values
		else
		{
			modifier = 0;
			currentEdit = new Die();
			adapter = new DieAdapter();	
			title.setText("");
		}
				
		//finds the gridView that will be holding the dice by its id
		content = (GridView)findViewById(R.id.create_die_set_content);
		
		//sets the adapter of the gridView to the adapter we instantiated earlier
		content.setAdapter(adapter);

		//sets a click listener for the items in the gridView
		content.setOnItemClickListener(
        		new OnItemClickListener() 
        		{
					//called when one of the die buttons is clicked
        			public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
        			{
        				//remove it from the adapter
        				adapter.remove(position);
        				
        				//and force a redraw
        				content.invalidateViews();
        			}
        		});
        
		//sets a long click listener for the items in the gridView
        content.setOnItemLongClickListener(
        		new OnItemLongClickListener() 
        		{
        			//called when an item is longClicked
					public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id)
					{
						//set the die to be edited as the one that was clicked
						currentEdit = (Die) adapter.getItem(position);
						
						//show the dialog to edit the die
						showDialog(EDIT_DIE);						

						//returns true to indicate that the long click was handled and that the 
						//onClick method for this view does not need to be run
						return true;
					}
        			
				});

        //finds the textView that will be holding the modifier by its id
        modifierView = (TextView)findViewById(R.id.modifier);
        
        //updates the view 
        updateModifierView();
        
        //sets a long click listener for the modifierView
        modifierView.setOnLongClickListener(
        		new OnLongClickListener()
        		{
					//called when the modifier view is longClicked
					public boolean onLongClick(View v) 
					{
						//reset the modifier
						modifier = 0;
						
						//update the view
						updateModifierView();

						//returns true to indicate that the long click was handled and that the 
						//onClick method for this view does not need to be run
						return true;
					}
				});
        
		}
	
	/**
	 * This method is called when a view directly associated with this method in xml is clicked 
	 * @param v the view that generated the event
	 */
	public void onClick(View v)
	{
		//determines what to do based on the id of the calling view
		switch(v.getId())
		{
			//if the add a die button is clicked
			case R.id.new_die:
				
				//open up the dialog to create a new die
				showDialog(NEW_DIE);
				
				break;
			
				
			//if the save button is clicked
			case R.id.save:		
				
				//and there is at least one die in the adapter
				if(adapter.getDice().size() > 0)
				{
					//create an intent
					Intent i = new Intent();
					
					//put the die set that was just created into it
					i.putExtra("dieSetParcel", new DieSet(title.getText().toString(),adapter.getDice(), modifier));
					
					//and set the result as ok with the intent
					setResult(RESULT_OK, i);
			
					//and end the activity sending the result and intent back to the calling activity
					finish();
				}
				break;
				
			//if the +1 button was clicked
			case R.id.plus_one:
				
				//add one to the modifier
				modifier ++;
				
				//and update the display
				updateModifierView();
				
				break;
				
				
			//if the -1 button was clicked
			case R.id.minus_one:
				
				//subtract one from the modifier
				modifier --;
				
				//and update the display
				updateModifierView();
				
				break;
				
		}
		
		//regardless of what happened, redraw the gridVeiw to ensure the view is up to date
		content.invalidateViews();
	}

	/**
	 * Called when the value of the modifier is changed to ensure the display is correct
	 */
	private void updateModifierView() 
	{
		//if the modifier is more than or equal to 0
		if(modifier >= 0)
			
			//add a + to the front of it in the textView to indicate it is positive
			modifierView.setText("+"+modifier);
		
		//if it is less than 0
		else
			
			//don't do anything because a negative integer already includes a - sign
			modifierView.setText(""+modifier);
	}

	/**
	 * Called when showDialog(int) is called to create the dialog based on the int passed there
	 * @param id the id of the dialog to be created
	 * @return the constructed dialog
	 */
	@Override
	protected Dialog onCreateDialog(int id) 
	{	
		//instantiates the inflater used to inflate any custom layouts
		LayoutInflater factory = LayoutInflater.from(this);

		//inflates the custom layout so we can use it in the dialog we are building
	    final View dieDialogLayout = factory.inflate(R.layout.die_dialog, null);

	    //grab the two edit texts from the layout to simplify click listener declarations
	    final EditText coefficient = (EditText)dieDialogLayout.findViewById(R.id.coefficient);
	    final EditText value = (EditText)dieDialogLayout.findViewById(R.id.value);
	    
	    //add a click listener for the + button under the coefficient editText
	    ((Button)dieDialogLayout.findViewById(R.id.coefficient_plus)).setOnClickListener(
	    		new OnClickListener() 
	    		{
					//called when the + button under the coefficient editText is clicked
					public void onClick(View v) 
					{
						//get the current value from the editText
						//because the editText is marked as inputType="number" in the xml
						//no try/catch block is needed because the text returned will always
						//be a parsable integer
						int temp = Integer.parseInt(coefficient.getText().toString());
						
						//incriment the value
						temp ++;
						
						//and update the editText 
						coefficient.setText(temp+"");
					}
				});

	    
	    //add a click listener for the - button under the coefficient editText
	    ((Button)dieDialogLayout.findViewById(R.id.coefficient_minus)).setOnClickListener(
	    		new OnClickListener() 
	    		{
					
					//called when the - button under the coefficient editText is clicked
					public void onClick(View v) 
					{
						//get the current value from the editText
						int temp = Integer.parseInt(coefficient.getText().toString());
						
						//and the value is greater than 1
						//this is required because it impossible to roll less than 1 die
						if(temp > 1)
						{
							//decrement the value 
							temp --;
							
							//and update the editText 
							coefficient.setText(temp+"");
						}
					}
				});
	    
	    

	    //add a click listener for the + button under the value editText
	    ((Button)dieDialogLayout.findViewById(R.id.value_plus)).setOnClickListener(
	    		new OnClickListener() 
	    		{
					
					//called when the + button under the value editText is clicked
					public void onClick(View v) 
					{
						//get the current value from the editText
						int temp = Integer.parseInt(value.getText().toString());
						
						//increment the value 
						temp ++;
						
						//and update the editText 
						value.setText(temp+"");
					}
				});

	    //add a click listener for the - button under the value editText
	    ((Button)dieDialogLayout.findViewById(R.id.value_minus)).setOnClickListener(
	    		new OnClickListener() 
	    		{
					
					//called when the - button under the value editText is clicked
					public void onClick(View v) 
					{
						//get the current value from the editText
						int temp = Integer.parseInt(value.getText().toString());
						
						//and the value is greater than 2
						//this is required because it impossible to generate
						//a random number between 1 and 1
						if(temp > 2)
						{
							//decrement the value 
							temp --;
							
							//and update the editText 
							value.setText(temp+"");
						}
					}
				});
	    
	    
	    //initialize the variable to be returned
	    AlertDialog dieDialog = new AlertDialog.Builder(this).create();
	    
		//determine which dialog to create 
	    switch(id)
	    {
	    	//if the new die dialog is to be created
	    	case NEW_DIE:
	    		
	    		//generate an instance of the dialog builder
			    dieDialog = new AlertDialog.Builder(this)

			    	//set the title to the provided resource 
			    	.setTitle(R.string.new_die_dialog_title)
			    	
			    	//set the main view of the dialog to the custom layout
			    	.setView(dieDialogLayout)
			    	
			    	//sets the dialog's positive button text and listener
			        .setPositiveButton("Ok", 
			        		new DialogInterface.OnClickListener() 
			                {
			        			//called when then positive button is clicked
			                    public void onClick(DialogInterface dialog, int whichButton) 
			                    {
			                    	//if the values in the dialog are valid values for a die
			                    	if(Integer.parseInt(coefficient.getText().toString()) > 0 && Integer.parseInt(value.getText().toString()) > 1)
			                    	{
			                    		//add a new die to the adapter with the values in the dialog 
			                    		adapter.add(new Die(Integer.parseInt(coefficient.getText().toString()),
		                    								Integer.parseInt(value.getText().toString())));
			                    		
			                    		//force a re-draw of the gridView
			                    		content.invalidateViews();

			                    	}
			                    	
									//removes the dialog from the user's view
			                    	dialog.dismiss();
			                    }
			                })

	                //sets the negative button's text and listener		
			        .setNegativeButton("Cancel", 
			        		new DialogInterface.OnClickListener() 
			        		{
			        			//called when the negative button is clicked
			        			public void onClick(DialogInterface dialog, int whichButton) 
			                    {
									//removes the dialog from the user's view
			        				dialog.dismiss();
			                    }
			                })
	            
	                //take the defined attributes and make them into a Dialog object
			        .create();
		    	break;
		    	
		    	
	    	//if an edit die dialog is to be created
	    	case EDIT_DIE:

	    		//generate an instance of the dialog builder
	    		dieDialog = new AlertDialog.Builder(this)

	    		//set the title to the provided resource 
		    	.setTitle(R.string.edit_die_dialog_title)

		    	//set the main view of the dialog to the custom layout
		    	.setView(dieDialogLayout)

		    	//sets the dialog's positive button text and listener
		    	.setPositiveButton("Ok", 
		        		new DialogInterface.OnClickListener() 
		                {
		    				//called when then positive button is clicked
		                    public void onClick(DialogInterface dialog, int whichButton) 
		                    {
		                    	//if the new value in the editText is a valid value
		                    	if(Integer.parseInt(value.getText().toString()) > 1)

		                    		//update the die being edited
		                    		currentEdit.setValue(Integer.parseInt(value.getText().toString()));

		                    	//if the new coefficient in the editText is a valid coefficient
		                    	if(Integer.parseInt(coefficient.getText().toString()) > 0)

		                    		//update the die being edited
		                    		currentEdit.setCoefficient(Integer.parseInt(coefficient.getText().toString()));
		                    	
		                    	//force a re-draw of the gridView
	                    		content.invalidateViews();
		                    }
		                })

                //sets the negative button's text and listener		
               .setNegativeButton("Cancel", 
		        		new DialogInterface.OnClickListener() 
		        		{
            	   			//called when the negative button is clicked
		                    public void onClick(DialogInterface dialog, int whichButton) 
		                    {
								//removes the dialog from the user's view
		        				dialog.dismiss();
		                    }
		                })

                //take the defined attributes and make them into a Dialog object
                .create();
	    		break;
	    }
		
		return dieDialog;
	}


	/**
	 * The Android OS hold on to dialogs after they are dismissed, so this
	 * method is used to update the state of the dialog each time it is shown
	 */
	protected void onPrepareDialog(int id, Dialog dialog)	
	{
		//determine which kind of dialog is going to be shown
		switch(id)
		{
			//if it is a dialog for a new die, load in default values (1d6)
			case NEW_DIE:	    	    
		    	((EditText)dialog.findViewById(R.id.coefficient)).setText(1+"");
		    	((EditText)dialog.findViewById(R.id.value)).setText(6+"");
		    	break;
		    	
	    	//if it is a dialog for editing an existing dialog, load in that die's values 
			case EDIT_DIE:
		    	((EditText)dialog.findViewById(R.id.coefficient)).setText(currentEdit.getCoefficient()+"");
		    	((EditText)dialog.findViewById(R.id.value)).setText(currentEdit.getValue()+"");
				break;
		}
		
		super.onPrepareDialog(id, dialog);
	}

	/**
	 * Called when the activity is being destroyed as part of a configuration change
	 * @return an object representing the activity's previous state 	
	 */
	@Override
	public Object onRetainNonConfigurationInstance()
	{
		//declare our object to be returned
		Bundle retain = new Bundle();

		//put the current modifier
		retain.putInt("modifier", modifier);
		
		//the current title
		retain.putString("title", title.getText().toString());
		
		//the die being edited
		retain.putParcelable("currentEdit", currentEdit);		
		
		//and the list of dice already created
		retain.putParcelableArrayList("currentDieSet", adapter.getDice());
		
		return retain;
	}



	/**
	 * 
	 * @author Brian Stambaugh
	 *
	 * Class used to render the dice properly in the gridView
	 */
	public class DieAdapter extends BaseAdapter 
	{
		//variable to keep track of the dice
	    private ArrayList<Die> dice;
	    
	    /**
	     * Public Constructor - Default Constructor
	     */
	    public DieAdapter()
	    {
	        dice = new ArrayList<Die>();
	    }
	    
	    /**
	     * Public Constructor
	     * @param dice	The content for the adapter to start with
	     */	    
	    @SuppressWarnings("unchecked")
		public DieAdapter(ArrayList<Die> dice)
	    {
	        this.dice = (ArrayList<Die>) dice.clone();
		}

		/**
		 * Adds the provided die to the adapter
		 * @param d the die to be added to the adapter
		 */
	    public void add(Die d)
	    {
	    	dice.add(d);
	    }
	  
	    /**
	     * Removes the die at the provided position
	     * @param position the position of the die to be removed
	     */	 
	    public void remove(int position) 
	    {
	    	dice.remove(position);
		}

	    /**
	     * Returns all of the dice contained in the adapter
	     * @return all the dice contained in this adapter
	     */
		public ArrayList<Die> getDice()
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
	    public Die getItem(int position) 
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
	
}
