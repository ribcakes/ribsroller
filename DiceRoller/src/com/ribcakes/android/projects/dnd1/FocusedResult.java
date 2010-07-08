package com.ribcakes.android.projects.dnd1;

import java.util.ArrayList;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FocusedResult extends LinearLayout
{

	private GridView grid;
	private ResultAdapter adapter;
	private Context context;
	
	public FocusedResult(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		this.context = context;
		
		LayoutInflater layoutInflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(R.layout.focused_result,this);
		
		adapter = new ResultAdapter();
		grid = (GridView)findViewById(R.id.focused_result_grid);
		grid.setAdapter(adapter);
	}

	public void setFocused(RollResult result) 
	{
		grid.setVisibility(VISIBLE);
		
		adapter.empty();
		
		((TextView)findViewById(R.id.focused_result_title)).setText(result.getRolled().toString());
		((TextView)findViewById(R.id.focused_result_result)).setText(result.getResult()+"");
		
		adapter.fillGrid(result);
		
		grid.setBackgroundResource(R.color.grid_background);
		grid.invalidateViews();
	}
	
	public void clearFocused()
	{
		adapter.empty();
		grid.setBackgroundResource(R.drawable.transparent00000000);
		grid.invalidateViews();
		((TextView)findViewById(R.id.focused_result_result)).setText("");
		grid.setVisibility(GONE);
		
	}
	
	
	public class ResultAdapter extends BaseAdapter 
	{
	    private ArrayList<String> results;
	    
	    public ResultAdapter()
	    {
	        results = new ArrayList<String>();
	    }

		public void empty()
		{
			results.clear();
		}
	    
	    public void add(String d)
	    {
	    	results.add(d);
	    }
	    
	    
	    public void fillGrid(RollResult result) 
		{
			ArrayList<Integer> results = result.getResults();
			ArrayList<Die> dice = result.getRolled().getDice();
			String complexResult = "";
			Die currentDie;
			int k = 0;
			
			for(int i = 0; i < dice.size(); i++)
			{
				
				currentDie = dice.get(i);
				
				for(int j = 0; j < currentDie.getCoefficient(); j++)
				{
					complexResult = "";

					complexResult += ("<b>" + results.get(k) + "</b>" );
					complexResult += "/("+currentDie.getValue()+")";
					k++;
					
					add(complexResult);
				}
			}		
			
			int modifier = result.getRolled().getModifier();
			if(modifier != 0)
			{
				if(modifier > 0)
					add("+"+modifier);
				else
					add(modifier+"");
			}
		}


		public int getCount() 
		{
	        return results.size();
	    }

	    public String getItem(int position) 
	    {
	        return results.get(position);
	    }
	    
	    public long getItemId(int position) 
	    {
	        return 0;
	    }

	    // create a new ImageView for each item referenced by the Adapter
	    public View getView(final int position, View convertView, ViewGroup parent)
	    {
	        View resultView;
	        if (convertView == null) 
	        {  // if it's not recycled, initialize some attributes

	            LayoutInflater inflator = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            resultView = inflator.inflate(R.layout.focused_result_item, null);
             
	        }
	        else 
	        {
	            resultView = (TextView) convertView;
	        }	     
	        
	        ((TextView)resultView.findViewById(R.id.focused_result_item)).setText(Html.fromHtml(results.get(position)));	        
	        return resultView;
	    }
	}

}
