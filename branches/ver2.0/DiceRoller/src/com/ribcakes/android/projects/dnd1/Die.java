package com.ribcakes.android.projects.dnd1;

public class Die implements Comparable<Die> 
{

	private int mValue;
	private int mCoefficient;
	
	
	public Die() 
	{
		this.mValue = 0;
		this.mCoefficient = 0;
	}
	
	
	public Die(int mCoefficient, int mValue) 
	{
		this.mValue = mValue;
		this.mCoefficient = mCoefficient;
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

	public int compareTo(Die another) 
	{
		if(this.mValue == another.getValue())
			return 0;
		else if(this.mValue > another.getValue())
			return 1;
		else
			return -1;
	}
	
	public String toString()
	{
		return this.mCoefficient+"d"+this.mValue;
	}
}
