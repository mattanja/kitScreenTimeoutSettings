package de.kernetics.android.preference;

import java.util.prefs.InvalidPreferencesFormatException;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import de.kernetics.android.screenTimeoutSettings.R;

public class BrightnessPreference extends SeekBarDialogPreference implements
        SeekBar.OnSeekBarChangeListener, CheckBox.OnCheckedChangeListener {

    private SeekBar mSeekBar;
    private CheckBox mCheckBox;

    private int mOldBrightness;
    private int mOldAutomatic;

    private boolean mAutomaticAvailable;

    private boolean mRestoredOldState;

    // Backlight range is from 0 - 255. Need to make sure that user
    // doesn't set the backlight to 0 and get stuck
//    private int mScreenBrightnessDim =
//	    getContext().getResources().getInteger(com.android.internal.R.integer.config_screenBrightnessDim);
    //private int mScreenBrightnessDim = 0;
    private static final int MAXIMUM_BACKLIGHT = 255; //android.os.Power.BRIGHTNESS_ON;

    // Internal settings
	private int brightness;
	private int brightnessMode;
	
    public BrightnessPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mAutomaticAvailable = true;
//		context.getResources().getBoolean(
//            com.android.internal.R.bool.config_automatic_brightness_available
//        );

        setDialogLayoutResource(R.layout.preference_dialog_brightness);
        setDialogIcon(R.drawable.ic_settings_display);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        try {
			this.loadPreference();
		} catch (InvalidPreferencesFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			this.brightness = 255;
			this.brightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
		}
        
//        getContext().getContentResolver().registerContentObserver(
//                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), true,
//                mBrightnessObserver);
//
//        getContext().getContentResolver().registerContentObserver(
//                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE), true,
//                mBrightnessModeObserver);
        mRestoredOldState = false;
    }

	@Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mSeekBar = getSeekBar(view);
        mSeekBar.setMax(MAXIMUM_BACKLIGHT); // - mScreenBrightnessDim);
        mOldBrightness = getBrightness(0);
        mSeekBar.setProgress(mOldBrightness); // - mScreenBrightnessDim);

        mCheckBox = (CheckBox)view.findViewById(R.id.automatic_mode);
        if (mAutomaticAvailable) {
            mCheckBox.setOnCheckedChangeListener(this);
            mOldAutomatic = getBrightnessMode(0);
            mCheckBox.setChecked(mOldAutomatic != 0);
        } else {
            mCheckBox.setVisibility(View.GONE);
        }
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromTouch) {
        setBrightness(progress); // + mScreenBrightnessDim);
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // NA
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        // NA
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setBrightnessMode(isChecked ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                : Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        if (!isChecked) {
            setBrightness(mSeekBar.getProgress()); // + mScreenBrightnessDim);
        }
    }

    private int getBrightness(int defaultValue) {
//        int brightness = defaultValue;
//        try {
//            brightness = Settings.System.getInt(getContext().getContentResolver(),
//                    Settings.System.SCREEN_BRIGHTNESS);
//        } catch (SettingNotFoundException snfe) {
//        }
        return this.brightness;
    }

    public int getBrightnessMode(int defaultValue) {
//        int brightnessMode = defaultValue;
//        try {
//            brightnessMode = Settings.System.getInt(getContext().getContentResolver(),
//                    Settings.System.SCREEN_BRIGHTNESS_MODE);
//        } catch (SettingNotFoundException snfe) {
//        }
        return this.brightnessMode;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        //final ContentResolver resolver = getContext().getContentResolver();

        if (positiveResult) {
        	this.brightness = mSeekBar.getProgress();
//            Settings.System.putInt(resolver,
//                    Settings.System.SCREEN_BRIGHTNESS,
//                    mSeekBar.getProgress() + mScreenBrightnessDim);
        } else {
            restoreOldState();
        }

//        resolver.unregisterContentObserver(mBrightnessObserver);
//        resolver.unregisterContentObserver(mBrightnessModeObserver);
    }

    private void restoreOldState() {
        if (mRestoredOldState) return;

        if (mAutomaticAvailable) {
            setBrightnessMode(mOldAutomatic);
        }
        if (!mAutomaticAvailable || mOldAutomatic == 0) {
            setBrightness(mOldBrightness);
        }
        mRestoredOldState = true;
    }

    public void setBrightness(int brightness) {
//        try {
//            IPowerManager power = IPowerManager.Stub.asInterface(
//                    ServiceManager.getService("power"));
//            if (power != null) {
//                power.setBacklightBrightness(brightness);
//            }
//        } catch (RemoteException doe) {
//            
//        }
    	this.brightness = brightness;
    	this.savePreference();
    }
    
    /**
     * Save brightness mode and brightness to single string preference.
     */
    private void savePreference() {
    	persistString(String.format("%s:%s", this.getBrightnessMode(0), this.getBrightness()));
	}

    /**
     * Load brightness mode and brightness from preference.
     * @throws InvalidPreferencesFormatException
     */
    private void loadPreference() throws InvalidPreferencesFormatException {
    	String current = getPersistedString(Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL + ":255");
    	String[] values = current.split(":");
    	if (values.length != 2) {
    		throw new InvalidPreferencesFormatException(getKey());
    	}
    	
    	this.setBrightnessMode(Integer.parseInt(values[0]));
    	this.setBrightness(Integer.parseInt(values[1]));
	}

	public int getBrightness() {
    	return this.brightness;
    }

    public void setBrightnessMode(int mode) {
        if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            mSeekBar.setVisibility(View.GONE);
        } else {
            mSeekBar.setVisibility(View.VISIBLE);
        }
        this.brightnessMode = mode;
        this.savePreference();
//        Settings.System.putInt(getContext().getContentResolver(),
//                Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (getDialog() == null || !getDialog().isShowing()) return superState;

        // Save the dialog state
        final SavedState myState = new SavedState(superState);
        myState.automatic = mCheckBox.isChecked();
        myState.progress = mSeekBar.getProgress();
        myState.oldAutomatic = mOldAutomatic == 1;
        myState.oldProgress = mOldBrightness;

        // Restore the old state when the activity or dialog is being paused
        restoreOldState();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mOldBrightness = myState.oldProgress;
        mOldAutomatic = myState.oldAutomatic ? 1 : 0;
        setBrightnessMode(myState.automatic ? 1 : 0);
        setBrightness(myState.progress); // + mScreenBrightnessDim);
    }

