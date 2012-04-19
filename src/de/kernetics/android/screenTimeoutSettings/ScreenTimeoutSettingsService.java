package de.kernetics.android.screenTimeoutSettings;

import de.kernetics.android.preference.TimePreference;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.BatteryManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.widget.Toast;

public class ScreenTimeoutSettingsService extends Service implements
		OnSharedPreferenceChangeListener {

	private static String LOGTAG = "ScreenTimeoutService";

	private SharedPreferences sharedPreferences;
	BroadcastReceiver batteryChangedReceiver;
	private AppSettings appSettings;
	private int previousStatus = -1;

	int mStartMode; // indicates how to behave if the service is killed
	IBinder mBinder; // interface for clients that bind
	boolean mAllowRebind; // indicates whether onRebind should be used

	@Override
	public void onCreate() {
		// The service is being created

		this.initReceiver();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d(LOGTAG, "Updating from preferences: "
				+ sharedPreferences.getAll().toString());
		this.appSettings.updateFromPreferences(sharedPreferences);
		
		// Unchanged status, but update settings with new preferences
		this.updateSystemSettings(previousStatus);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// The service is starting, due to a call to startService()
		Log.d(LOGTAG, "ScreenTimeoutSettingsService started");

		this.sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		this.appSettings = new AppSettings();
		this.appSettings.updateFromPreferences(sharedPreferences);
		this.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		
		Log.d(LOGTAG, "Default preferences: "
				+ sharedPreferences.getAll().toString());

		return mStartMode;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// A client is binding to the service with bindService()
		Log.d(LOGTAG, "onBind()");
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// All clients have unbound with unbindService()
		Log.d(LOGTAG, "onUnbind()");
		return mAllowRebind;
	}

	@Override
	public void onRebind(Intent intent) {
		// A client is binding to the service with bindService(),
		// after onUnbind() has already been called
		Log.d(LOGTAG, "onRebind()");
	}

	public void onDestroy() {
		// The service is no longer used and is being destroyed
		Log.d(LOGTAG, "Service destroyed");
		
		if (sharedPreferences != null) {
			sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		}
	}
	
	/**
	 * Update the system settings - this is where the system settings will
	 * be set.
	 * @param pluggedStatus
	 */
	private void updateSystemSettings(int pluggedStatus) {
		int previousTimeout = 0;
		int newTimeout = 0;
		String newTimeoutString;

		try {
			previousTimeout = Settings.System.getInt(
					getContentResolver(),
					Settings.System.SCREEN_OFF_TIMEOUT);

			Log.d(LOGTAG, "Current screen timeout (sec): " + previousTimeout / 1000);
		} catch (SettingNotFoundException e) {
			Log.wtf(LOGTAG, e.toString());
			e.printStackTrace();
		}

		switch (pluggedStatus) {
		case BatteryManager.BATTERY_PLUGGED_AC:
			Log.d(LOGTAG, "BATTERY_PLUGGED_AC");
			newTimeoutString = ScreenTimeoutSettingsService.this.appSettings.pluggedTimeout;
			break;
		case BatteryManager.BATTERY_PLUGGED_USB:
			Log.d(LOGTAG, "BATTERY_PLUGGED_USB");
			newTimeoutString = ScreenTimeoutSettingsService.this.appSettings.pluggedTimeout;
			break;
		default:
			Log.d(LOGTAG, "UNPLUGGED");
			newTimeoutString = ScreenTimeoutSettingsService.this.appSettings.unpluggedTimeout;
			break;
		}

		
		newTimeout = TimePreference.getTimespan(newTimeoutString);
		if (previousTimeout != newTimeout) {
			String msg = String.format(
					"Setting new sceen-timeout: %s",
					newTimeoutString);
			Toast.makeText(getApplicationContext(), msg,
					Toast.LENGTH_LONG).show();
			Log.d(LOGTAG, msg + "(previously: " + previousTimeout / 1000 + ")");
			
			Settings.System.putInt(getContentResolver(),
					Settings.System.SCREEN_OFF_TIMEOUT, newTimeout);
		}
	}

	/**
	 * Initiate the broadcast receiver for the plug/unplug intent
	 * (ACTION_BATTERY_CHANGED).
	 */
	protected void initReceiver() {
		batteryChangedReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,
						0);
				
				if (plugged == previousStatus) {
					Log.d(LOGTAG, "Unchanged status");
					return;
				}
				
				updateSystemSettings(plugged);
			}
		};

		getApplicationContext().registerReceiver(batteryChangedReceiver,
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}
}
