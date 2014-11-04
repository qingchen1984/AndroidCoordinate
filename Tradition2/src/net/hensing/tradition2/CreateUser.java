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
import java.util.Scanner;

import net.hensing.tradition2.WelcomeActivity.ClientThread;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.preference.PreferenceManager;

public class CreateUser extends ActionBarActivity {

	SharedPreferences sharedPref;
	SharedPreferences.Editor editor;

	EditText editTextName;
	EditText editTextPassword;
	EditText editTextDisplayName;
	String user;
	Intent intent;
	String is_new_user;
	String Password;
	String DisplayName;
	private boolean isConnected = false;
	private Socket socket;
	private static final int SERVERPORT = 1234;
	//private static final String SERVER_IP = "10.0.2.2";
	private static final String SERVER_IP = "90.226.9.91";	
	DecimalFormat dec = new DecimalFormat("0.0000");
	private String send_message;

	public static final String EXTRA_MESSAGE_USER = "net.hensing.tradition2.MESSAGE_USER";
	public static final String EXTRA_MESSAGE_NEW_USER = "net.hensing.tradition2.MESSAGE_NEW_USER";
	public static final String EXTRA_MESSAGE_PASSWORD = "net.hensing.tradition2.MESSAGE_PASSWORD";

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
				send_message = "CREATE_NEW_USER " + user + " " + DisplayName + " " + Password;
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
			if (scanner.findInLine("CREATE_USER ") != null){

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
			loginSuccess();
		}
	};
	Handler handler_remove = new Handler() {
		@Override
		public void handleMessage(Message msg) {


			Context context = getApplicationContext();
			CharSequence text = "Create user failed";
			int duration = Toast.LENGTH_LONG;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	};		


	public void create_user(View view) {

		editTextName = (EditText) findViewById(R.id.editTextName);
		user = editTextName.getText().toString();
		editTextPassword = (EditText) findViewById(R.id.editTextPassword);
		Password = editTextPassword.getText().toString();
		editTextDisplayName = (EditText) findViewById(R.id.editTextDisplayName);
		DisplayName = editTextDisplayName.getText().toString();

		if (!Password.equals("") && !user.equals("") && !DisplayName.equals("")){
			
			if (!Password.contains(" ") && !user.contains(" ") && !DisplayName.contains(" ")){


				if (user.contains("@") && user.contains(".")){

					new Thread(new ClientThread()).start();
				}
				else {

					Toast.makeText(getApplicationContext(),
							"Invalid email", Toast.LENGTH_LONG)
							.show();

				}
			}
			else {

				Toast.makeText(getApplicationContext(),
						"Space not allowed", Toast.LENGTH_LONG)
						.show();
			}
		}
		else{
			Toast.makeText(getApplicationContext(),
					"Empty field", Toast.LENGTH_LONG)
					.show();
		}

	}


	private void loginSuccess() {
		Toast.makeText(getApplicationContext(),
				"Create user success", Toast.LENGTH_LONG)
				.show();
		intent = new Intent(this, SelectGroup.class);
		intent.putExtra(EXTRA_MESSAGE_USER, user);
		intent.putExtra(EXTRA_MESSAGE_NEW_USER, is_new_user);
		intent.putExtra(EXTRA_MESSAGE_PASSWORD, Password);
		sharedPref =  PreferenceManager.getDefaultSharedPreferences(this);
		editor = sharedPref.edit();
		editor.putString("UserName", user);
		editor.putString("Password", Password);
		editor.commit();
		startActivity(intent);

	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_user);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}
/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_user, menu);
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
*/
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_create_user,
					container, false);
			return rootView;
		}
	}

}
