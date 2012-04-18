package de.kernetics.android.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

public class TimePreference extends DialogPreference {
	private int lastHour = 0;
	private int lastMinute = 0;
	private int lastSecond = 0;
	private TimeSecondsPicker picker = null;
	
	public static int getHour(String time) {
		String[] pieces = time.split(":");

		return (Integer.parseInt(pieces[0]));
	}

	public static int getMinute(String time) {
		String[] pieces = time.split(":");

		return (Integer.parseInt(pieces[1]));
	}

	public static int getSecond(String time) {
		String[] pieces = time.split(":");

		return (Integer.parseInt(pieces[2]));
	}

	public TimePreference(Context ctxt) {
		this(ctxt, null);
	}

	public TimePreference(Context ctxt, AttributeSet attrs) {
		this(ctxt, attrs, 0);
	}

	public TimePreference(Context ctxt, AttributeSet attrs, int defStyle) {
		super(ctxt, attrs, defStyle);

		setPositiveButtonText("Set");
		setNegativeButtonText("Cancel");
	}

	@Override
	protected View onCreateDialogView() {
		picker = new TimeSecondsPicker(getContext());
		return (picker);
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);

		picker.setCurrentHour(lastHour);
		picker.setCurrentMinute(lastMinute);
		picker.setCurrentSecond(lastSecond);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			lastHour = picker.getCurrentHour();
			lastMinute = picker.getCurrentMinute();
			lastSecond = picker.getCurrentSecond();

			String time = TimeSecondsPicker.TWO_DIGIT_FORMATTER.format(lastHour)
				+ ":" + TimeSecondsPicker.TWO_DIGIT_FORMATTER.format(lastMinute)
				+ ":" + TimeSecondsPicker.TWO_DIGIT_FORMATTER.format(lastSecond);

			if (callChangeListener(time)) {
				persistString(time);
			}
			
			this.setSummary(time);
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return (a.getString(index));
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		String time = null;

		if (restoreValue) {
			if (defaultValue == null) {
				time = getPersistedString("00:00:00");
			} else {
				time = getPersistedString(defaultValue.toString());
			}
		} else {
			time = defaultValue.toString();
		}

		this.setSummary(time);
		
		lastHour = getHour(time);
		lastMinute = getMinute(time);
		lastSecond = getSecond(time);
	}
}