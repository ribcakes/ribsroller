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
	
	private ArrayList<Die> dice;
	private int modifier;
	private String title;
	private long databaseRowID;

	public DieSet()
	{
		dice = new ArrayList<Die>();
		modifier = 0;
		title = "";
		databaseRowID = -1;
	}
	
	
	/**
	 * @param title
	 * @param dice
	 * @param modifier
	 */
	public DieSet(String title, ArrayList<Die> dice, int modifier) 
	{
		this.dice = dice;
		this.modifier = modifier;
		this.title = title;
		this.databaseRowID = -1;
	}

	public DieSet(Die d)
	{
		this.dice = new ArrayList<Die>();
		dice.add(d);
		
		this.title = "";
		this.modifier = 0;
		this.databaseRowID = -1;
	}
	
	@SuppressWarnings("unchecked")
	public DieSet(Parcel source) 
	{
		this.title = source.readString();
		this.modifier = source.readInt();
		this.dice = source.readArrayList(getClass().getClassLoader());
		this.databaseRowID = -1;
	}


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


	public String toString()
	{
		if(title.length() >= 1)
			return this.title;
		else
		{
			String rtrn = "";
			
			for(Die i: dice)
			{
				if(i.getCoefficient() > 1)
					rtrn += i.getCoefficient()+"d"+i.getValue()+"+";
				else
					rtrn += "d"+i.getValue()+"+";
			}
						
			rtrn = rtrn.substring(0, rtrn.length()-1);

			if(modifier > 0)
				rtrn += "+";
			
			if(modifier != 0)
				rtrn += this.modifier;

			
			return rtrn;
		}
	}

	public static final Parcelable.Creator<DieSet> CREATOR
			= new Parcelable.Creator<DieSet>()
				{
	
					public DieSet createFromParcel(Parcel source) 
					{
						return new DieSet(source);
					}
	
					public DieSet[] newArray(int size) 
					{
						return new DieSet[size];
					}
				
				};

	public int describeContents() 
	{
		return 0;
	}


	public void writeToParcel(Parcel dest, int flags) 
	{
		dest.writeString(this.title);
		dest.writeInt(this.modifier);
		dest.writeList(this.dice);
	}


	public String toDataBaseString() 
	{
		String rtrn = "";
		ArrayList<Die> dice = this.dice;
		
		for(Die i: dice)
		{
			rtrn += i.getCoefficient()+"d"+i.getValue()+"+";
		}
		rtrn += this.modifier;
		
		return rtrn;
	}
	
	public void decodeDataBaseString(String string)
	{
		int locationOfPlus = -1;
		int locationOfD = -1;
		String workingString = "";
		String coefficient = "";
		String value = "";
		Die parsedDie = null;
				
		while(string.contains("d"))
		{
			locationOfPlus = string.indexOf("+");
			workingString = string.substring(0, locationOfPlus);
			locationOfD = workingString.indexOf("d");
			
			coefficient = workingString.substring(0,locationOfD);
			value = workingString.substring(locationOfD+1, workingString.length());

			parsedDie = new Die(Integer.parseInt(coefficient), Integer.parseInt(value));
			this.dice.add(parsedDie);
			
			string = string.replaceFirst(workingString, "");
			string = string.replaceFirst("\\+", "");
		}
		this.modifier = Integer.parseInt(string);
	}
	
}
