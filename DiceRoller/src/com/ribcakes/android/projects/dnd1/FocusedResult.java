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

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This class is used to display specific information about 
 * the roll that was just performed.  It contains textViews for
 * both the title and the result, along with a grid view to 
 * display the individual dice
 */
public class FocusedResult extends LinearLayout
{
	//the gridView that will be holding the individual dice
	private GridView grid;
	
	//the adapter used for managing the gridView
	private ResultAdapter adapter;
	
	//the context of the activity displaying this widget
	private Context context;
	
	/**
	 * Public Constructor 
	 * @param context the context of the activity displaying this widget
	 * @param attrs the attributes associated with this widget
	 */
	public FocusedResult(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		
		//saves the context for later use
		this.context = context;
		
		
		//get an instance of the layout inflater
		LayoutInflater layoutInflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		//use it to inflate the custom xml associated with this widget
		@SuppressWarnings("unused")
		View view = layoutInflater.inflate(R.layout.focused_result,this);
		
		
		//instantiate the result adapter
		adapter = new ResultAdapter();
		
		//find the gridView using it ID
		grid = (GridView)findViewById(R.id.focused_result_grid);
		
		//set the adapter of the gridView to the previously instantiated adapter
		grid.setAdapter(adapter);
	}

	/**
	 * Updates the view to display the result that was just generated
	 * @param result	the result that was just generated to update the widget
	 */
	public void setFocused(RollResult result) 
	{
		//set the girdView to be visible
		grid.setVisibility(VISIBLE);
		
		
		//clear the adapter of any previously entered data
		adapter.empty();
		
		//set the title and result textViews to the appropriate values
		((TextView)findViewById(R.id.focused_result_title)).setText(result.getRolled().toString());
		((TextView)findViewById(R.id.focused_result_result)).setText(result.getResult()+"");
		
		
		//fill the adapter with the data from the result
		adapter.fillGrid(result);
		
		//set the background color of the gridView
		grid.setBackgroundResource(R.color.grid_background);
		
		//force a redraw of the gridView
		grid.invalidateViews();
	}
	
	/**
	 * Clears the widget of all data except for the title of the most recently
	 * rolled dieSet
	 */
	public void clearFocused()
	{
		
		//clear the adapter of any previously entered data
		adapter.empty();
		
		//sets the background of the gridView to be transparent
		grid.setBackgroundResource(R.drawable.transparent00000000);
		
		//forces a redraw of the gridView
		grid.invalidateViews();
		
		//clears the result textView
		((TextView)findViewById(R.id.focused_result_result)).setText("");
		
		//sets the gridView to be invisible
		grid.setVisibility(INVISIBLE);
		
	}
	
	/**
	 * Completely clears the widget of all data including the title
	 */
	public void completelyClearFocused() 
	{
		//clears it as normal
		clearFocused();
		
		//clears the title textView
		((TextView)findViewById(R.id.focused_result_title)).setText("");
	}
	
	
	/**
	 * 
	 * @author Brian Stambaugh
	 *
	 * This class is used to handle the roll results in the gridView
	 */
	public class ResultAdapter extends BaseAdapter 
	{
		//a list of the results of the individual dice
	    private ArrayList<String> results;
	    
	    /**
	     * Public Constructor - Default Constructor
	     */
	    public ResultAdapter()
	    {
	        results = new ArrayList<String>();
	    }

	    /**
	     * Clears all the data contained in the adapter
	     */
		public void empty()
		{
			results.clear();
		}
	    
		/**
		 * Adds a String to the adapter
		 * @param d	the string to be added
		 */
	    public void add(String d)
	    {
	    	results.add(d);
	    }
	    
	    /**
	     * Takes a rollResult and uses it to fill the adapter with the 
	     * appropriate data
	     * @param result	the rollResult to be used to fill the adapter
	     */
	    public void fillGrid(RollResult result) 
		{
	    	//initialize variables to be used 
			ArrayList<Integer> results = result.getResults();
			ArrayList<Die> dice = result.getRolled().getDice();
			String complexResult = "";
			Die currentDie;
			int k = 0;
			
			
			//for every die in the dieSet that created this result
			for(int i = 0; i < dice.size(); i++)
			{
				//take the current one
				currentDie = dice.get(i);
				
				
				//for each iteration of the die represented by its coefficient
				for(int j = 0; j < currentDie.getCoefficient(); j++)
				{
					//reset the string
					complexResult = "";

					
					//add the result that was generated in bold tags
					complexResult += ("<b>" + results.get(k) + "</b>" );
				
					//add the value of the die next to it
					complexResult += "/("+currentDie.getValue()+")";
					
					//increment the value that tracks our position in the result array 
					k++;
					
					//add the string to the adapter
					add(complexResult);
				}
			}		
			
			//after all the dice have been added, get the modifier
			int modifier = result.getRolled().getModifier();
			
			//as long as the modifier isn't 0
			if(modifier != 0)
			{
				//if the modifier is greated than 0
				if(modifier > 0)
					
					//add a plus sign
					add("+"+modifier);
				
				//if it's less than 0
				else
					
					//there is no need to add anything
					//negative numbers come with a minus sign
					add(modifier+"");
			}
		}


		/**
		 * Returns the number of items in this adapter
		 * used by system to draw contents
		 */
		public int getCount() 
		{
	        return results.size();
	    }

		/**
		 * Returns the item at the provided position
		 */
	    public String getItem(int position) 
	    {
	        return results.get(position);
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
	    public View getView(final int position, View convertView, ViewGroup parent)
	    {
	    	//if the position provided is a valid position in the array
	    	if(position < getCount() && position >= 0)
	    	{
		    	//declare the view
		        View resultView;

		        //if we don't have a recycled view	    
		        if (convertView == null) 
		        {	
		        	//get a layout inflater
		            LayoutInflater inflator = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		            //and inflate an xml to be our view
		            resultView = inflator.inflate(R.layout.focused_result_item, null);
	             
		        }

		        //if it's recycled
		        else 
		        {

		        	//set the view to be returned as the recycled view
		        	resultView = convertView;
		        }	     
		        
		        //set the text of the view to be the string at position position
		        ((TextView)resultView.findViewById(R.id.focused_result_item)).setText(Html.fromHtml(results.get(position)));	        
		        return resultView;
	    	}
	    	return null;
	    }
	}

}
