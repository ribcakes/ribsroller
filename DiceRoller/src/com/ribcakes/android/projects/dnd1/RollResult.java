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
import java.util.Random;

import android.os.Parcel;
import android.os.Parcelable;


public class RollResult implements Parcelable
{
	public static final Random gen = new Random();
	
	
	private DieSet rolled;
	private ArrayList<Integer> results;
	private int result;
	
	public RollResult(DieSet rolled)
	{
		this.rolled = rolled;
		this.results = new ArrayList<Integer>();
		this.result = 0;
		
		rollDice();
	}
	
	@SuppressWarnings("unchecked")
	public RollResult(Parcel source)
	{
		this.rolled = source.readParcelable(getClass().getClassLoader());
		this.results = source.readArrayList(null);
		this.result = source.readInt();
	}

	public void rollDice()
	{
		int temp = 0;
		int result = 0;
		ArrayList<Integer> results = new ArrayList<Integer>();
		
		for(Die i: rolled.getDice())
		{
			for(int j = 0; j < i.getCoefficient(); j++)
			{
				temp = gen.nextInt(i.getValue()) + 1;
				result += temp;
				results.add(temp);
			}
		}
		
		result += rolled.getModifier();
		
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
	
	public String toString()
	{
		return result+"";
	}

	public int describeContents() 
	{
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) 
	{
		dest.writeParcelable(this.rolled, flags);
		dest.writeList(this.results);
		dest.writeInt(result);
	}
	
	public static final Parcelable.Creator<RollResult> CREATOR
	= new Parcelable.Creator<RollResult>()
	{

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
