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

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Class used to display Text Fields.  
 * The view automatically scrolls a line
 * of text if it is too long for the view, 
 * and keeps it perpetually scrolling
 */
public class RotatingText extends TextView 
{

	public RotatingText(Context context) 
	{
		super(context);
	}
	
	
	public RotatingText(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}


	public RotatingText(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
	}


	//methods prevent the object from losing focus
	
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect)
	{
	    if(focused)
	        super.onFocusChanged(focused, direction, previouslyFocusedRect);
	}

	@Override
	public void onWindowFocusChanged(boolean focused) 
	{
	    if(focused)
	        super.onWindowFocusChanged(focused);
	}


	@Override
	public boolean isFocused() 
	{
	    return true;
	}


}