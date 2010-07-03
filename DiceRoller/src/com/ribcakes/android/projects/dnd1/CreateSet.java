package com.ribcakes.android.projects.dnd1;


import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

public class CreateSet extends Activity 
{
	
	private static final int NEW_DIE = 0;
	private static final int EDIT_DIE = 1;
	
	private GridView content;
	private DieAdapter adapter;
	private Die currentEdit;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_die_set);
		
		content = (GridView)findViewById(R.id.create_die_set_content);
		adapter = new DieAdapter(this);				
		content.setAdapter(adapter);
		
		currentEdit = new Die();
		
        content.setOnItemClickListener(
        		new OnItemClickListener() 
        		{
        			public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
        			{
        				adapter.remove(position);
        				content.invalidateViews();
        			}
        		});
        
        content.setOnItemLongClickListener(
        		new OnItemLongClickListener() 
        		{

					public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id)
					{
						currentEdit = (Die) adapter.getItem(position);
						showDialog(EDIT_DIE);						
						return true;
					}
        			
				});

	}
	
	public void onClick(View v)
	{
		switch(v.getId())
		{
			case R.id.new_die:
				showDialog(NEW_DIE);
				break;
			
		}
		content.invalidateViews();
	}
	
	
	

	@Override
	protected Dialog onCreateDialog(int id) 
	{
		LayoutInflater factory = LayoutInflater.from(this);
	    final View dieDialogLayout = factory.inflate(R.layout.die_dialog, null);
	    AlertDialog dieDialog = new AlertDialog.Builder(this).create();
	    
	    switch(id)
	    {
	    	case NEW_DIE:
			    dieDialog = new AlertDialog.Builder(this)
			    	.setTitle(R.string.new_die_dialog_title)
			        .setView(dieDialogLayout)
			        .setPositiveButton("Ok", 
			        		new DialogInterface.OnClickListener() 
			                {
			                    public void onClick(DialogInterface dialog, int whichButton) 
			                    {
			                    	try
			                    	{
			                    		adapter.add(new Die(Integer.parseInt(((EditText)((Dialog)dialog).findViewById(R.id.coefficient)).getText().toString()),
		                    								Integer.parseInt(((EditText)((Dialog)dialog).findViewById(R.id.value)).getText().toString())));
			                    		content.invalidateViews();
			                    	}
			                    	catch (NumberFormatException e)
			                    	{
			                    		Log.e("Rib's Roller:CreateSet:onCreateDialog():onClick()", "NumberFormatException: "+e);
			                    	}
			                    }
			                })
			        .setNegativeButton("Cancel", 
			        		new DialogInterface.OnClickListener() 
			        		{
			                    public void onClick(DialogInterface dialog, int whichButton) 
			                    {
			                    	dialog.dismiss();
			                    }
			                })
			        .create();
		    	break;
		    	
	    	case EDIT_DIE:
			    dieDialog = new AlertDialog.Builder(this)
		    	.setTitle(R.string.edit_die_dialog_title)
		        .setView(dieDialogLayout)
		        .setPositiveButton("Ok", 
		        		new DialogInterface.OnClickListener() 
		                {
		                    public void onClick(DialogInterface dialog, int whichButton) 
		                    {
		                    	try
		                    	{
		                    		currentEdit.setCoefficient(Integer.parseInt(((EditText)((Dialog)dialog).findViewById(R.id.coefficient)).getText().toString()));
		                    		currentEdit.setValue(Integer.parseInt(((EditText)((Dialog)dialog).findViewById(R.id.value)).getText().toString()));
		                    		content.invalidateViews();
		                    	}
		                    	catch (NumberFormatException e)
		                    	{
		                    		Log.e("Rib's Roller:CreateSet:onCreateDialog():onClick()", "NumberFormatException: "+e);
		                    	}
		                    }
		                })
		        .setNegativeButton("Cancel", 
		        		new DialogInterface.OnClickListener() 
		        		{
		                    public void onClick(DialogInterface dialog, int whichButton) 
		                    {
		                    	dialog.dismiss();
		                    }
		                })
		        .create();
	    		break;
	    }
		
		return dieDialog;
	}
	
	



	protected void onPrepareDialog(int id, Dialog dialog)
	{
		switch(id)
		{
			case NEW_DIE:	    	    
		    	((EditText)dialog.findViewById(R.id.coefficient)).setText(1+"");
		    	((EditText)dialog.findViewById(R.id.value)).setText(6+"");
		    	break;
		    	
			case EDIT_DIE:
		    	((EditText)dialog.findViewById(R.id.coefficient)).setText(currentEdit.getCoefficient()+"");
		    	((EditText)dialog.findViewById(R.id.value)).setText(currentEdit.getValue()+"");
				break;
		}
		
		super.onPrepareDialog(id, dialog);
	}





	public class DieAdapter extends BaseAdapter 
	{
	    private Context mContext;
	    private ArrayList<Die> dice;
	    
	    public DieAdapter(Context c)
	    {
	        mContext = c;
	        dice = new ArrayList<Die>();
	    }

	    public void remove(int position) 
	    {
	    	dice.remove(position);
			Log.i("CreateSet:remove()", "position: "+position);
		}
	    
	    public void add(Die d)
	    {
	    	dice.add(d);
			Log.i("CreateSet:add()", "new die: "+d);
	    }

		public int getCount() 
		{
	        return dice.size();
	    }

	    public Object getItem(int position) 
	    {
	        return dice.get(position);
	    }

	    public long getItemId(int position) 
	    {
	        return 0;
	    }

	    // create a new ImageView for each item referenced by the Adapter
	    public View getView(final int position, View convertView, ViewGroup parent)
	    {
	        TextView dieView;
	        if (convertView == null) 
	        {  // if it's not recycled, initialize some attributes
	            dieView = new TextView(mContext);
	            dieView.setLayoutParams(new GridView.LayoutParams(90, 60));
	            dieView.setBackgroundResource(R.drawable.button_background);
	            dieView.setTextSize(18);
	            dieView.setTextColor(Color.BLACK);
	            dieView.setGravity(Gravity.CENTER);
	                        
	        }
	        else 
	        {
	            dieView = (TextView) convertView;
	        }

	        dieView.setText(dice.get(position).toString());
	        return dieView;
	    }
	}
	
}
