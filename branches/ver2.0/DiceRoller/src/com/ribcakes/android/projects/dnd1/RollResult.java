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
import java.util.Random;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class is used to hold a result after a 
 * die has been rolled.
 */
public class RollResult implements Parcelable
{
	//the random number generator used by the class 
	//to generate rolls
	public static final Random gen = new Random();
	
	
	//the dieSet that was rolled to produce this result
	private DieSet rolled;
	
	//a list of all the results of each die
	private ArrayList<Integer> results;
	
	//the total result of the roll
	private int result;
	
	/**
	 * Public Constructor - Default constructor
	 * @param rolled	the dieSet to be rolled for this result
	 */
	public RollResult(DieSet rolled)
	{
		this.rolled = rolled;
		this.results = new ArrayList<Integer>();
		this.result = 0;
		
		rollDice();
	}
	
	/**
	 * Public Constructor
	 * @param source	the parcel from which to inflate the rollResult
	 */
	@SuppressWarnings("unchecked")
	public RollResult(Parcel source)
	{
		this.rolled = source.readParcelable(getClass().getClassLoader());
		this.results = source.readArrayList(null);
		this.result = source.readInt();
	}

	/**
	 * Takes the dieSet stored in the rollResult object and rolls 
	 * its constituent dice
	 */
	public void rollDice()
	{
		//initialize variables
		int temp = 0;
		int result = 0;
		ArrayList<Integer> results = new ArrayList<Integer>();
		
		
		//for each die in the die set
		for(Die i: rolled.getDice())
		{
			//for each iteration under the constraint of the coefficient
			for(int j = 0; j < i.getCoefficient(); j++)
			{
				//get the value of the die and generate a random number 
				//between 1 and its value
				temp = gen.nextInt(i.getValue()) + 1;
				
				
				//add the randomly generated result to the result
				result += temp;
				
				//and to the array
				results.add(temp);
			}
		}
		
		//after all the dice have been iterated through, add the modifier
		result += rolled.getModifier();
		
		//store the local variables into the instance variables
		this.result = result;
		this.results = results;
	}

	/**
	 * @return the rolled
	 */
	public DieSet getRolled()
	{
		return rolled;
	}

	/**
	 * @return the results
	 */
	public ArrayList<Integer> getResults() 
	{
		return results;
	}

	/**
	 * @return the result
	 */
	public int getResult() 
	{
		return result;
	}
	
	/**
	 * Retruns a string representation of the object
	 * @return a string representation of a rollResult
	 */
	public String toString()
	{
		return result+"";
	}
	
	/**
	 * Parcel method (??)
	 */
	public int describeContents() 
	{
		return 0;
	}

	/**
	 * Collapses the roll result into a parcel object
	 * @param dest the parcel for the roll result to be placed in
	 */
	public void writeToParcel(Parcel dest, int flags) 
	{
		dest.writeParcelable(this.rolled, flags);
		dest.writeList(this.results);
		dest.writeInt(result);
	}
	
	//the creator used by the parsable interface to inflate a parcel	
	public static final Parcelable.Creator<RollResult> CREATOR
	= new Parcelable.Creator<RollResult>()
	{
		/**
		 * Used to inflate a die object from a parcel
		 * @param source the parcel from which to construct the die
		 */
		public RollResult createFromParcel(Parcel source) 
		{
			return new RollResult(source);
		}

		public RollResult[] newArray(int size) 
		{
			return new RollResult[size];
		}
		
	};

}
