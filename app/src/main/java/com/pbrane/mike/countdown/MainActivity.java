package com.pbrane.mike.countdown;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.joda.time.Interval;
import org.joda.time.Period;

public class MainActivity extends AppCompatActivity {

	public TextView textView;
	String retirementDateTime = "01/01/2023 17:00:00"; // dd/M/yyyy hh:mm:ss
	private DatePicker dp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		textView = (TextView) findViewById(R.id.textView);
		textView.setTypeface(Typeface.MONOSPACE);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36.0f);
		textView.setTextColor(Color.GRAY);

		dp = (DatePicker)findViewById(R.id.datePicker);
		dp.init(2023, 0, 1, null); // month is zero-offset

		Button button = (Button)findViewById(R.id.button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				int year = dp.getYear();
				int month = dp.getMonth() + 1; // month is zero-offset
				int day = dp.getDayOfMonth();
				retirementDateTime = String.format(Locale.getDefault(), "%02d/%02d/%4d 17:00:00", day, month, year);
				saveDateTime(String.format(Locale.getDefault(), "%04d%02d%02d170000", year, month, day));
			}
		});

		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.getDefault());

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					String currentDateTime = new SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());
					final Date start = simpleDateFormat.parse(currentDateTime);
					final Date end = simpleDateFormat.parse(retirementDateTime);
					printDifference(start, end);
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
				dp.init(dateTime[0], dateTime[1] - 1, dateTime[2], null);
				retirementDateTime = String.format(Locale.getDefault(), "%02d/%02d/%4d %02d:%02d:%02d", dateTime[2], dateTime[1], dateTime[0], dateTime[3], dateTime[4], dateTime[5]);
			}
		} else {
			SharedPreferences sharedPref = this.getPreferences (MODE_PRIVATE);
			String date = sharedPref.getString(getString(R.string.savedDateTime), "");
			int[] dateTime = parseDateTimeString(date);
			if (dateTime != null) {
				dp.init(dateTime[0], dateTime[1] - 1, dateTime[2], null);
				retirementDateTime = String.format(Locale.getDefault(), "%02d/%02d/%4d %02d:%02d:%02d", dateTime[2], dateTime[1], dateTime[0], dateTime[3], dateTime[4], dateTime[5]);
			}
		}

	}

	@Override
	public void onSaveInstanceState (@NonNull Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		String dateTime = String.format(Locale.getDefault(), "%04d%02d%02d", dp.getYear(), dp.getMonth() + 1, dp.getDayOfMonth());
		dateTime += "170000";
		savedInstanceState.putString("DateTime", dateTime);
	}

	@Override
	public void onRestoreInstanceState (@NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		String date = savedInstanceState.getString("DateTime");
		int[] dateTime = parseDateTimeString(date);
		if (dateTime != null) {
			dp.init(dateTime[0], dateTime[1] - 1, dateTime[2], null);
			retirementDateTime = String.format(Locale.getDefault(), "%02d/%02d/%4d %02d:%02d:%02d", dateTime[2], dateTime[1], dateTime[0], dateTime[3], dateTime[4], dateTime[5]);
		}
	}

	private void saveDateTime(String dateTime) {
		SharedPreferences sharedPrefs = this.getPreferences (MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(getString(R.string.savedDateTime), dateTime);
		editor.apply();
	}

	// array = year, month, day, hour, minute, second
	private int[] parseDateTimeString(String str) {
		if (str.length() > 0) {
			int year = Integer.valueOf(str.substring(0, 4));
			int month = Integer.valueOf(str.substring(4, 6));
			int day = Integer.valueOf(str.substring(6, 8));
			int hour = Integer.valueOf(str.substring(8, 10));
			int minute = Integer.valueOf(str.substring(10, 12));
			int second = Integer.valueOf(str.substring(12));
			return new int[]{year, month, day, hour, minute, second};
		}

		return null;
	}

	private void printDifference(Date startDate, Date endDate) {
		Interval interval = new Interval(startDate.getTime(), endDate.getTime());
		Period period = interval.toPeriod();

		int years = period.getYears();
		int months = period.getMonths();
		int days = period.getDays();
		int hours = period.getHours();
		int minutes = period.getMinutes();
		int seconds = period.getSeconds();

		textView.setText("");
		if (years > 0) {
			textView.append(String.format(Locale.getDefault(), "%-4d %s\n", years, years > 1 ? "Years" : "Year"));
		}
		if (months > 0) {
			textView.append(String.format(Locale.getDefault(), "%-4d %s\n", months, months > 1 ? "Months" : "Month"));
		}
		if (days > 0) {
			textView.append(String.format(Locale.getDefault(), "%-4d %s\n", days, days > 1 ? "Days" : "Day"));
		}
		if (hours > 0) {
			textView.append(String.format(Locale.getDefault(), "%-4d %s\n", hours, hours > 1 ? "Hours" : "Hour"));
		}
		if (minutes > 0) {
			textView.append(String.format(Locale.getDefault(), "%-4d %s\n", minutes, minutes > 1 ? "Minutes" : "Minute"));
		}
		textView.append(String.format(Locale.getDefault(), "%-4d %s\n", seconds, seconds > 1 ? "Seconds" : "Second"));
	}

}
