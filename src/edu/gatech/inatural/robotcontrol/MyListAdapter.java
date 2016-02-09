package edu.gatech.inatural.robotcontrol;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyListAdapter extends ArrayAdapter<BTListItem> { //change this to the actual list item class
	
	////// Member variables
	//Adapter variables
	Context context;
	int layoutResourceId;
	ArrayList<BTListItem> data;  //change this to the proper class
	//Touch variables
	View.OnTouchListener touchy;
	
	
	////// Constructor
	public MyListAdapter(Context context, int layoutResourceId, ArrayList<BTListItem> data){
		
		super(context, layoutResourceId, data);
		
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
	}
	
	private void removeItem(int position){
		data.remove(position);
		notifyDataSetChanged();
	}
	
	////// Overridden Member Methods
	@Override
	public View getView(final int position, View convertView, ViewGroup parent){
		View row = convertView;
		ListHolder holder = null;
		
		//inflate view and place view ids into holder
		if(row == null){
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId , parent , false);
			
			holder = new ListHolder();									//change these to what's down below
			holder.dataText = (TextView)row.findViewById(R.id.bt_macdata); //change these to what's down below
			holder.cancelText = (TextView)row.findViewById(R.id.bt_canceltext);//change these to what's down below
			holder.listPosition = (TextView)row.findViewById(R.id.bt_listposition); //change these to what's down below
			
			row.setTag(holder);
		} else {
			holder = (ListHolder)row.getTag();	
		}
		
		//Set the text to the views
		BTListItem listclass = data.get(position);
		holder.dataText.setText(listclass.data);
		holder.cancelText.setText(listclass.cancelText);
		holder.listPosition.setText(""+(position+1));
		
		TextView testblah = (TextView)row.findViewById(R.id.bt_canceltext);
		//touch Stuff
		
		setListeners(holder, position, parent);
		return row;
	}

	private void setListeners(ListHolder holder, final int position, ViewGroup parent){

		//When The X is pressed delete the data
		holder.cancelText.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				data.remove(position);
				notifyDataSetChanged();
			}
		});
		
		//When Mac  address is changed add to sharedPref
		holder.dataText.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				
				String macAddress = data.get(position).data;
				data.remove(position);
				
				//Restoring Preferences
				Context cont = context;
		        SharedPreferences settings = cont.getSharedPreferences(BTConnect.robotControlPrefName, 0);
		        String prev = settings.getString(BTConnect.addressConstant, "No MAC Address");
		        
		      //if the string is an actual mac address then write to shared preferences
		        if(CheckMacAddress.checkMac(macAddress)){
		        	//save previous mac address to top of array
		        	if(prev != "No MAC Address"){
		        		data.add(0,new BTListItem(prev,"X"));
		        		//checkForArrayRepeats();
		        		notifyDataSetChanged();
		        	}
		        }
		        
		        //Writing Mac Address to preferences
	            SharedPreferences.Editor editor = settings.edit();
	            editor.putString(BTConnect.addressConstant, macAddress);
	            editor.commit();
	            
			}
		});
	}
	
	// Other Class Stuff //
	static class ListHolder { //change these to what is needed
		TextView dataText; 
		TextView cancelText; 
		TextView listPosition;
		
		LinearLayout dataText_ll;
		LinearLayout cancelText_ll;
	}

}
