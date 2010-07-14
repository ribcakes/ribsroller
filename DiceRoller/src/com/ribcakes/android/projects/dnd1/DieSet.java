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

import android.os.Parcel;
import android.os.Parcelable;

public class DieSet implements Parcelable
{
	//the list of dice contained in this die set
	private ArrayList<Die> dice;
	
	//the modifier for this die set
	private int modifier;
	
	//the title of this die set
	private String title;
	
	//the row in the database that contains a reference to this die set
	private long databaseRowID;

	
	/**
	 * Public Constructor - Default Constructor
	 */
	public DieSet()
	{
		dice = new ArrayList<Die>();
		modifier = 0;
		title = "";
		databaseRowID = -1;
	}
	
	
	/**
	 * Public Constructor
	 * @param title the title of the die set
	 * @param dice the dice contained in the die set
	 * @param modifier the modifier associated with the die set
	 */
	public DieSet(String title, ArrayList<Die> dice, int modifier) 
	{
		this.dice = dice;
		this.modifier = modifier;
		this.title = title;
		this.databaseRowID = -1;
	}

	/**
	 * Public Constructor
	 * @param d the die from which to create the die set
	 */
	public DieSet(Die d)
	{
        this.dice = new ArrayList<Die>();
        dice.add(d);
        
        this.title = "";
        this.modifier = 0;
        this.databaseRowID = -1;

	}
	
	/**
	 * Public Constructor
	 * @param source	the parcel from which to inflate the die set
	 */
	@SuppressWarnings("unchecked")
	public DieSet(Parcel source) 
	{
		this.title = source.readString();
		this.modifier = source.readInt();
		this.dice = source.readArrayList(getClass().getClassLoader());
		this.databaseRowID = -1;
	}

	/**
	 * Public Constructor
	 * @param rowID the row in the database that contains a reference to the die set
	 * @param title the title of the die set
	 * @param stringToParse the string collected from the database to be parsed into an array of dice
	 */
	public DieSet(long rowID, String title, String stringToParse) 
	{
		this.dice = new ArrayList<Die>();
		this.modifier = 0;
		this.title = title;
		this.databaseRowID = rowID;
		
		decodeDataBaseString(stringToParse);
	}


	/**
	 * @return the dice
	 */
	public ArrayList<Die> getDice() 
	{
		return dice;
	}


	/**
	 * @return the modifier
	 */
	public int getModifier() 
	{
		return modifier;
	}


	/**
	 * @return the title
	 */
	public String getTitle() 
	{
		return title;
	}


	/**
	 * @param dice the dice to set
	 */
	public void setDice(ArrayList<Die> dice)
	{
		this.dice = dice;
	}


	/**
	 * @param modifier the modifier to set
	 */
	public void setModifier(int modifier) 
	{
		this.modifier = modifier;
	}


	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) 
	{
		this.title = title;
	}
	
	/**
	 * @param databaseRowID the databaseRowID to set
	 */
	public void setDatabaseRowID(long databaseRowID)
	{
		this.databaseRowID = databaseRowID;
	}


	/**
	 * @return the databaseRowID
	 */
	public long getDatabaseRowID()
	{
		return databaseRowID;
	}

	/**
	 * Returns a string representation of the die
	 * @return a string representation of the die
	 */
	public String toString()
	{
		//if the die set has a title, that isn't white space, return it
		if(title.trim().length() >= 1)
			return this.title;
		
		//if not, use the array of dice to generate one
		else
		{
			//declare object to be returned
			String rtrn = "";
			
			//for every die
			for(Die i: dice)
			{
				//if the coefficient is more than one
				if(i.getCoefficient() > 1)

					//add it
					rtrn += i.getCoefficient()+"d"+i.getValue()+"+";
				
				//if not
				else
					//don't bother
					rtrn += "d"+i.getValue()+"+";
			}
						
			//the previous for loop leaves a plus on the end of the string
			//regardless of whether or not it is needed
			//so it is removed
			rtrn = rtrn.substring(0, rtrn.length()-1);
			
			//if the modifier is greater than 0
			if(modifier > 0)
				//add a + 
				rtrn += "+";
			
			//as long as the modifier isn't 0, add it
			if(modifier != 0)
				rtrn += this.modifier;

			
			return rtrn;
		}
	}

	
	//the creator used by the parsable interface to inflate a parcel
	public static final Parcelable.Creator<DieSet> CREATOR
			= new Parcelable.Creator<DieSet>()
				{
	
					/**
					 * Used to inflate a die object from a parcel
					 * @param source the parcel from which to construct the die
					 */
					public DieSet createFromParcel(Parcel source) 
					{
						return new DieSet(source);
					}
	
					public DieSet[] newArray(int size) 
					{
						return new DieSet[size];
					}
				
				};

	/**
	 * Parcel method (??)
	 */
	public int describeContents() 
	{
		return 0;
	}

	/**
	 * Collapses the die into a parcel object
	 * @param dest the parcel for the die to be placed in
	 */
	public void writeToParcel(Parcel dest, int flags) 
	{
		dest.writeString(this.title);
		dest.writeInt(this.modifier);
		dest.writeList(this.dice);
	}


	/**
	 * Takes the die set and creates a representative string to be placed in the database
	 * @return a string representation of the die set to be placed in the database
	 */
	public String toDataBaseString() 
	{
		//create value to be returned
		String rtrn = "";
		
		//create local instance of global variable
		ArrayList<Die> dice = this.dice;
		
		
		//add each die to the string
		for(Die i: dice)
		{
			rtrn += i.getCoefficient()+"d"+i.getValue()+"+";
		}
		
		//add the modifier
		rtrn += this.modifier;
		
		return rtrn;
	}
	
	/**
	 * Takes a string from the database and inflates it into a die set object
	 * @param string	The string from the database
	 */
	public void decodeDataBaseString(String string)
	{
		//initialize variables that will be used
		int locationOfPlus = -1;
		int locationOfD = -1;
		String workingString = "";
		String coefficient = "";
		String value = "";
		Die parsedDie = null;
			
		//while there is still a d in the string
		//because all dies are in the form xdy when
		//there are no more d's there are no more dice
		while(string.contains("d"))
		{
			//finds the location of the first +
			//the string is in the form xdy+xdy+xdy
			locationOfPlus = string.indexOf("+");
			
			//takes a piece of the string from the begining to the instance of the +
			workingString = string.substring(0, locationOfPlus);

			//finds the location of the d in the string
			//the d seperates the coefficient from the value
			locationOfD = workingString.indexOf("d");
			
			
			//puts the coefficient into a string
			coefficient = workingString.substring(0,locationOfD);
			
			//puts the value into a string
			value = workingString.substring(locationOfD+1, workingString.length());

			
			//creates a new die with the values parsed from the string
			parsedDie = new Die(Integer.parseInt(coefficient), Integer.parseInt(value));
			
			//adds it to the list of dice in the die set
			this.dice.add(parsedDie);
			
			
			//remove the first instance of the die we just parsed from the string
			string = string.replaceFirst(workingString, "");
			
			//remove the first + from the string (using a Regular Expression)
			string = string.replaceFirst("\\+", "");
		}

		//after everything has been removed, all that remains is the modifier
		this.modifier = Integer.parseInt(string);
	}
	
}
