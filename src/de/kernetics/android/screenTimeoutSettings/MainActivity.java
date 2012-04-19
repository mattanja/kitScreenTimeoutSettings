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

import java.util.List;

import de.kernetics.android.preference.BrightnessPreference;
import de.kernetics.android.preference.TimePreference;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * 
 * @author Mattanja Kern <mattanja.kern@kernetics.de>
 *
 */
public class MainActivity extends PreferenceActivity {

	public static final String LogTag = "MainActivity";

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
	
	@Override
	protected void onStart() {
		super.onStart();
	}
    
    @Override
    public void onBuildHeaders(List<Header> target) {
    	//super.onBuildHeaders(target);
    	loadHeadersFromResource(R.xml.screen_timeout_preferenceheaders, target);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    }
    
    /**
     * 
     * The settings fragment.
     *
     */
    public static class ScreenTimeoutSettings extends PreferenceFragment
	{
    	private BrightnessPreference pluggedBrightness;
		private TimePreference pluggedTimeout;
		
		private BrightnessPreference unpluggedBrightness;
		private TimePreference unpluggedTimeout;
		
		public static final String PLUGGED_BRIGHTNESS = "pluggedBrightness";
		public static final String PLUGGED_TIMEOUT = "pluggedTimeout";
		public static final String UNPLUGGED_BRIGHTNESS = "unpluggedBrightness";
		public static final String UNPLUGGED_TIMEOUT = "unpluggedTimeout";

		// TODO: Car mode switching

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
	 		PreferenceManager.setDefaultValues(getActivity(), R.xml.screen_timeout_settings, false);
	 		this.addPreferencesFromResource(R.xml.screen_timeout_settings);
	 		
	 		//SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());

	 		pluggedBrightness = (BrightnessPreference)findPreference(PLUGGED_BRIGHTNESS);
	 		pluggedTimeout = (TimePreference)findPreference(PLUGGED_TIMEOUT);
	 		unpluggedBrightness = (BrightnessPreference)findPreference(UNPLUGGED_BRIGHTNESS);
	 		unpluggedTimeout = (TimePreference)findPreference(UNPLUGGED_TIMEOUT);
	 		
	 		pluggedBrightness.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Log.d(LogTag, String.format("Preference change (%s=%s)", preference.getKey(), newValue));
					return true;
				}
			});
	 		pluggedTimeout.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Log.d(LogTag, String.format("Preference change (%s=%s)", preference.getKey(), newValue));
					return true;
				}
			});

	 		unpluggedBrightness.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Log.d(LogTag, String.format("Preference change (%s=%s)", preference.getKey(), newValue));
					return true;
				}
			});
	 		unpluggedTimeout.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Log.d(LogTag, String.format("Preference change (%s=%s)", preference.getKey(), newValue));
					return true;
				}
			});

	 		/*
	 		if (Log.isLoggable(LogTag, Log.DEBUG)) {
		 		Log.d(LogTag, "Plugged brightness: " + MainActivity.AppSettings.pluggedBrightness);
		 		Log.d(LogTag, "Plugged auto brightness: " + MainActivity.AppSettings.pluggedAutoBrightness);
		 		Log.d(LogTag, "Plugged timeout: " + MainActivity.AppSettings.pluggedTimeout / 1000);
		 		
		 		Log.d(LogTag, "Unplugged brightness: " + MainActivity.AppSettings.unpluggedBrightness);
		 		Log.d(LogTag, "Unplugged auto brightness: " + MainActivity.AppSettings.unpluggedAutoBrightness);
		 		Log.d(LogTag, "Unplugged timeout: " + MainActivity.AppSettings.unpluggedTimeout / 1000);
		 		pluggedBrightness.getBrightnessMode(Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
	 		}
	 		*/
	 	}
	 }
}
