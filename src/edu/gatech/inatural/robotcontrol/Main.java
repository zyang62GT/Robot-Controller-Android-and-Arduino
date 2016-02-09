/* 
 * RobotControl Android Application
 * VIP I-Natural Robot Team
 * Last Edited by Yao Lu, Oct.27, 2012
 */


package edu.gatech.inatural.robotcontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class Main extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void onClick1(View view) {
        Intent intent = new Intent(this, Bcontroller.class);
        startActivity(intent);
    }
    
    public void onClick2(View view) {
        Intent intent = new Intent(this, AccelExample.class);
        startActivity(intent);
    }
    
    public void onClick3(View view) {
        Intent intent = new Intent(this, VerticalSlidebarExampleActivity.class);
        startActivity(intent);
    }
    
    public void onClick4(View view) {
        Intent intent = new Intent(this, BTConnect.class);
        startActivity(intent);
    }
}
