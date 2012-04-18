/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.kernetics.android.preference;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import de.kernetics.android.screenTimeoutSettings.R;

import java.util.Calendar;
import java.util.Locale;

/**
 * A view for selecting a time with hours, minutes and seconds. The
 * hour, each minute digit, and seconds can be controlled by
 * vertical spinners. The hour can be entered by keyboard input. Entering in two
 * digit hours can be accomplished by hitting two digits within a timeout of
 * about a second (e.g. '1' then '2' to select 12). The minutes can be entered
 * by entering single digits. Under AM/PM mode, the user can hit 'a', 'A", 'p'
 * or 'P' to pick. For a dialog using this view, see
 * {@link android.app.TimePickerDialog}.
 *<p>
 * See the <a href="{@docRoot}resources/tutorials/views/hello-timepicker.html">Time Picker
 * tutorial</a>.
 * </p>
 */
public class TimeSecondsPicker extends FrameLayout {

    private static final boolean DEFAULT_ENABLED_STATE = true;

    /**
     * A no-op callback used in the constructor to avoid null checks later in
     * the code.
     */
    private static final OnTimeChangedListener NO_OP_CHANGE_LISTENER = new OnTimeChangedListener() {
        public void onTimeChanged(TimeSecondsPicker view, int hourOfDay, int minute, int second) {
        }
    };

    // ui components
    private final NumberPicker mHourSpinner;
    private final NumberPicker mMinuteSpinner;
    private final NumberPicker mSecondsSpinner;

//    private final EditText mHourSpinnerInput;
//    private final EditText mMinuteSpinnerInput;
//    private final EditText mSecondsSpinnerInput;

    private final TextView mDivider;
    private final TextView mDivider2;

    private boolean mIsEnabled = DEFAULT_ENABLED_STATE;

    // callbacks
    private OnTimeChangedListener mOnTimeChangedListener;

    private Calendar mTempCalendar;

    private Locale mCurrentLocale;

    /**
     * Use a custom NumberPicker formatting callback to use two-digit minutes
     * strings like "01". Keeping a static formatter etc. is the most efficient
     * way to do this; it avoids creating temporary objects on every call to
     * format().
     *
     * @hide
     */
    public static final NumberPicker.Formatter TWO_DIGIT_FORMATTER = new NumberPicker.Formatter() {
        final StringBuilder mBuilder = new StringBuilder();

        final java.util.Formatter mFmt = new java.util.Formatter(mBuilder, java.util.Locale.US);

        final Object[] mArgs = new Object[1];

        public String format(int value) {
            mArgs[0] = value;
            mBuilder.delete(0, mBuilder.length());
            mFmt.format("%02d", mArgs);
            return mFmt.toString();
        }
    };
    
    /**
     * The callback interface used to indicate the time has been adjusted.
     */
    public interface OnTimeChangedListener {

        /**
         * @param view The view associated with this listener.
         * @param hours The current hours.
         * @param minutes The current minutes.
         * @param seconds The current seconds.
         */
        void onTimeChanged(TimeSecondsPicker view, int hours, int minutes, int seconds);
    }

    public TimeSecondsPicker(Context context) {
        this(context, null);
    }

    public TimeSecondsPicker(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.timeSecondsPickerStyle);
    }

    public TimeSecondsPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // initialization based on locale
        setCurrentLocale(Locale.getDefault());

        // process style attributes
        TypedArray attributesArray = context.obtainStyledAttributes(
                attrs, R.styleable.TimeSecondsPicker, defStyle, 0);
        int layoutResourceId = attributesArray.getResourceId(
                R.styleable.TimeSecondsPicker_android_layout, R.layout.time_seconds_picker);
        attributesArray.recycle();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layoutResourceId, this, true);

        // divider (only for the new widget style)
        mDivider = (TextView) findViewById(R.id.divider);
        if (mDivider != null) {
            mDivider.setText(R.string.time_picker_separator);
        }
        mDivider2 = (TextView) findViewById(R.id.divider2);
        if (mDivider2 != null) {
            mDivider2.setText(R.string.time_picker_separator);
        }

        // hour
        mHourSpinner = (NumberPicker) findViewById(R.id.hours);
        mHourSpinner.setMinValue(0);
        mHourSpinner.setMaxValue(23);
        mHourSpinner.setOnLongPressUpdateInterval(100);
        mHourSpinner.setFormatter(TWO_DIGIT_FORMATTER);
        mHourSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker spinner, int oldVal, int newVal) {
                onTimeChanged();
            }
        });
//        mHourSpinnerInput = (EditText) mHourSpinner. //findViewById(R.id.numberpicker_input);
//        mHourSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        // minute
        mMinuteSpinner = (NumberPicker) findViewById(R.id.minutes);
        mMinuteSpinner.setMinValue(0);
        mMinuteSpinner.setMaxValue(59);
        mMinuteSpinner.setOnLongPressUpdateInterval(100);
        mMinuteSpinner.setFormatter(TWO_DIGIT_FORMATTER);
        mMinuteSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker spinner, int oldVal, int newVal) {
                int minValue = mMinuteSpinner.getMinValue();
                int maxValue = mMinuteSpinner.getMaxValue();
                if (oldVal == maxValue && newVal == minValue) {
                    int newHour = mHourSpinner.getValue() + 1;
                    mHourSpinner.setValue(newHour);
                } else if (oldVal == minValue && newVal == maxValue) {
                    int newHour = mHourSpinner.getValue() - 1;
                    mHourSpinner.setValue(newHour);
                }
                onTimeChanged();
            }
        });
