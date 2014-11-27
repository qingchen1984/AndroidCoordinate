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

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

public class CreateNewGroup extends ActionBarActivity {

	Intent intent;
	String group,mMail;

	private boolean isConnected = false;
	private Socket socket;
	private static final int SERVERPORT = 1234;
	private static final String SERVER_IP = "90.226.9.91";	
	private String send_message, newGroupName;
	public static final String EXTRA_MESSAGE_GROUP = "net.hensing.tradition2.MESSAGE_GROUP";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_new_group);
		
		intent = getIntent();
		//intent2 = getIntent();

		final String temp_mail = intent.getStringExtra(SelectGroup.EXTRA_MESSAGE_USER);
		mMail = temp_mail;

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_new_group, menu);
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
					R.layout.fragment_create_new_group, container, false);
			return rootView;
		}
	}
	
	public void createClick(View view) {

		EditText mGroup = (EditText) findViewById(R.id.txtGroup);
		
		newGroupName = mGroup.getText().toString(); 

		if (!newGroupName.equals("") && !newGroupName.contains(" ")){

			send_message = "CREATE_GROUP " + newGroupName + " " + mMail;



			//CREATE_GROUP GroupName CreatorsMail
			new Thread(new ClientThread()).start();
		}
		else {

			Toast.makeText(getApplicationContext(),
					"Invalid Name", Toast.LENGTH_LONG)
					.show();

		}
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
			if (scanner.findInLine("CREATE_GROUP ") != null){
				
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
			groupSuccess();
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
	
private void groupSuccess() {
		
		Toast.makeText(getApplicationContext(),
				"Group "+newGroupName+" created!", Toast.LENGTH_LONG)
				.show();
		intent = new Intent(this, AddUserToGroup.class);
		intent.putExtra(EXTRA_MESSAGE_GROUP, newGroupName);
		
		startActivity(intent);
		
		

	}

private void print(String string) {
	// TODO Auto-generated method stub
	
}

}
