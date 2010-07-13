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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class is used to represent a die
 */
public class Die implements Parcelable
{
	//the value of the die
	private int mValue;
	
	//how many times to roll this die
	private int mCoefficient;
	
	/**
	 * Public Constructor - Default Constructor
	 */
	public Die() 
	{
		this.mValue = 0;
		this.mCoefficient = 0;
	}
	
	/**
	 * Public Constructor
	 * @param mCoefficient	the coefficient for the die
	 * @param mValue	the value of the die
	 */
	public Die(int mCoefficient, int mValue) 
	{
		this.mValue = mValue;
		this.mCoefficient = mCoefficient;
	}

	/**
	 * Public Constructor
	 * @param source	the parcel from which to construct a die
	 */
	public Die(Parcel source) 
	{
		this.mCoefficient = source.readInt();
		this.mValue = source.readInt();
	}


	/**
	 * @return the mValue
	 */
	public int getValue() 
	{
		return mValue;
	}

	/**
	 * @return the mCoefficient
	 */
	public int getCoefficient() 
	{
		return mCoefficient;
	}

	/**
	 * @param mValue the mValue to set
	 */
	public void setValue(int mValue) 
	{
		this.mValue = mValue;
	}

	/**
	 * @param mCoefficient the mCoefficient to set
	 */
	public void setCoefficient(int mCoefficient) 
	{
		this.mCoefficient = mCoefficient;
	}

	/**
	 * Returns a string representation of the die
	 * @return a string representation of the die
	 */
	public String toString()
	{
		return this.mCoefficient+"d"+this.mValue;
	}

	//the creator used by the parsable interface to inflate a parcel
	public static final Parcelable.Creator<Die> CREATOR 
			= new Parcelable.Creator<Die>() 
				{
					/**
					 * Used to inflate a die object from a parcel
					 * @param source the parcel from which to construct the die
					 */
					public Die createFromParcel(Parcel source) 
					{
						return new Die(source);
					}
			
					public Die[] newArray(int size) 
					{
						return new Die[size];
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
		dest.writeInt(mCoefficient);
		dest.writeInt(mValue);
	}

}
