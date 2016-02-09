package edu.gatech.inatural.robotcontrol;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
//import android.widget.SeekBar;
import android.widget.TextView;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

public class Controller extends Activity implements SensorEventListener {
	private SensorManager sensorManager;
	
	// Hardcoded Arduino address (future plans: create another activity 
	// that scans for bluetooth addresses and sets this address)
	private static final String DEVICE_ADDRESS = "00:07:80:91:30:AE";
	
	private ArduinoReceiver arduinoReceiver = new ArduinoReceiver();
	
	// controlMode 1 = buttons, 2 = accelerometer
	int controlMode = 2;
	int progressL =0;
	int progressR=0;
	boolean lock = false;
	String controlModeS = "accelerometer", lockS = "unlocked";
	
	private static final float accelSensitivity = (float) 0.5;
	
	// default accelerometer coordinates
	private float defaultX;
	private float defaultY;
	//private float defaultZ;
	
	// current discrete coordinates in incrememts of accelSensitivity
	private float currentX;
	private float currentY;
	//private float currentZ;
	
	// realtime data from accelerometer
	private float accelX;
	private float accelY;
	//private float accelZ;
	
	// calibration variable will reset default accel variables to current
	private boolean calibrate = true;

	// accel data conversion to speed commands depends on the orientation
	private enum Orientation {PORTRAIT, LANDSCAPE1, LANDSCAPE2};
	private Orientation orientation = Orientation.PORTRAIT;
	
	// used for debugging
	TextView xCoor;
	TextView yCoor;
	TextView zCoor;
	
	int sending = 0;

	@Override
	public void onCreate(Bundle savedInstanceState){

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_controller);
		
		// used for debugging
		xCoor = (TextView) findViewById(R.id.xcoor);
		yCoor = (TextView) findViewById(R.id.ycoor);
		zCoor = (TextView) findViewById(R.id.zcoor);

		sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
		// add listener. The listener will be this class
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);}
		// SENSOR_DELAY_FASTEST, SENSOR_DELAY_GAME, SENSOR_DELAY_NORMAL
		
		
	@Override
	protected void onStart(){
		super.onStart();
		
		// register receiver and connect to Arduino
		registerReceiver(arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));
		Amarino.connect(this, DEVICE_ADDRESS);
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		
		// unregister receiver and disconnect from Arduino
		unregisterReceiver(arduinoReceiver);
		Amarino.disconnect(this, DEVICE_ADDRESS);
		
	}

	public void onAccuracyChanged(Sensor sensor,int accuracy){

	}

	public void onSensorChanged(SensorEvent event){

		// check sensor type
		if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){

			// assign directions
			accelX = event.values[0];
			accelY = event.values[1];
			//accelZ = event.values[2];
		}
		
		// reset default accel settings if calibrate is true
		if (calibrate){
			defaultX = accelX;
			defaultY = accelY;
			//defaultZ = accelZ;
			currentX = accelX;
			currentY = accelY;
			//currentZ = accelZ;
			calibrate = false;
		}
		
		// detect current orientation
		orientation = orientationDetect(orientation, accelX);
		
		if (controlMode == 2){
			switch (orientation){
				case PORTRAIT:
					
					// detect discrete level change in Y axis
					if (Math.abs(currentY - accelY) >= accelSensitivity){
						
						// update current Y position
						currentY = currentY + accelSensitivity * Math.round((accelY - currentY)/accelSensitivity);

						sendArduino(Math.round((defaultY - currentY)/accelSensitivity),
								Math.round((defaultX - currentX)/accelSensitivity));
					}
					
					// detect discrete level change in X axis
					if (Math.abs(currentX - accelX) >= accelSensitivity){
						
						// update current X position
						currentX = currentX + accelSensitivity * Math.round((accelX - currentX)/accelSensitivity);

						sendArduino(Math.round((defaultY - currentY)/accelSensitivity),
								Math.round((defaultX - currentX)/accelSensitivity));
					}
					break;
					
				// repeat for other orientations
				case LANDSCAPE1:
					if (Math.abs(currentX - accelX) >= accelSensitivity){
						currentX = currentX + accelSensitivity * Math.round((accelX - currentX)/accelSensitivity);
						sendArduino(Math.round((defaultY - currentY)/accelSensitivity),
								Math.round((defaultX - currentX)/accelSensitivity));
					}
					if (Math.abs(currentY - accelY) >= accelSensitivity){
						currentY = currentY + accelSensitivity * Math.round((accelY - currentY)/accelSensitivity);
						sendArduino(Math.round((defaultY - currentY)/accelSensitivity),
								Math.round((defaultX - currentX)/accelSensitivity));
					}
					break;
				case LANDSCAPE2:
					if (Math.abs(currentX - accelX) >= accelSensitivity){
						currentX = currentX + accelSensitivity * Math.round((accelX - currentX)/accelSensitivity);
						sendArduino(Math.round((defaultY - currentY)/accelSensitivity),
								Math.round((defaultX - currentX)/accelSensitivity));
					}
					if (Math.abs(currentY - accelY) >= accelSensitivity){
						currentY = currentY + accelSensitivity * Math.round((accelY - currentY)/accelSensitivity);
						sendArduino(Math.round((defaultY - currentY)/accelSensitivity),
								Math.round((defaultX - currentX)/accelSensitivity));
					}
					break;
			}
		}
	}
	
	// set up object to listen to Arduino
	public class ArduinoReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent){
			String data = null;
			
			// get device address from where data was sent
			//final String address = intent.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);
			final int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);
			
			// Amarino only supports receiving strings
			if (dataType == AmarinoIntent.STRING_EXTRA){
				data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
				
				if (data != null){
					try {
						// parse int data from string
						final int sensorReading = Integer.parseInt(data);
						zCoor.setText("recieve " + sensorReading);
					} 
					catch (NumberFormatException e) {
						// data not int
					}
				}
			}
		}
	}
	
	/*
	 * This method detects changes in the orientation and sets the orientation state variable
	 * accordingly. Note that it does not detect changes between the two landscape modes.
	 */
	private Orientation orientationDetect(Orientation current, float accelX){
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			return(Orientation.PORTRAIT);
		}else{
			if (current == Orientation.PORTRAIT){
				if (accelX <= 0){
					return(Orientation.LANDSCAPE1);
				}else{
					return(Orientation.LANDSCAPE2);
				}
			}else{
				return(current);
			}
		}
	}
	
	/* 
	 * This method is called to send data to the Arduino, it assumes that
	 * the "speed" and "turn" variables have been set prior to being called
	 */
	private void sendArduino(int speed, int turn){
		sending++;
		
		// do not respond to small tilts in turns
		int offset;
		if (turn >= 4){
			offset = turn - 3;
		}else if (turn <= -4){
			offset = turn + 3;
		}else{
			offset = 0;
		}
		
		// calculate left and right wheel speeds
		int leftSpeed = speed + offset;
		int rightSpeed = speed - offset;
		
		// apply max limit on speed and turn
		if (leftSpeed > 10){
			leftSpeed = 10;
		}else if (leftSpeed < -10){
			leftSpeed = -10;
		}
		if (rightSpeed > 10){
			rightSpeed = 10;
		}else if (rightSpeed < -10){
			rightSpeed = -10;
		}
		
		//xCoor.setText("left " + leftSpeed);
		//yCoor.setText("right " + rightSpeed);
		
		// sendLeft wheel
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'l', leftSpeed);
		xCoor.setText("leftwheel " + leftSpeed);
		
		// sendRight wheel
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'r', rightSpeed);
		yCoor.setText("rightwheel " + rightSpeed);
	}
	
	private void sendArduinoM3(int progress){
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'a', progress);
	}
	
	private void sendArduinoM4(int progress){
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'b', progress);
	}
	
	// This method provides screen orientation locking functionality
	public void onClickLock(View view) {
		if (lock){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			lock = false;
			lockS = "unlocked";
		}else{
			if (orientation == Orientation.PORTRAIT){
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				
				// LANDSCAPE1 not working?! locks itself into portrait
			}else if (orientation == Orientation.LANDSCAPE1){
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
			}else if (orientation == Orientation.LANDSCAPE2){
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
			lock = true;
			lockS = "locked";
		}
    }
	
	public void onClickLopen(View view) {
		if (progressL<90){
			progressL+=1;
		}
		xCoor.setText("leftarm " + (progressL*2));
		sendArduinoM3(2*progressL);
    }
	
	public void onClickLclose(View view) {
		if (progressL>0){
			progressL-=1;
		}
		xCoor.setText("leftarm " + (progressL*2));
		sendArduinoM3(2*progressR);
    }
	
	public void onClickSounds(View view) {
		
		
    }
	
	public void onClickRopen(View view) {
		if(progressR<90){
			progressR+=1;
		}
		yCoor.setText("rightarm " + (progressR*2));
		sendArduinoM4(2*progressR);
    }
	public void onClickRclose(View view) {
		if(progressR>0){
			progressR-=1;
		}
		yCoor.setText("rightarm " + (progressR*2));
		sendArduinoM4(2*progressR);
		
    }
	
	public void onClickStop(View view) {
		
		
		sendArduino(0, 0);
		calibrate = true;
    }
	
public void onClickChangeMode(View view) {
		
		
		sendArduino(0, 0);
		calibrate = true;
		Intent intent = new Intent(this, Bcontroller.class);
        startActivity(intent);
        finish();
    }
	
	
	
	
}