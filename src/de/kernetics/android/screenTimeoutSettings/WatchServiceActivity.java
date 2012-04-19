package de.kernetics.android.screenTimeoutSettings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class WatchServiceActivity extends Activity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		Intent i = new Intent(getApplicationContext(), ScreenTimeoutSettingsService.class);  
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startService(i);
     
        this.finish();
    }
}
