package com.ribcakes.android.projects.dnd1;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

public class CreateSet extends Activity 
{

	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_die_set);
		
		LinearLayout content = (LinearLayout)findViewById(R.id.create_die_set_content);
		content.setOrientation(LinearLayout.VERTICAL);
		
		Button a = new Button(content.getContext());
		a.setBackgroundResource(R.drawable.button_background);
		
		content.addView(a, 70, 50);		
		a = new Button(content.getContext());
		a.setBackgroundResource(R.drawable.button_background);		
		
		content.addView(a, 70, 50);		
		a = new Button(content.getContext());
		a.setBackgroundResource(R.drawable.button_background);
		
		content.addView(a, 70, 50);		
		a = new Button(content.getContext());
		a.setBackgroundResource(R.drawable.button_background);
		
		content.addView(a, 70, 50);		
		a = new Button(content.getContext());
		a.setBackgroundResource(R.drawable.button_background);

		//content.addView(a, 4);
		
		content.addView(a, 70, 50);		
		a = new Button(content.getContext());
		a.setBackgroundResource(R.drawable.button_background);

		content.addView(a, 70, 50);		
		a = new Button(content.getContext());
		a.setBackgroundResource(R.drawable.button_background);
		
		content.addView(a, 70, 50);		
		a = new Button(content.getContext());
		a.setBackgroundResource(R.drawable.button_background);
		
		content.addView(a, 70, 50);		
		a = new Button(content.getContext());
		a.setBackgroundResource(R.drawable.button_background);
		
		content.addView(a, 70, 50);		
		a = new Button(content.getContext());
		a.setBackgroundResource(R.drawable.button_background);
		

	}

}
