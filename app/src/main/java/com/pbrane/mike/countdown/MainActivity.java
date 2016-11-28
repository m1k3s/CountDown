package com.pbrane.mike.countdown;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
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
	private String retirementDateTime = "01/01/2023 17:00:00"; // dd/MM/yyyy HH:mm:ss
	private DatePicker dp;
	private TimePicker tp;
	// initial values for date/time
	private int year = 2023;
	private int month = 1;
	private int day = 1;
	private int hour = 17;
	private int minute = 0;
	private final int second = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// setup the textview
		textView = (TextView) findViewById(R.id.textView);
		textView.setTypeface(Typeface.MONOSPACE);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32.0f);
		textView.setTextColor(Color.GRAY);

		DatePicker.OnDateChangedListener dateChangedListener = new DatePicker.OnDateChangedListener() {
			@Override
			public void onDateChanged(DatePicker datePicker, int yearIn, int monthIn, int dayIn) {
				year = yearIn;
				month = monthIn + 1;
				day = dayIn;
				retirementDateTime = String.format(Locale.getDefault(), "%02d/%02d/%4d %02d:%02d:%02d", day, month, year, hour, minute, second);
				saveDateTime(String.format(Locale.getDefault(), "%02d%02d%04d%02d%02d%02d", day, month, year, hour, minute, second));
			}
		};

		dp = (DatePicker)findViewById(R.id.datePicker);
		dp.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS); // no keyboard
		dp.init(year, month - 1, day, dateChangedListener); // month is zero-offset

		TimePicker.OnTimeChangedListener timeChangedListener = new TimePicker.OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker timePicker, int hourIn, int minuteIn) {
				hour = hourIn;
				minute = minuteIn;
				retirementDateTime = String.format(Locale.getDefault(), "%02d/%02d/%4d %02d:%02d:%02d", day, month, year, hour, minute, second);
				saveDateTime(String.format(Locale.getDefault(), "%02d%02d%04d%02d%02d%02d", day, month, year, hour, minute, second));
			}
		};

		tp = (TimePicker)findViewById(R.id.timePicker);
		tp.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS); // no keyboard
		tp.setIs24HourView(true);
		tp.setHour(hour);
		tp.setMinute(minute);
		tp.setOnTimeChangedListener(timeChangedListener);

		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					String currentDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());
					final Date start = simpleDateFormat.parse(currentDateTime);
					final Date end = simpleDateFormat.parse(retirementDateTime);
					if (end.compareTo(start) < 0) {
						printError();
					} else {
						printDifference(start, end);
					}
					handler.postDelayed(this, 1000);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}, 250);

		if (savedInstanceState != null) {
			String date = savedInstanceState.getString("DateTime");
			int[] dateTime = parseDateTimeString(date);
			if (dateTime != null) {
				day = dateTime[0];
				month = dateTime[1];
				year = dateTime[2];
				dp.updateDate(year, month - 1, day);
				retirementDateTime = setRetirementDateTime(dateTime);
			}
		} else {
			SharedPreferences sharedPref = this.getPreferences (MODE_PRIVATE);
			String date = sharedPref.getString(getString(R.string.savedDateTime), "");
			int[] dateTime = parseDateTimeString(date);
			if (dateTime != null) {
				day = dateTime[0];
				month = dateTime[1];
				year = dateTime[2];
				dp.updateDate(year, month - 1, day);
				retirementDateTime = setRetirementDateTime(dateTime);
			}
		}

	}

	@Override
	public void onSaveInstanceState (@NonNull Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		String dateTime = String.format(Locale.getDefault(), "%02d%02d%04d", dp.getDayOfMonth(), dp.getMonth() + 1, dp.getYear());
		dateTime += String.format(Locale.getDefault(), "%02d%02d%02d", tp.getHour(), tp.getMinute(), second);
		savedInstanceState.putString("DateTime", dateTime);
	}

	@Override
	public void onRestoreInstanceState (@NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		String date = savedInstanceState.getString("DateTime");
		int[] dateTime = parseDateTimeString(date);
		if (dateTime != null) {
			day = dateTime[0];
			month = dateTime[1];
			year = dateTime[2];
			dp.updateDate(year, month - 1, day);
			tp.setHour(dateTime[3]);
			tp.setMinute(dateTime[4]);
			retirementDateTime = setRetirementDateTime(dateTime);
		}
	}

	private void saveDateTime(String dateTime) {
		SharedPreferences sharedPrefs = this.getPreferences (MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(getString(R.string.savedDateTime), dateTime);
		editor.apply();
	}

	// format: dd/MM/yyy hh:mm:ss
	private String setRetirementDateTime(int[] dateTime) {
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

	@SuppressWarnings("deprecation")
	private void printError() {
		textView.setText("");
		textView.append(Html.fromHtml("<font color=#cc0000><b>The selected date cannot be before today's date</b></font><br>"));
	}

	private void printDifference(Date startDate, Date endDate) {
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
		if (years > 0) {
			textView.append(String.format(Locale.getDefault(), "%3d %10s\n", years, years > 1 ? "Years" : "Year"));
		}
		if (months > 0) {
			textView.append(String.format(Locale.getDefault(), "%3d %10s\n", months, months > 1 ? "Months" : "Month"));
		}
		if (weeks > 0) {
			textView.append(String.format(Locale.getDefault(), "%3d %10s\n", weeks, weeks > 1 ? "Weeks" : "Week"));
		}
		if (days > 0) {
			textView.append(String.format(Locale.getDefault(), "%3d %10s\n", days, days > 1 ? "Days" : "Day"));
		}
		if (hours > 0) {
			textView.append(String.format(Locale.getDefault(), "%3d %10s\n", hours, hours > 1 ? "Hours" : "Hour"));
		}
		if (minutes > 0) {
			textView.append(String.format(Locale.getDefault(), "%3d %10s\n", minutes, minutes > 1 ? "Minutes" : "Minute"));
		}
		if (seconds > 0) {
			textView.append(String.format(Locale.getDefault(), "%3d %10s\n", seconds, seconds > 1 ? "Seconds" : "Second"));
		}
		if ((years + months + weeks + days + hours + minutes + seconds) <= 0) {
			textView.append("You have reached your goal!");
		}
	}

}
