package com.pbrane.mike.lifeclock;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.joda.time.Interval;
import org.joda.time.Period;

public class MainActivity extends AppCompatActivity {

	public TextView textView;
	private String targetDateTime = "02/07/1956 06:56:00"; // dd/MM/yyyy HH:mm:ss 24-hour time format
	private DatePicker dp;
	private TimePicker tp;

	// seconds aren't used - yet
	private final int second = 0;
	private boolean goalReached;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		goalReached = false;

		// setup the TextView
		textView = (TextView) findViewById(R.id.textView);
		textView.setTypeface(Typeface.MONOSPACE);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26.0f);

		dp = (DatePicker)findViewById(R.id.datePicker);
		tp = (TimePicker)findViewById(R.id.timePicker);

		DatePicker.OnDateChangedListener dateChangedListener = new DatePicker.OnDateChangedListener() {
			@Override
			public void onDateChanged(DatePicker datePicker, int yearIn, int monthIn, int dayIn) {
				targetDateTime = String.format(Locale.getDefault(), "%02d/%02d/%4d %02d:%02d:%02d", dayIn, monthIn + 1, yearIn, tp.getHour(), tp.getMinute(), second);
				saveDateTime(String.format(Locale.getDefault(), "%02d%02d%04d%02d%02d%02d", dayIn, monthIn + 1, yearIn, tp.getHour(), tp.getMinute(), second));
			}
		};
		dp.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS); // don't allow keyboard to show when long-pressed
		dp.init(2023, 1, 7, dateChangedListener); // month is zero-offset

		TimePicker.OnTimeChangedListener timeChangedListener = new TimePicker.OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker timePicker, int hourIn, int minuteIn) {
				targetDateTime = String.format(Locale.getDefault(), "%02d/%02d/%4d %02d:%02d:%02d", dp.getDayOfMonth(), dp.getMonth() + 1, dp.getYear(), hourIn, minuteIn, second);
				saveDateTime(String.format(Locale.getDefault(), "%02d%02d%04d%02d%02d%02d", dp.getDayOfMonth(), dp.getMonth() + 1, dp.getYear(), hourIn, minuteIn, second));
			}
		};
		tp.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS); // don't allow keyboard to show when long-pressed
		tp.setIs24HourView(true);
		tp.setHour(17);
		tp.setMinute(0);
		tp.setOnTimeChangedListener(timeChangedListener);

		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					if (!goalReached) {
						String currentDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());
						Date start = simpleDateFormat.parse(currentDateTime);
						Date end = simpleDateFormat.parse(targetDateTime);
						if (end.compareTo(start) < 0) { // make sure end date/time is in the future
//							printError();
							Date tmp = end;
							end = start;
							start = tmp;
						} //else {
							calcAndPrintInterval(start, end);
						//}
						handler.postDelayed(this, 1000);
					} else {
						textView.setText(R.string.goalAttained);
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}, 250);

		if (savedInstanceState != null) {
			String date = savedInstanceState.getString("DateTime");
			int[] dateTime = parseDateTimeString(date);
			if (dateTime != null) {
				dp.updateDate(dateTime[2], dateTime[1] - 1, dateTime[0]);
				tp.setHour(dateTime[3]);
				tp.setMinute(dateTime[4]);
				targetDateTime = setTargetDateTime(dateTime);
			}
		} else {
			SharedPreferences sharedPref = this.getPreferences (MODE_PRIVATE);
			String date = sharedPref.getString(getString(R.string.savedDateTime), "");
			int[] dateTime = parseDateTimeString(date);
			if (dateTime != null) {
				dp.updateDate(dateTime[2], dateTime[1] - 1, dateTime[0]);
				tp.setHour(dateTime[3]);
				tp.setMinute(dateTime[4]);
				targetDateTime = setTargetDateTime(dateTime);
			}
		}

	}

	@Override
	public void onSaveInstanceState (@NonNull Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		String dateTime = String.format(Locale.getDefault(), "%02d%02d%04d%02d%02d%02d", dp.getDayOfMonth(), dp.getMonth(), dp.getYear(), tp.getHour(), tp.getMinute(), second);
		savedInstanceState.putString("DateTime", dateTime);
	}

	@Override
	public void onRestoreInstanceState (@NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		String date = savedInstanceState.getString("DateTime");
		int[] dateTime = parseDateTimeString(date);
		if (dateTime != null) {
			dp.updateDate(dateTime[2], dateTime[1], dateTime[0]);
			tp.setHour(dateTime[3]);
			tp.setMinute(dateTime[4]);
			targetDateTime = setTargetDateTime(dateTime);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		// don't need to do anything special
	}

	@Override
	public void onResume() {
		super.onResume();
		// don't need to do anything special
	}

	private void saveDateTime(String dateTime) {
		SharedPreferences sharedPrefs = this.getPreferences (MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(getString(R.string.savedDateTime), dateTime);
		editor.apply();
	}

	// format: dd/MM/yyy HH:mm:ss
	private String setTargetDateTime(int[] dateTime) {
		return String.format(Locale.getDefault(), "%02d/%02d/%4d %02d:%02d:%02d",
				dateTime[0], dateTime[1], dateTime[2], dateTime[3], dateTime[4], dateTime[5]);
	}


	// array = day, month, year, hour, minute, second
	private int[] parseDateTimeString(String str) {
		if (str.length() > 0) {
			int day = Integer.valueOf(str.substring(0, 2));
			int month = Integer.valueOf(str.substring(2, 4));
			int year = Integer.valueOf(str.substring(4, 8));
			int hour = Integer.valueOf(str.substring(8, 10));
			int minute = Integer.valueOf(str.substring(10, 12));
			int second = Integer.valueOf(str.substring(12));
			return new int[] { day, month, year, hour, minute, second };
		}

		return null;
	}

	private void printError() {
		textView.setText("");
		textView.setTextColor(Color.RED);
		textView.append(getText(R.string.errorString));
	}

	// calculate date/time interval using joda library
	private void calcAndPrintInterval(Date startDate, Date endDate) {
		Interval interval = new Interval(startDate.getTime(), endDate.getTime());
		Period period = interval.toPeriod();

		int years = period.getYears();
		int months = period.getMonths();
		int weeks = period.getWeeks();
		int days = period.getDays();
		int hours = period.getHours();
		int minutes = period.getMinutes();
		int seconds = period.getSeconds();

		textView.setText("");
		textView.setTextColor(Color.GRAY);
		if (years > 0) {
			textView.append(String.format(Locale.getDefault(), "%2d %9s\n", years, years > 1 ? "Years" : "Year"));
		}
		if (months > 0) {
			textView.append(String.format(Locale.getDefault(), "%2d %9s\n", months, months > 1 ? "Months" : "Month"));
		}
		if (weeks > 0) {
			textView.append(String.format(Locale.getDefault(), "%2d %9s\n", weeks, weeks > 1 ? "Weeks" : "Week"));
		}
		if (days > 0) {
			textView.append(String.format(Locale.getDefault(), "%2d %9s\n", days, days > 1 ? "Days" : "Day"));
		}
		if (hours > 0) {
			textView.append(String.format(Locale.getDefault(), "%2d %9s\n", hours, hours > 1 ? "Hours" : "Hour"));
		}
		if (minutes > 0) {
			textView.append(String.format(Locale.getDefault(), "%2d %9s\n", minutes, minutes > 1 ? "Minutes" : "Minute"));
		}
		textView.append(String.format(Locale.getDefault(), "%2d %9s\n", seconds, seconds > 1 ? "Seconds" : "Second"));

		if ((years + months + weeks + days + hours + minutes + seconds) <= 0) {
			textView.setTextColor(Color.MAGENTA);
			textView.setText(R.string.goalAttained);
			goalReached = true;
		}
	}

}
