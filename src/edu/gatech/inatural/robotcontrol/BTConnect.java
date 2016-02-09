package edu.gatech.inatural.robotcontrol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import edu.gatech.inatural.robotcontrol.CheckMacAddress;

public class BTConnect extends Activity {
	
	///////////MEMBER VARIABLES/CONSTANTS////////////
	
	//for SharedPreferences
	public static final String robotControlPrefName = "edu.gatech.inatural.RobotControl";
	public static final String addressConstant = "edu.gatech.inatural.RobotControl.MACAddress";
	public static final String androidName = "edu.gatech.inatural.RobotControl.androidName";
	public static final String androidAddress = "edu.gatech.inatural.RobotControl.androidAddress";
	
	//internal storage variables
	public static final String robotControlStorageName = "edu.gatech.inatural.RobotControl";
	
	//For previous connections data
	private ArrayList<BTListItem> macArray = new ArrayList<BTListItem>();
	private ListView lv;
    private MyListAdapter adapter;
    
    //for the Bluetooth Adapter
    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver mReceiver;

    //For touch events
    int initx, finalx, item; 
    static boolean lock=false;
    int minDragLength = 12;
    
      
    /////////ON ACTION EVENTS///////////////
    
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btconnect);
            
		//////////Do Bluetooth Stuff
        initBluetooth();

        
        ////////Do Mac List Stuff
        
        macArray = new ArrayList<BTListItem>();
        for(int i=0 ; i<30 ; i++){
        	macArray.add(new BTListItem("Data Point "+(i+1),"X"));
        }
        
        
        //attach an array adapter to the listview
        adapter = new MyListAdapter(this , R.layout.bt_list_item , macArray);
        lv = (ListView)findViewById(R.id.bt_listview);
        lv.setAdapter(adapter);
	    
       
        lv.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_UP){
					restorePref();
				}
				return false;
			}
        });
       
       
        
        initList(); //initialize the arraylist for the array listview
        
        //update the text view with the current Mac Address
        restorePref();
		
	}
	

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_btconnect, menu);
        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    
    //Stops the bluetooth discovery when the application is stopped
	@Override
	protected void onStop() {
		super.onStop();
		if(mBluetoothAdapter.isDiscovering()){
			mBluetoothAdapter.cancelDiscovery();
		}
		
		
		String string="";
		if(macArray.size()!=0){
			string = macArray.get(0).data;
			for(int i=1 ; i<macArray.size() ; i++){
				string = string + ";" + macArray.get(i).data;
			}
		
			try{
				FileOutputStream fos = openFileOutput(robotControlStorageName, Context.MODE_PRIVATE);
				fos.write(string.getBytes());
				fos.close();
			} catch (Exception e) {
				//nothing
			}
		} else { //delete the file
			this.deleteFile(robotControlStorageName);
		}
		
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		
		unregisterReceiver(mReceiver);
	}

	//////////////INITIALIZATION METHODS/////////////////
	
	//initialize the bluetooth stuff
	private void initBluetooth(){
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
	        // Device does support Bluetooth
	        
	        
        	//if the bluetooth adapter is not enabled, enable it
	        if (!mBluetoothAdapter.isEnabled()) {
	            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	        }
	        
	        
	        // Create a BroadcastReceiver for ACTION_FOUND
	    	mReceiver = new BroadcastReceiver() {
	    	    public void onReceive(Context context, Intent intent) {
	    	        String action = intent.getAction();
	    	        // When discovery finds a device
	    	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	    	            // Get the BluetoothDevice object from the Intent
	    	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	    	            // collect the name and address of android device
	    	            String name = device.getName();
	    	            String address = device.getAddress();
	    	            //open shared preferences to save android name and address
	    	            SharedPreferences settings = getSharedPreferences(robotControlPrefName, 0);
	    	            SharedPreferences.Editor editor = settings.edit();
	    	            editor.putString(androidName, name);		//Write Android name to preferences
	    	            editor.putString(androidAddress, address);	//Write Android Mac Address to preferences
	    	            editor.commit(); 							//commit changes
	    	        }
	    	    }
	    	};
	    	
	    	// Register the BroadcastReceiver
	    	IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	    	registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
	    	
	    	//if it's already discoverable or not
	    	/* !!!!!!!This is breaking the runtime app!!!!!!!
	    	if (!mBluetoothAdapter.isDiscovering()){
	    		mBluetoothAdapter.startDiscovery();
	    	} 
	    	*/
	    	
	        
        } else {
        	//The device does not support bluetooth, let's inform the user
        	
        	//create a message
        	AlertDialog.Builder noBluetooth = new AlertDialog.Builder(this);
        	
        	//set up the message and create a clickable button
        	noBluetooth.setMessage("Your device does not support bluetooth.");
        	noBluetooth.setCancelable(true);
        	
        	noBluetooth.setPositiveButton("Okay", new DialogInterface.OnClickListener(){
        		public void onClick(DialogInterface dialog, int which){
        			BTConnect.this.finish();
        		}
        	});
        	
        	//show the message
        	AlertDialog alert = noBluetooth.create();
        	alert.show();
        	
        }
	}
	
	
	
	//updates the previous MAC Address List 
	private void initList(){
		String data = ""; 
		
		macArray.clear(); //clear the array before reinitializing
		
		//tries to read from internal storage and place in string
		File file = this.getFileStreamPath(robotControlStorageName);
		if(file.exists()){
			FileInputStream in;
			InputStreamReader inputStreamReader;
			BufferedReader bufferedReader;
			try {
				in = openFileInput(robotControlStorageName);
				inputStreamReader = new InputStreamReader(in);
			    bufferedReader = new BufferedReader(inputStreamReader);
			    try {
					data = bufferedReader.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
			String delims = "[;]+";					//using colon as the delimiter for the string parse
			String[] parts = data.split(delims);	//parses string into a String array
			
			//add mac addresses to the array
			for(int i=0 ; i<parts.length ; i++){
				if(parts[i] != null){
					macArray.add(new BTListItem(parts[i],"X"));
				}
			}
		} //ends if(file.exists)
		
		////TEST DATA IF There were no saved addresses///
		if(macArray.size() == 0){
			Toast.makeText(this, "Add Random Addresses", Toast.LENGTH_SHORT).show();
			macArray.add(new BTListItem("00:07:80:91:30:AE","X"));
			for(int i=0 ; i<2 ; i++){
				macArray.add(new BTListItem(randomMac(),"X"));
			}
		}
		
		//checkForArrayRepeats();
		adapter.notifyDataSetChanged();
	}
	
	//////////////METHODS//////////////
	
	//Restore preferences and write mac address to be viewed on BTConnect screen
	private void restorePref(){
		//Restoring Preferences
        SharedPreferences settings = getSharedPreferences(robotControlPrefName, 0);
        String macAddress = settings.getString(addressConstant, "No MAC Address");
        macAddress = "Current Address: " + macAddress;
		
        //Write string to the text view 
        TextView textView = new TextView(this);
        textView=(TextView)findViewById(R.id.textView1); //finds the currently connected text view
        textView.setText(macAddress);
        
	}
	
	//allows calling method through an xml button
	public void restorePrefButton(View view){
		restorePref();
	}
	
	//Change the Mac address in the preferences
	private void changeMAC(String macAddress){
		//Restoring Preferences
        SharedPreferences settings = getSharedPreferences(robotControlPrefName, 0);
        String prev = settings.getString(addressConstant, "No MAC Address");
        
        //if the string is an actual mac address then write to shared preferences
        if(CheckMacAddress.checkMac(macAddress)){
        	//save previous mac address to top of array
        	if(prev != "No MAC Address"){
        		macArray.add(0,new BTListItem(prev,"X"));
        		//checkForArrayRepeats();
        		adapter.notifyDataSetChanged();
        	}
        	
        	//Writing Mac Address to preferences
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(addressConstant, macAddress);
            editor.commit();
            
        }
        
        //call restorePref() to update the text view.
        restorePref();
	}
	

	public void changeMACButton(View view){
		//Get Mac Address from EditText Field
        EditText editText = (EditText) findViewById(R.id.bt_editMAC);
        String macAddress = editText.getText().toString();
        
        //Clear EditText
        editText.setText("");

        changeMAC(macAddress);
	}
	
	private void checkForArrayRepeats(){
		int size = macArray.size();
		String current;
		
		for(int i=0 ; i<size; i++){
			current = macArray.get(i).data;
			for(int j=i ; j<size ; j++){
				if(current == macArray.get(j).data){
					macArray.remove(j);
					i--;
					size = macArray.size();
				}
			}
		}
		
		adapter.notifyDataSetChanged();
	}
	
	//////////////ON PRESS EVENTS//////////////
	
	//will refresh the textview at the top to display current mac address
	public void button_refresh(View v){
		restorePref();
	}
	
	//will create random mac address and add to the list
	public void button_random(View v){
		macArray.add(0,new BTListItem(randomMac(),"X"));
		adapter.notifyDataSetChanged();
	}
	
	//////////////TESTING STUFF////////////////
	private String randomMac(){
		String MAC = "";
		String nextChar = "";
		Random rg = new Random();
		int nextnumber;
		
		for(int i=0 ; i<6 ; i++){
			nextChar = "";
			for(int j=0 ; j<2 ; j++){
				nextnumber = rg.nextInt(16);
				
				nextChar = nextChar + Integer.toHexString(nextnumber);
			}
			
			if(i==0){ //if first set of numbers
				MAC = nextChar;
			} else {
				MAC = MAC + ":" + nextChar;
			}
		}
		return MAC;
	}
}
