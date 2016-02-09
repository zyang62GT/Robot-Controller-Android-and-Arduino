package edu.gatech.inatural.robotcontrol;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

public class VerticalSlidebarExampleActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        VerticalProgressBar vProgressBar = (VerticalProgressBar)findViewById(R.id.ProgressBar01);
        vProgressBar.setMax(100);
        vProgressBar.setProgress(20);
        //vProgressBar.setSecondaryProgress(50);

        VerticalSeekBar vSeekBar = (VerticalSeekBar)findViewById(R.id.SeekBar01);
        vSeekBar.setMax(100);
        vSeekBar.setProgress(30);
        vSeekBar.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {

    		public void onStopTrackingTouch(VerticalSeekBar seekBar) {
    		}

    		public void onStartTrackingTouch(VerticalSeekBar seekBar) {

    		}

    		public void onProgressChanged(VerticalSeekBar seekBar, int progress,
    				boolean fromUser) {
//Log.v("D",String.valueOf(progress));
    		}
    	});

        VerticalSeekBar vSeekBar2 = (VerticalSeekBar)findViewById(R.id.SeekBar02);
        vSeekBar2.setMax(100);
        vSeekBar2.setProgress(30);

    }
    
	@Override
	protected void onStart(){
		super.onStart();
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		
	}
}