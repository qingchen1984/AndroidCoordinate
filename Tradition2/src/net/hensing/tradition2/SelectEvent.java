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
import java.util.ArrayList;
import java.util.Scanner;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

public class SelectEvent extends ActionBarActivity {

	String group;
	private boolean isConnected = false;
	private Socket socket;
	private static final int SERVERPORT = 1234;
	//private static final String SERVER_IP = "10.0.2.2";
	private static final String SERVER_IP = "90.226.9.91";	
	DecimalFormat dec = new DecimalFormat("0.0000");
	private String send_message, send_message_del;
	private String user;
	int nr_of_events;
	private ArrayList<String> eventList = new ArrayList<String>();
	Intent intent;
	Intent newEventIntent;
	public static final String EXTRA_MESSAGE_EVENT = "net.hensing.tradition2.MESSAGE_EVENT";
	public static final String EXTRA_MESSAGE_USER = "net.hensing.tradition2.MESSAGE_USER";
	public static final String EXTRA_MESSAGE_GROUP = "net.hensing.tradition2.MESSAGE_GROUP";
	
	String response = "";
	ServerDataProvider sdp;
	Handler ok = null;
	Handler nok = null;
	
	String responseDel = "";
	ServerDataProvider sdpDel;
	Handler okDel = null;
	Handler nokDel = null;
	

	public void print(String message) {

		//Log.d("MyLog ", "message: " + message);

	}



	class DeleteThread implements Runnable {
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
				send_message_del = "DELETE_GROUP " + group;
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
	}

