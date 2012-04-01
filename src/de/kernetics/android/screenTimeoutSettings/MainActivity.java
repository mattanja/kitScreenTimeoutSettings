/*
 * Copyright 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.kernetics.android.screenTimeoutSettings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
    //private TimePicker timePickerScreenTimeoutWhenCharging;
    //private EditText editTextStatus;
	private Spinner spinnerCurrentStatus;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //timePickerScreenTimeoutWhenCharging = (TimePicker)findViewById(R.id.timePickerScreenTimeoutWhenCharging);
        //timePickerScreenTimeoutWhenCharging.setIs24HourView(DateFormat.is24HourFormat(getApplicationContext()));
        spinnerCurrentStatus = (Spinner)findViewById(R.id.spinnerCurrentStatus);
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this, R.array.ChargingStatus, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrentStatus.setAdapter(statusAdapter);
        
        BroadcastReceiver batteryChangedReceiver = new BroadcastReceiver() {
        	@Override
        	public void onReceive(Context context, Intent intent) {
        		int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        		int previousTimeout = 0;
        		int newTimeout;
        		int statusSelection = 0;
        		
        		try {
					previousTimeout = Settings.System.getInt(getContentResolver(), 
					        Settings.System.SCREEN_OFF_TIMEOUT);
				} catch (SettingNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        		switch (plugged) {
					case BatteryManager.BATTERY_PLUGGED_AC:
						newTimeout = 180000;
						statusSelection = 1;
						break;
					case BatteryManager.BATTERY_PLUGGED_USB:
						newTimeout = 120000;
						statusSelection = 2;
						break;
					default:
						newTimeout = 30000;
						statusSelection = 0;
						break;
				}
        		
        		if (previousTimeout != newTimeout) {
    				Toast.makeText(MainActivity.this, String.format("Previous sceen-timeout: %s", previousTimeout), Toast.LENGTH_SHORT).show();
            		Toast.makeText(MainActivity.this, String.format("New sceen-timeout: %s", newTimeout), Toast.LENGTH_LONG).show();
    				Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, newTimeout);        			
        		}
        		
				spinnerCurrentStatus.setSelection(statusSelection, true);
        		//editTextStatus.setText(statusText);
        	}
        };
        
        getApplicationContext().registerReceiver(batteryChangedReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //getApplicationContext().registerReceiver(null, new IntentFilter(EDIT));
        
        // Intent: public static final String ACTION_BATTERY_CHANGED
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Toast.makeText(this, "Tapped home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_about:
            	Toast.makeText(this, "About", Toast.LENGTH_LONG).show();
            	break;

        }
        return super.onOptionsItemSelected(item);
    }
}
