package com.ribcakes.android.projects.dnd1;

import java.util.ArrayList;
import java.util.Random;

public class RollResult 
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
	
}