	Handler handler_add = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			populateButtons();
		}
	};


	private void populateButtons() {
		TableLayout table = (TableLayout) findViewById(R.id.tableForButtons);

		for (int event = 1; event < nr_of_events; event=event+2){
			final int FINAL_EVENT = event;
			TableRow tableRow = new TableRow(this);
			table.addView(tableRow);
			Button button = new Button(this);
			Drawable myBS = getResources().getDrawable( R.drawable.button_shape );
			//button.setBackground(myBS);
			button.setBackgroundResource(R.drawable.button_shape);
			button.setTextColor(Color.parseColor("#FFFFFF"));
			button.setText(eventList.get(event));
			button.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					gridButtonClicked(FINAL_EVENT);

				}

			});

			tableRow.addView(button);
		}	
		



		//Delete Group
		TableRow tableRowDelete = new TableRow(this);
		table.addView(tableRowDelete);
		Button buttonDelete = new Button(this);
		Drawable myBS = getResources().getDrawable( R.drawable.button_shape_config );
		//buttonDelete.setBackground(myBS);
		buttonDelete.setBackgroundResource(R.drawable.button_shape_config);
		buttonDelete.setTextColor(Color.parseColor("#FFFFFF"));

		buttonDelete.setText("Delete Group");
		buttonDelete.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				deleteGroupButtonClicked();
			}

		});

		tableRowDelete.addView(buttonDelete);

		//View Member list
		TableRow tableRowMemberList = new TableRow(this);
		table.addView(tableRowMemberList);
		Button buttonMemberList = new Button(this);
		//buttonMemberList.setBackground(myBS);
		buttonMemberList.setBackgroundResource(R.drawable.button_shape_config);
		buttonMemberList.setTextColor(Color.parseColor("#FFFFFF"));
		buttonMemberList.setText("View group members");
		buttonMemberList.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				viewMemberButtonClicked();
			}

		});

		tableRowMemberList.addView(buttonMemberList);



		//Add Member
		TableRow tableRowMember = new TableRow(this);
		table.addView(tableRowMember);
		Button buttonMember = new Button(this);
		//buttonMember.setBackground(myBS);
		buttonMember.setBackgroundResource(R.drawable.button_shape_config);
		buttonMember.setTextColor(Color.parseColor("#FFFFFF"));

		buttonMember.setText("Add member to Group");
		buttonMember.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				addMemberButtonClicked();
			}

		});

		tableRowMember.addView(buttonMember);




		// Create NEW Event
		TableRow tableRow = new TableRow(this);
		table.addView(tableRow);
		Button button = new Button(this);
		//button.setBackground(myBS);
		button.setBackgroundResource(R.drawable.button_shape_config);
		button.setTextColor(Color.parseColor("#FFFFFF"));

		button.setText("New Event");
		button.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				newEventButtonClicked();
			}

		});

		tableRow.addView(button);
		
		// View Group Imags
				TableRow tableRowImages = new TableRow(this);
				table.addView(tableRowImages);
				Button buttonImages = new Button(this);
				//button.setBackground(myBS);
				buttonImages.setBackgroundResource(R.drawable.button_shape_config);
				buttonImages.setTextColor(Color.parseColor("#FFFFFF"));

				buttonImages.setText("View group images");
				buttonImages.setOnClickListener(new View.OnClickListener(){

					@Override
					public void onClick(View v) {
						groupImagesButtonClicked();
					}

				});

				tableRowImages.addView(buttonImages);

	}

	protected void groupImagesButtonClicked() {

		//Toast.makeText(this, "Event clicked", Toast.LENGTH_SHORT).show();

		intent = new Intent(this, GroupImages.class);
		intent.putExtra(EXTRA_MESSAGE_GROUP, group);

		startActivity(intent);

	}

	protected void newEventButtonClicked() {

		//Toast.makeText(this, "Event clicked", Toast.LENGTH_SHORT).show();

		intent = new Intent(this, SearchLocation.class);
		intent.putExtra(EXTRA_MESSAGE_GROUP, group);

		startActivity(intent);

	}
	protected void addMemberButtonClicked() {

		//Toast.makeText(this, "Event clicked", Toast.LENGTH_SHORT).show();

		intent = new Intent(this, AddUserToGroup.class);
		intent.putExtra(EXTRA_MESSAGE_GROUP, group);

		startActivity(intent);

	}
	protected void viewMemberButtonClicked(){
		intent = new Intent(this, ViewMember.class);
		intent.putExtra(EXTRA_MESSAGE_GROUP, group);
		startActivity(intent);
		
		

		
	}

	protected void deleteGroupButtonClicked() {
		
		intent = new Intent(this, SelectGroup.class);
		
		new AlertDialog.Builder(this)
	    .setTitle("Delete this group?")
	    .setMessage("Are you sure you want to delete the group "+group+"?")
	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // continue with delete
	        	send_message_del = "DELETE_GROUP " + group;
	        	sdpDel = new ServerDataProvider(send_message_del,nokDel,okDel);
				Thread thread = new Thread(sdpDel);
				thread.start();	
	        	
	    		

	        }
	     })
	    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // do nothing
	        }
	     })
	    .setIcon(android.R.drawable.ic_dialog_alert)
	     .show();

	}


	protected void gridButtonClicked(int event) {
		String eventName = eventList.get(event);
		String eventNr = eventList.get(event-1);		
		//Toast.makeText(this, "Event "+eventName + " eventNr: " + eventNr+ " clicked", Toast.LENGTH_SHORT).show();

		intent = new Intent(this, EventData.class);

		intent.putExtra(EXTRA_MESSAGE_USER, user);
		intent.putExtra(EXTRA_MESSAGE_EVENT, eventNr);
		intent.putExtra(EXTRA_MESSAGE_GROUP, group);
		startActivity(intent);

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
	
	public void startSelectGroup(){
		intent = new Intent(this, SelectGroup.class);
		startActivity(intent);
		
	}
	
	public void createHandlersDel(){
		okDel = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				
				startSelectGroup();
			}
		};
		nokDel = new Handler() {
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


		Scanner scanner = new Scanner(msg);
		//scanner.useDelimiter("=");
		if (scanner.findInLine("MY_EVENTS") != null){

			nr_of_events = msg.split(" ").length -1;
			String word;
			for (int i = 1; i<=nr_of_events; i++){

				word = scanner.next();
				eventList.add(word);

			}
			populateButtons();


		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_event);
		Intent intent = getIntent();
		final String login_group = intent.getStringExtra(SelectGroup.EXTRA_MESSAGE_GROUP);
		final String login_user = intent.getStringExtra(SelectGroup.EXTRA_MESSAGE_USER);
		group = login_group;
		user = login_user;
		
		createHandlers();
		createHandlersDel();

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}
		send_message = "GET_MY_EVENTS " + group;
		
		sdp = new ServerDataProvider(send_message,nok,ok);
		Thread thread = new Thread(sdp);
		thread.start();	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select_event, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_select_event,
					container, false);
			return rootView;
		}
	}

}
