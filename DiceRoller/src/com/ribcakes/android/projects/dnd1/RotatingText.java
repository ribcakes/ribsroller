package com.ribcakes.android.projects.dnd1;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
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
		
		 LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		 View view = layoutInflater.inflate(R.layout.example,this);
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