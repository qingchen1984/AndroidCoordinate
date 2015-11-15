package net.hensing.tradition2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class EventData extends ActionBarActivity {

	String eventID;
	String user;
	String mUser;
	String group,mMail,mGroup;
	String eventName;
	String eventDate, eventTime;
	String trackingStartDate, trackingStartTime;
	String trackingEndDate, trackingEndTime;
	Date dateStart;
	Date dateEnd;
	
	public static final String EXTRA_MESSAGE_EVENT = "net.hensing.tradition2.MESSAGE_EVENT";
	public static final String EXTRA_MESSAGE_USER = "net.hensing.tradition2.MESSAGE_USER";
	public static final String EXTRA_MESSAGE_GROUP = "net.hensing.tradition2.MESSAGE_GROUP";
	
	


	private String send_message;
	
	String response = "";
	ServerDataProvider sdp;
	Handler ok = null;
	Handler nok = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_data);
		
		Intent intent = getIntent();
		final String login_event = intent.getStringExtra(SelectEvent.EXTRA_MESSAGE_EVENT);
		final String login_user = intent.getStringExtra(SelectEvent.EXTRA_MESSAGE_USER);
		final String login_group = intent.getStringExtra(SelectEvent.EXTRA_MESSAGE_GROUP);
		group = login_group;
		user = login_user;
		eventID = login_event;
		
		createHandlers();
		send_message = "GET_EVENT_DETAILS " + eventID;
		sdp = new ServerDataProvider(send_message,nok,ok);
		Thread thread = new Thread(sdp);
		thread.start();	
		

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.event_data, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_event_data,
					container, false);
			return rootView;
		}
	}
	

	
private void addSuccess() {
		
	TextView mPlace = (TextView) findViewById(R.id.txtDetail);
	mPlace.setText(eventName);
	
	TextView eventStart = (TextView) findViewById(R.id.EventStart);
	eventStart.setText(eventDate+" "+eventTime);
	
	TextView trackStart = (TextView) findViewById(R.id.trackingStart);
	trackStart.setText(trackingStartDate+" "+trackingStartTime);
	
	TextView trackEnd = (TextView) findViewById(R.id.trackingEnd);
	trackEnd.setText(trackingEndDate+" "+trackingEndTime);

	Button map = (Button) findViewById(R.id.GotoMap);
	map.setEnabled(true);
	
	//Button pic = (Button) findViewById(R.id.TakePicture);
	//pic.setEnabled(true);
	
	}

private void print(String string) {
	// TODO Auto-generated method stub
	
}
/*
public void PictureClick(View view) {
	Intent intent;
	
	intent = new Intent(this, TakeImage.class);

	intent.putExtra(EXTRA_MESSAGE_USER, user);
	intent.putExtra(EXTRA_MESSAGE_EVENT, eventID);
	intent.putExtra(EXTRA_MESSAGE_GROUP, group);
	startActivity(intent);
}
	
*/

public void startClick(View view) {
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	try {
		dateStart = sdf.parse(trackingStartDate+" "+trackingStartTime);
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	try {
		dateEnd = sdf.parse(trackingEndDate+" "+trackingEndTime);
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	Date dateNow = new Date();
	
	if (dateNow.after(dateStart) && dateNow.before(dateEnd)){
		Toast.makeText(this, "Valid Time", Toast.LENGTH_SHORT).show();
		
		Intent intent;
		
		intent = new Intent(this, MapActivity.class);

		intent.putExtra(EXTRA_MESSAGE_USER, user);
		intent.putExtra(EXTRA_MESSAGE_EVENT, eventID);
		intent.putExtra(EXTRA_MESSAGE_GROUP, group);
		startActivity(intent);
		
		
	}
	else{
		Toast.makeText(this, "Event not ongoing", Toast.LENGTH_SHORT).show();
	}
	

}

public void createHandlers(){
	ok = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String message = (String) msg.obj; //Extract the string from the Message
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

private void parser(String msg) {


	//Log.d("qwerty", msg);
	Scanner scanner = new Scanner(msg);
	//scanner.useDelimiter("=");
	if (scanner.findInLine("EVENT_DETAILS ") != null){	
		
		eventName = scanner.next();
		eventDate = scanner.next();
		eventTime = scanner.next();
		
		trackingStartDate = scanner.next();
		trackingStartTime = scanner.next();
		trackingEndDate = scanner.next();
		trackingEndTime = scanner.next();
		
		addSuccess();	
		
	}
}


}
