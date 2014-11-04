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
import java.util.Scanner;

import net.hensing.tradition2.CreateNewGroup.ClientThread;
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
import android.widget.Toast;
import android.os.Build;

public class AddUserToGroup extends ActionBarActivity {
	Intent intent;
	String mUser;
	String group,mMail,mGroup;

	private boolean isConnected = false;
	private Socket socket;
	private static final int SERVERPORT = 1234;
	private static final String SERVER_IP = "90.226.9.91";	
	private String send_message, userToAdd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_user_to_group);
		
		intent = getIntent();

		final String temp_group = intent.getStringExtra(CreateNewGroup.EXTRA_MESSAGE_GROUP);
		mGroup = temp_group;
		
		if (mGroup == null){
			final String temp_group2 = intent.getStringExtra(CreateNewGroup.EXTRA_MESSAGE_GROUP);
			mGroup = temp_group2;
		}
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_user_to_group, menu);
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
					R.layout.fragment_add_user_to_group, container, false);
			return rootView;
		}
	}
	
	public void toGroups(View view){
		intent = new Intent(this, SelectGroup.class);

		startActivity(intent);
	}
	
	public void addClick(View view) {

		EditText mUser = (EditText) findViewById(R.id.txtUser);
		
		userToAdd = mUser.getText().toString(); 

		if (!userToAdd.equals("") && !userToAdd.contains(" ")){

			send_message = "ADD_USER_TO_GROUP " + mGroup + " " + userToAdd;



			//CREATE_GROUP GroupName CreatorsMail
			new Thread(new ClientThread()).start();
		}
		else {

			Toast.makeText(getApplicationContext(),
					"Invalid User", Toast.LENGTH_LONG)
					.show();

		}
		mUser.setText("");
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
			if (scanner.findInLine("ADD_USER ") != null){
				
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
		
		Toast.makeText(getApplicationContext(),
				"User "+userToAdd+" added !", Toast.LENGTH_LONG)
				.show();
		//intent = new Intent(this, AddUserToGroup.class);
		//intent.putExtra(EXTRA_MESSAGE_GROUP, newGroupName);
		
		//startActivity(intent);
		
		

	}

private void print(String string) {
	// TODO Auto-generated method stub
	
}

}
