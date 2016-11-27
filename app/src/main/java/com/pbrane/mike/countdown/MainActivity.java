package com.pbrane.mike.countdown;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		textView = (TextView) findViewById(R.id.textView);
		textView.setTypeface(Typeface.MONOSPACE);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36.0f);
		textView.setTextColor(Color.GRAY);

		final DatePicker dp = (DatePicker)findViewById(R.id.datePicker);
		dp.init(2023, 0, 1, null); // month is zero-offset

		Button button = (Button)findViewById(R.id.button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				int year = dp.getYear();
				int month = dp.getMonth() + 1; // month is zero-offset
				int day = dp.getDayOfMonth();
				retirementDateTime = String.format(Locale.getDefault(), "%02d/%02d/%4d 17:00:00", day, month, year);
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
		}, 1000);

	}

	private void printDifference(Date startDate, Date endDate) {
		Interval interval = new Interval(startDate.getTime(), endDate.getTime());
		Period period = interval.toPeriod();

		int years = period.getYears();
		int months = period.getMonths();
		int days = period.getDays();
		int hours = period.getHours();
		int  minutes = period.getMinutes();
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

//		textView.append(Html.fromHtml("<font color=#cc0000><b>" +
//				String.format(Locale.getDefault(), "%d %s", years, years > 1 ? "Years" : "Year") + "</b></font><br>"));
//
//		textView.append(Html.fromHtml("<font color=#00cc00><b>" +
//				String.format(Locale.getDefault(), "%d %s", months, months > 1 ? "Months" : "Month") + "</b></font><br>"));
//
//		textView.append(Html.fromHtml("<font color=#0000cc><b>" +
//				String.format(Locale.getDefault(), "%d %s", days, days > 1 ? "Days" : "Day") + "</b></font><br>"));
//
//		textView.append(Html.fromHtml("<font color=#cccc00><b>" +
//				String.format(Locale.getDefault(), "%d %s", hours, hours > 1 ? "Hours" : "Hour") + "</b></font><br>"));
//
//		textView.append(Html.fromHtml("<font color=#00cccc><b>" +
//				String.format(Locale.getDefault(), "%d %s", minutes, minutes > 1 ? "Minutes" : "Minute") + "</b></font><br>"));
//
//		textView.append(Html.fromHtml("<font color=#cc00cc><b>" +
//				String.format(Locale.getDefault(), "%d %s", seconds, seconds > 1 ? "Seconds" : "Second") + "</b></font><br>"));
	}

}
