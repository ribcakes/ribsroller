package com.ribcakes.android.projects.dnd1;

import java.util.ArrayList;

public class DieSet 
{
	
	private ArrayList<Die> dice;
	private int modifier;
	private String title;

	public DieSet()
	{
		dice = new ArrayList<Die>();
		modifier = 0;
		title = "";
	}
	
	
	/**
	 * @param dice
	 * @param modifier
	 * @param title
	 */
	public DieSet(ArrayList<Die> dice, int modifier, String title) 
	{
		this.dice = dice;
		this.modifier = modifier;
		this.title = title;
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
	
	public String toString()
	{
		if(title.length() >= 1)
			return this.title;
		else
		{
			String rtrn = "";
			
			for(Die i: dice)
			{
				rtrn += i.getCoefficient()+"d"+i.getValue()+"+";
			}

			if(rtrn.length() >= 1)
				rtrn += this.modifier;
			else
				rtrn += "d"+this.modifier;
			
			return rtrn;
		}
	}
	
}
