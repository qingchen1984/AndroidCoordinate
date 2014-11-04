package net.hensing.tradition2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Scanner;

import net.hensing.tradition2.WelcomeActivity.ClientThread;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import android.os.Build;

public class CreateNewEvent extends ActionBarActivity {

	// Widget GUI
	Button btnCalendar, btnTimePicker;
	EditText txtDate, txtTime;

	String lat;
	String lng;
	Intent intent, intent2;
	String group;
	String Password;
	private boolean isConnected = false;
	private Socket socket;
	private static final int SERVERPORT = 1234;
	//private static final String SERVER_IP = "10.0.2.2";
	private static final String SERVER_IP = "90.226.9.91";	
	DecimalFormat dec = new DecimalFormat("0.0000");
	private String send_message;

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

		//Toast.makeText(getApplicationContext(),
		//		lat+lng+group, Toast.LENGTH_LONG)
		//		.show();
		//txtTime = (EditText) findViewById(R.id.txtTime);

		//btnCalendar.setOnClickListener(this);
		//btnTimePicker.setOnClickListener(this);

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

				new Thread(new ClientThread()).start();

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

	class ClientThread implements Runnable {
		// thread to connect to socket and listen
		// for messages and then print them in chatbox.
		public void run(){
			try {
				InetAddress serverAddress = InetAddress.getByName(SERVER_IP);			
				socket = new Socket();
				socket.connect(new InetSocketAddress(serverAddress, SERVERPORT), 9000);
				isConnected = true;
				print("Connected to server");
				PrintWriter out;

				try {
					out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
					out.println(send_message);
					out.flush();
					//clearChat();
					print("Jag: " + send_message);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				BufferedReader buf_from_server = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				while(isConnected && socket.isConnected()){
					String line_from_server = buf_from_server.readLine();
					if (line_from_server == null){print("exit");
					isConnected=false; print("disconnected from server"); break;}

					print(line_from_server);
					parser(line_from_server);
				}

				//handler_add.sendEmptyMessage(0);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				print("ERROR No conn est...");
			}

		}

		private void parser(String msg) {
			
			
			Scanner scanner = new Scanner(msg);
			//scanner.useDelimiter("=");
			if (scanner.findInLine("EVENT ") != null){
				
				String word;
				word = scanner.next();
				if (word.equals("OK")){
					handler_add.sendEmptyMessage(0);	
				}
				else {
					handler_remove.sendEmptyMessage(0);	
				}

				
			}
		}
	}  

	Handler handler_add = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			eventSuccess();
		}
	};
	Handler handler_remove = new Handler() {
		@Override
		public void handleMessage(Message msg) {


			Context context = getApplicationContext();
			CharSequence text = "Invalid creation!";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	};	
	private void eventSuccess() {
		
		Toast.makeText(getApplicationContext(),
				"Event created!", Toast.LENGTH_LONG)
				.show();
		intent = new Intent(this, SelectGroup.class);

		startActivity(intent);

	}
}
