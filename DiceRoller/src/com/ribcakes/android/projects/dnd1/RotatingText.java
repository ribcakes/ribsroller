package com.ribcakes.android.projects.dnd1;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

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


	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
	    if(focused)
	        super.onFocusChanged(focused, direction, previouslyFocusedRect);
	}

	@Override
	public void onWindowFocusChanged(boolean focused) {
	    if(focused)
	        super.onWindowFocusChanged(focused);
	}


	@Override
	public boolean isFocused() {
	    return true;
	}


}