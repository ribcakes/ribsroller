package com.ribcakes.android.projects.dnd1;

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
