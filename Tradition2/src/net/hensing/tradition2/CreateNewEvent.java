package net.hensing.tradition2;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Scanner;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class CreateNewEvent extends ActionBarActivity {

	// Widget GUI
	Button btnCalendar, btnTimePicker;
	EditText txtDate, txtTime;

	String lat;
	String lng;
	Intent intent, intent2;
	String group;
	String Password;

	DecimalFormat dec = new DecimalFormat("0.0000");
	private String send_message;

	String response = "";
	ServerDataProvider sdp;
	Handler ok = null;
	Handler nok = null;

	// Variable for storing current date and time
	private int mYear, mMonth, mDay, mHour, mMinute;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_new_event);
		btnCalendar = (Button) findViewById(R.id.btnCalendar);
		btnTimePicker = (Button) findViewById(R.id.btnTimePicker);

		txtDate = (EditText) findViewById(R.id.txtDate);
		intent = getIntent();
		//intent2 = getIntent();

		final String temp_lat = intent.getStringExtra(SearchLocation.EXTRA_MESSAGE_LAT);
		final String temp_lng = intent.getStringExtra(SearchLocation.EXTRA_MESSAGE_LON);
		final String temp_group = intent.getStringExtra(SearchLocation.EXTRA_MESSAGE_GROUP);
		lat = temp_lat;
		lng = temp_lng;
		group = temp_group;

		createHandlers();

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_new_event, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_create_new_event, container, false);
			return rootView;
		}
	}

	public void calendarClick(View v) {
		// TODO Auto-generated method stub


		// Process to get Current Date
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);

		// Launch Date Picker Dialog
		DatePickerDialog dpd = new DatePickerDialog(this,
				new DatePickerDialog.OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year,
					int monthOfYear, int dayOfMonth) {
				// Display Selected date in textbox
				EditText txtDate2 = (EditText) findViewById(R.id.txtDate);
				txtDate2.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);

			}
		}, mYear, mMonth, mDay);
		dpd.show();

	}

	public void timeClick(View v) {
		// Process to get Current Time
		final Calendar c = Calendar.getInstance();
		mHour = c.get(Calendar.HOUR_OF_DAY);
		mMinute = c.get(Calendar.MINUTE);

		// Launch Time Picker Dialog
		TimePickerDialog tpd = new TimePickerDialog(this,
				new TimePickerDialog.OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay,
					int minute) {
				// Display Selected time in textbox
				EditText txtTime2 = (EditText) findViewById(R.id.txtTimeXXX);
				txtTime2.setText(hourOfDay + ":" + minute +":00");
			}
		}, mHour, mMinute, true);
		tpd.show();


	}

	public void timeClickBefore(View v) {
		// Process to get Current Time
		final Calendar c = Calendar.getInstance();
		mHour = c.get(Calendar.HOUR_OF_DAY);
		mMinute = c.get(Calendar.MINUTE);

		// Launch Time Picker Dialog
		TimePickerDialog tpd = new TimePickerDialog(this,
				new TimePickerDialog.OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay,
					int minute) {
				// Display Selected time in textbox
				EditText txtTime2 = (EditText) findViewById(R.id.txtTimeBefore);
				txtTime2.setText(hourOfDay + ":" + minute +":00");
			}
		}, mHour, mMinute, true);
		tpd.show();


	}
	public void timeClickUntil(View v) {
		// Process to get Current Time
		final Calendar c = Calendar.getInstance();
		mHour = c.get(Calendar.HOUR_OF_DAY);
		mMinute = c.get(Calendar.MINUTE);

		// Launch Time Picker Dialog
		TimePickerDialog tpd = new TimePickerDialog(this,
				new TimePickerDialog.OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay,
					int minute) {
				// Display Selected time in textbox
				EditText txtTime2 = (EditText) findViewById(R.id.txtTimeUntil);
				txtTime2.setText(hourOfDay + ":" + minute +":00");
			}
		}, mHour, mMinute, true);
		tpd.show();


	}
	public void registerClick(View view) {

		EditText mDate = (EditText) findViewById(R.id.txtDate);
		EditText mTimeS = (EditText) findViewById(R.id.txtTimeXXX);
		EditText mTimeB = (EditText) findViewById(R.id.txtTimeBefore);
		EditText mTimeU = (EditText) findViewById(R.id.txtTimeUntil);

		String SmDate = mDate.getText().toString(); 
		String SmTimeS = mTimeS.getText().toString(); 
		String SmTimeB = mTimeB.getText().toString(); 
		String SmTimeU = mTimeU.getText().toString(); 

		EditText edName = (EditText) findViewById(R.id.EventName);
		String eventName = edName.getText().toString();

		if (!SmDate.equals("") && !SmTimeS.equals("") && !SmTimeB.equals("") && !SmTimeU.equals("")){


			String eventTime, startTime, endTime;
			eventTime = SmDate+" "+SmTimeS;
			startTime = SmDate+" "+SmTimeB;
			endTime = SmDate+" "+SmTimeU;


			if (!eventName.equals("") && !eventName.contains(" ")){

				send_message = "CREATE_EVENT "+eventTime + " " +startTime+ " " +endTime+ " " +lat+ " " +lng+ " " + eventName + " " + group;

				//print("send_message: "+send_message);
				sdp = new ServerDataProvider(send_message,nok,ok);
				Thread thread = new Thread(sdp);
				thread.start();	


			}
			else {

				Toast.makeText(getApplicationContext(),
						"Invalid Name", Toast.LENGTH_LONG)
						.show();
			}


		}
		else {

			Toast.makeText(getApplicationContext(),
					"Empty field", Toast.LENGTH_LONG)
					.show();

		}
	}
	public void print(String message) {

		//Log.d("MyLog ", "message: " + message);

	}



	private void parser(String msg) {

		print("in parser: "+msg);
		Scanner scanner = new Scanner(msg);
		//scanner.useDelimiter("=");
		if (scanner.findInLine("EVENT ") != null){

			String word;
			word = scanner.next();
			//print(word);
			if (word.equals("OK")){
				eventSuccess();
			}
			else {
				CharSequence text = "Invalid creation!";
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(this, text, duration);
				toast.show();


			}
		}
		else {
			CharSequence text = "Invalid event";
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(this, text, duration);
			toast.show();
		}
	}


	private void eventSuccess() {

		Toast.makeText(this,
				"Event created!", Toast.LENGTH_LONG)
				.show();
		intent = new Intent(this, SelectGroup.class);

		startActivity(intent);

	}

	public void createHandlers(){
		ok = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String message = (String) msg.obj; //Extract the string from the Message
				//print(message);
				parser(message);

			}
		};
		nok = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				showProblemMessage();
			}
		};   	

	}

	public void showProblemMessage(){
		Toast.makeText(this, "Connection Problem", Toast.LENGTH_LONG).show();
	}	

}
