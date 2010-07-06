package com.ribcakes.android.projects.dnd1;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class FocusedResult extends LinearLayout
{

	public FocusedResult(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		   LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		   View view = layoutInflater.inflate(R.layout.focused_result,this);
	}

}
