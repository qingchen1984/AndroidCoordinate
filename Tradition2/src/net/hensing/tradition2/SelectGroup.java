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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import android.os.Build;
import android.preference.PreferenceManager;

public class SelectGroup extends ActionBarActivity {
	
	SharedPreferences sharedPref;
	
	private boolean isConnected = false;
	private Socket socket;
	private static final int SERVERPORT = 1234;
	//private static final String SERVER_IP = "10.0.2.2";
	private static final String SERVER_IP = "90.226.9.91";	
	DecimalFormat dec = new DecimalFormat("0.0000");
	private String send_message;
	private String user;
	int nr_of_groups;
	private ArrayList<String> groupList = new ArrayList<String>();
	Intent intent;
	public static final String EXTRA_MESSAGE_GROUP = "net.hensing.tradition2.MESSAGE_GROUP";
	public static final String EXTRA_MESSAGE_USER = "net.hensing.tradition2.MESSAGE_USER";	
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
				send_message = "GET_MY_GROUPS " + user;
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
			if (scanner.findInLine("MY_GROUPS ") != null){
				
				nr_of_groups = msg.split(" ").length -1;
				String word;
				for (int i = 1; i<=nr_of_groups; i++){
					
					word = scanner.next();
					groupList.add(word);

				}
				handler_add.sendEmptyMessage(0);

				
			}
		}  
	}
	Handler handler_add = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			populateButtons();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_group);
		
/*
		
		Intent intent = getIntent();
		final String login_user = intent.getStringExtra(WelcomeActivity.EXTRA_MESSAGE_USER);
		user = login_user;
		
		Intent service_intent = new Intent(this, LocationService.class);
		service_intent.putExtra(EXTRA_MESSAGE_USER, user);
		startService(service_intent);

*/
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		//new Thread(new ClientThread()).start();
	}
	
	public void onResume() {
        super.onResume();
		sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		sharedPref =  PreferenceManager.getDefaultSharedPreferences(this);
		user = sharedPref.getString("UserName", "");
		String savedPassword = sharedPref.getString("Password", "");
		
		
		if(user.equals("")){
			Intent intent;
			intent = new Intent(this, CreateOrLogin.class);
			startActivity(intent);	
			

		}
		else{
			Intent service_intent = new Intent(this, LocationService.class);
			service_intent.putExtra(EXTRA_MESSAGE_USER, user);
			startService(service_intent);

			new Thread(new ClientThread()).start();		
		}        
    }

	private void populateButtons() {
		TableLayout table = (TableLayout) findViewById(R.id.tableForButtons);
		
		table.removeAllViews();
		
		
		for (int group = 0; group < nr_of_groups; group++){
			final int FINAL_GROUP = group;
			TableRow tableRow = new TableRow(this);
			table.addView(tableRow);
			Button button = new Button(this);
			Drawable myBS = getResources().getDrawable( R.drawable.button_shape );
			//button.setBackground(myBS);
			button.setBackgroundResource(R.drawable.button_shape);
			button.setTextColor(Color.parseColor("#FFFFFF"));
			button.setText(groupList.get(group));
			button.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					gridButtonClicked(FINAL_GROUP);
					
				}
				
			});
			
			
			tableRow.addView(button);
		}	
		TableRow tableRow = new TableRow(this);
		table.addView(tableRow);
		Button button = new Button(this);
		
		button.setText("New Group");
		Drawable myBS = getResources().getDrawable( R.drawable.button_shape_config );
		//button.setBackground(myBS);
		button.setBackgroundResource(R.drawable.button_shape_config);
		button.setTextColor(Color.parseColor("#FFFFFF"));
		//button.getBackground().setAlpha(180);  // 25% transparent
		button.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				newGroupButtonClicked();
			}
			
		});
		
		tableRow.addView(button);
	}
protected void newGroupButtonClicked() {
		
		//Toast.makeText(this, "New Group clicked", Toast.LENGTH_SHORT).show();
		
		intent = new Intent(this, CreateNewGroup.class);
		intent.putExtra(EXTRA_MESSAGE_USER, user);
		
		startActivity(intent);
		
	}

	protected void gridButtonClicked(int group) {
		String groupName = groupList.get(group);
		//Toast.makeText(this, ""+groupName + " clicked", Toast.LENGTH_SHORT).show();
		
		intent = new Intent(this, SelectEvent.class);

		intent.putExtra(EXTRA_MESSAGE_GROUP, groupName);
		intent.putExtra(EXTRA_MESSAGE_USER, user);

		startActivity(intent);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select_group, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_select_group,
					container, false);
			
			return rootView;
		}
	}
	

}
