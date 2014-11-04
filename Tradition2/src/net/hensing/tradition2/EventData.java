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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import net.hensing.tradition2.AddUserToGroup.ClientThread;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

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
	
	

	private boolean isConnected = false;
	private Socket socket;
	private static final int SERVERPORT = 1234;
	private static final String SERVER_IP = "90.226.9.91";	
	private String send_message, userToAdd;
	
	
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
		
		new Thread(new ClientThread()).start();
		

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
	
	class ClientThread implements Runnable {
		// thread to connect to socket and listen
		// for messages and then print them in chatbox.
		public void run(){
			try {
				send_message = "GET_EVENT_DETAILS " + eventID;
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
			if (scanner.findInLine("EVENT_DETAILS ") != null){	
				
				eventName = scanner.next();
				eventDate = scanner.next();
				eventTime = scanner.next();
				
				trackingStartDate = scanner.next();
				trackingStartTime = scanner.next();
				trackingEndDate = scanner.next();
				trackingEndTime = scanner.next();
				
				handler_add.sendEmptyMessage(0);	
				
			}
		}
		public void print(String message) {

			//Log.d("MyLog ", "message: " + message);

		}
		
	}  
	
	Handler handler_add = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			addSuccess();
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
	
private void addSuccess() {
		
	TextView mPlace = (TextView) findViewById(R.id.txtDetail);
	mPlace.setText(eventName);
	
	TextView eventStart = (TextView) findViewById(R.id.EventStart);
	eventStart.setText(eventDate+" "+eventTime);
	
	TextView trackStart = (TextView) findViewById(R.id.trackingStart);
	trackStart.setText(trackingStartDate+" "+trackingStartTime);
	
	TextView trackEnd = (TextView) findViewById(R.id.trackingEnd);
	trackEnd.setText(trackingEndDate+" "+trackingEndTime);


	}

private void print(String string) {
	// TODO Auto-generated method stub
	
}

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
	
	
	
	/*
	Toast.makeText(this, "Event clicked", Toast.LENGTH_SHORT).show();
	
	intent = new Intent(this, SearchLocation.class);
	intent.putExtra(EXTRA_MESSAGE_GROUP, group);
	
	startActivity(intent);
	*/
}

}
