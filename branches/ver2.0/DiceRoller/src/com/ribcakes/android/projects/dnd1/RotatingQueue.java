package com.ribcakes.android.projects.dnd1;

import android.widget.TextView;

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


public class RotatingQueue
{

	private String[] queue;
	private int head;
	private int tail;
	private int length;
	private int size;
	
	private String seperator;
	
	public RotatingQueue(int size)
	{
		this.size = size;
		this.queue = new String[this.size];
		this.head = 0;
		this.tail = 0;
		this.length = 0;
		seperator = "";
	}
	
	public RotatingQueue(int size, String[] log, int head, int tail)
	{
		this.size = size;
		this.queue = log.clone();
		this.head = head;
		this.tail = tail;
		this.length = 0;
		seperator = "";
	}
	
	public int getHead() 
	{
		return head;
	}

	/**
	 * @param seperator the seperator to set
	 */
	public void setSeperator(String seperator) 
	{
		this.seperator = seperator;
	}

	public int getTail() 
	{
		return tail;
	}

	public boolean add(String o) 
	{
		queue[tail] = o;
		
		tail ++;
		tail %= size;
		
		if(length == size)
		{
			head ++;
			head %= size;
		}
		else
			length++;
				
		return true;
	}

	public String delete() 
	{
        if(length == 0)
            return null;
        else
        {
            String helperString = queue[head];
        
            head ++;      
            head %= size;
            
            length --;

            return helperString;
        
        }
	}

	public boolean isEmpty() 
	{
		return length == 0;
	}

	public int size() 
	{
		return length;
	}

	public void updateView(TextView[] trackers)
	{
        String seperator = this.seperator;
		
		int tail = this.tail - 1;
		tail += size;
		tail %= size;
		
		int counter = 1;
		
		for(TextView i : trackers)
			i.setText("");
			
		for(int i = 0; i < 2; i++)
		{			
			
			for(int j = 0; j < 3; j++)
			{
								
				if(queue[tail] == null)
					break;
				
				trackers[i].append("("+counter +")"+queue[tail]+"\n");
				
				trackers[i].append(seperator);
				
				tail --;
				tail += this.size;
				tail %= this.size;
				
				counter ++;
			}
		}
	}

	public String[] getLog()
	{
		return queue;
	}
	
//	public void printContents()
//	{
//		for(String i: queue)
//		{
//			Log.i("RotatingQueue:printContents()", "value: "+i);
//		}
//	}

}
