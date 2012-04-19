package de.kernetics.android.screenTimeoutSettings;

import java.util.prefs.InvalidPreferencesFormatException;

import de.kernetics.android.preference.BrightnessPreference;
import de.kernetics.android.preference.TimePreference;
import de.kernetics.android.screenTimeoutSettings.MainActivity.ScreenTimeoutSettings;
import android.content.SharedPreferences;
import android.provider.Settings;

public class AppSettings {

	public int pluggedBrightness;
	public boolean pluggedAutoBrightness;
	public String pluggedTimeout;

	public int unpluggedBrightness;
	public boolean unpluggedAutoBrightness;
	public String unpluggedTimeout;
	
	public void updateFromPreferences(SharedPreferences sp) {
		String pb = sp.getString(ScreenTimeoutSettings.PLUGGED_BRIGHTNESS, "1:255");
		String pt = sp.getString(ScreenTimeoutSettings.PLUGGED_TIMEOUT, "00:02:00");
		
		String upb = sp.getString(ScreenTimeoutSettings.UNPLUGGED_BRIGHTNESS, "1:127");
		String upt = sp.getString(ScreenTimeoutSettings.UNPLUGGED_TIMEOUT, "00:00:30");
		
		try {
			pluggedBrightness = BrightnessPreference.getBrightness(pb);
			pluggedAutoBrightness = BrightnessPreference.getBrightnessMode(pb) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
			pluggedTimeout = pt; //TimePreference.getTimespan(pt);
			
			unpluggedBrightness = BrightnessPreference.getBrightness(upb);
			unpluggedAutoBrightness = BrightnessPreference.getBrightnessMode(upb) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
			unpluggedTimeout = upt; //TimePreference.getTimespan(upt);
		} catch (InvalidPreferencesFormatException e) {
			e.printStackTrace();
		}
	}

}