//  private ContentObserver mBrightnessObserver = new ContentObserver(new Handler()) {
//  @Override
//  public void onChange(boolean selfChange) {
//      onBrightnessChanged();
//  }
//};
//
//private ContentObserver mBrightnessModeObserver = new ContentObserver(new Handler()) {
//  @Override
//  public void onChange(boolean selfChange) {
//      onBrightnessModeChanged();
//  }
//};
//
//private void onBrightnessChanged() {
//  int brightness = getBrightness(MAXIMUM_BACKLIGHT);
//  mSeekBar.setProgress(brightness); // - mScreenBrightnessDim);
//}
//
//private void onBrightnessModeChanged() {
//  boolean checked = getBrightnessMode(0) != 0;
//  mCheckBox.setChecked(checked);
//}
    
    /**
     * 
     * @author kern
     *
     */
    private static class SavedState extends BaseSavedState {

        boolean automatic;
        boolean oldAutomatic;
        int progress;
        int oldProgress;

        public SavedState(Parcel source) {
            super(source);
            automatic = source.readInt() == 1;
            progress = source.readInt();
            oldAutomatic = source.readInt() == 1;
            oldProgress = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(automatic ? 1 : 0);
            dest.writeInt(progress);
            dest.writeInt(oldAutomatic ? 1 : 0);
            dest.writeInt(oldProgress);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