//        mMinuteSpinnerInput = (EditText) mMinuteSpinner.findViewById(R.id.numberpicker_input);
//        mMinuteSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        // seconds
        mSecondsSpinner = (NumberPicker) findViewById(R.id.seconds);
        mSecondsSpinner.setMinValue(0);
        mSecondsSpinner.setMaxValue(59);
        mSecondsSpinner.setOnLongPressUpdateInterval(100);
        mSecondsSpinner.setFormatter(TWO_DIGIT_FORMATTER);
        mSecondsSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker spinner, int oldVal, int newVal) {
                int minValue = mSecondsSpinner.getMinValue();
                int maxValue = mSecondsSpinner.getMaxValue();
                if (oldVal == maxValue && newVal == minValue) {
                    int newMinute = mMinuteSpinner.getValue() + 1;
                    mMinuteSpinner.setValue(newMinute);
                } else if (oldVal == minValue && newVal == maxValue) {
                    int newMinute = mMinuteSpinner.getValue() - 1;
                    mMinuteSpinner.setValue(newMinute);
                }
                onTimeChanged();
            }
        });
//        mSecondsSpinnerInput = (EditText) mSecondsSpinner.findViewById(R.id.numberpicker_input);
//        mSecondsSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        // update controls to initial state
        
        setOnTimeChangedListener(NO_OP_CHANGE_LISTENER);

        // set to current time
        setCurrentHour(mTempCalendar.get(Calendar.HOUR_OF_DAY));
        setCurrentMinute(mTempCalendar.get(Calendar.MINUTE));
        setCurrentSecond(mTempCalendar.get(Calendar.SECOND));

        if (!isEnabled()) {
            setEnabled(false);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mIsEnabled == enabled) {
            return;
        }
        super.setEnabled(enabled);
        
        mHourSpinner.setEnabled(enabled);
        mMinuteSpinner.setEnabled(enabled);
        mSecondsSpinner.setEnabled(enabled);
        
        if (mDivider != null) {
            mDivider.setEnabled(enabled);
        }
        if (mDivider2 != null) {
            mDivider2.setEnabled(enabled);
        }

        mIsEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return mIsEnabled;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
    }

    /**
     * Sets the current locale.
     *
     * @param locale The current locale.
     */
    private void setCurrentLocale(Locale locale) {
        if (locale.equals(mCurrentLocale)) {
            return;
        }
        mCurrentLocale = locale;
        mTempCalendar = Calendar.getInstance(locale);
    }

    /**
     * Used to save / restore state of time picker
     */
    private static class SavedState extends BaseSavedState {

        private final int mHour;
        private final int mMinute;
        private final int mSecond;

        private SavedState(Parcelable superState, int hour, int minute, int second) {
            super(superState);
            mHour = hour;
            mMinute = minute;
            mSecond = second;
        }

        private SavedState(Parcel in) {
            super(in);
            mHour = in.readInt();
            mMinute = in.readInt();
            mSecond = in.readInt();
        }

        public int getHour() {
            return mHour;
        }

        public int getMinute() {
            return mMinute;
        }
        
        public int getSecond() {
        	return mSecond;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mHour);
            dest.writeInt(mMinute);
            dest.writeInt(mSecond);
        }

        @SuppressWarnings({"unused"})
        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, getCurrentHour(), getCurrentMinute(), getCurrentSecond());
    }

	@Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentHour(ss.getHour());
        setCurrentMinute(ss.getMinute());
        setCurrentSecond(ss.getSecond());
    }

    /**
     * Set the callback that indicates the time has been adjusted by the user.
     *
     * @param onTimeChangedListener the callback, should not be null.
     */
    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        mOnTimeChangedListener = onTimeChangedListener;
    }

    /**
     * @return The current hour in the range (0-23).
     */
    public Integer getCurrentHour() {
        return mHourSpinner.getValue();
    }

    /**
     * Set the current hour.
     */
    public void setCurrentHour(Integer currentHour) {
        if (currentHour == getCurrentHour()) {
            return;
        }
        mHourSpinner.setValue(currentHour);
        onTimeChanged();
    }

    /**
     * @return The current minute.
     */
    public Integer getCurrentMinute() {
        return mMinuteSpinner.getValue();
    }

    /**
     * Set the current minute (0-59).
     */
    public void setCurrentMinute(Integer currentMinute) {
        if (currentMinute == getCurrentMinute()) {
            return;
        }
        mMinuteSpinner.setValue(currentMinute);
        onTimeChanged();
    }

    /**
     * @return The current minute.
     */
    public Integer getCurrentSecond() {
        return mSecondsSpinner.getValue();
    }

    /**
     * Set the current second (0-59).
     */
    public void setCurrentSecond(Integer currentSecond) {
        if (currentSecond == getCurrentSecond()) {
            return;
        }
        mSecondsSpinner.setValue(currentSecond);
        onTimeChanged();
    }

    @Override
    public int getBaseline() {
        return mHourSpinner.getBaseline();
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    @Override
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);

        int flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR;
        mTempCalendar.set(Calendar.HOUR_OF_DAY, getCurrentHour());
        mTempCalendar.set(Calendar.MINUTE, getCurrentMinute());
        mTempCalendar.set(Calendar.SECOND, getCurrentSecond());
        
        Context mContext = getContext();
        String selectedDateUtterance = DateUtils.formatDateTime(mContext,
                mTempCalendar.getTimeInMillis(), flags);
        event.getText().add(selectedDateUtterance);
    }

    private void onTimeChanged() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        if (mOnTimeChangedListener != null) {
            mOnTimeChangedListener.onTimeChanged(this, getCurrentHour(), getCurrentMinute(), getCurrentSecond());
        }
    }
}