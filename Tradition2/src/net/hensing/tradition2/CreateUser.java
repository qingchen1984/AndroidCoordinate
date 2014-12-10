package net.hensing.tradition2;

import java.text.DecimalFormat;
import java.util.Scanner;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
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

	DecimalFormat dec = new DecimalFormat("0.0000");
	private String send_message;

	public static final String EXTRA_MESSAGE_USER = "net.hensing.tradition2.MESSAGE_USER";
	public static final String EXTRA_MESSAGE_NEW_USER = "net.hensing.tradition2.MESSAGE_NEW_USER";
	public static final String EXTRA_MESSAGE_PASSWORD = "net.hensing.tradition2.MESSAGE_PASSWORD";

	String response = "";
	ServerDataProvider sdp;
	Handler ok = null;
	Handler nok = null;

	public void print(String message) {

		//Log.d("MyLog ", "message: " + message);

	}

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

					send_message = "CREATE_NEW_USER " + user + " " + DisplayName + " " + Password;

					sdp = new ServerDataProvider(send_message,nok,ok);
					Thread thread = new Thread(sdp);
					thread.start();	


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
		
		createHandlers();

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

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


		Scanner scanner = new Scanner(msg);
		//scanner.useDelimiter("=");
		if (scanner.findInLine("CREATE_USER ") != null){

			String word;
			word = scanner.next();
			if (word.equals("OK")){
				loginSuccess();	
			}
			else {
				CharSequence text = "Create user failed";
				int duration = Toast.LENGTH_LONG;

				Toast toast = Toast.makeText(this, text, duration);
				toast.show();
			}


		}
	}


}
