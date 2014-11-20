package net.hensing.tradition2;

import java.util.Scanner;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
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

public class AddUserToGroup extends ActionBarActivity {
	Intent intent;
	String mUser;
	String group,mMail,mGroup;

	private String send_message, userToAdd;

	String response = "";
	ServerDataProvider sdp;
	Handler ok = null;
	Handler nok = null;

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

		createHandlers();

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

	public void createHandlers(){
		ok = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				response = sdp.getResponse();
				parser(response);

			}
		};
		nok = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				showProblemMessage();
			}
		};   	

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
			sdp = new ServerDataProvider(send_message,nok,ok);
			Thread thread = new Thread(sdp);
			thread.start();		}
		else {

			Toast.makeText(getApplicationContext(),
					"Invalid User", Toast.LENGTH_LONG)
					.show();

		}
		mUser.setText("");
	}


	private void parser(String msg) {
		
		Scanner scanner = new Scanner(msg);
		//scanner.useDelimiter("=");
		if (scanner.findInLine("ADD_USER ") != null){

			String word;
			word = scanner.next();
			if (word.equals("OK")){
				addSuccess();	
			}
			else {
				addFailed();
			}
		}
		else {
			addFailed();
		}
	}
	public void print(String message) {

		//Log.d("MyLog ", "message: " + message);

	}

	public void addFailed() {

		Toast.makeText(this, "Failed to add user", Toast.LENGTH_LONG).show();
	}

	private void addSuccess() {

		Toast.makeText(this,
				"User "+userToAdd+" added !", Toast.LENGTH_LONG)
				.show();
		//intent = new Intent(this, AddUserToGroup.class);
		//intent.putExtra(EXTRA_MESSAGE_GROUP, newGroupName);

		//startActivity(intent);

	}

	public void showProblemMessage(){
		Toast.makeText(this, "Connection Problem", Toast.LENGTH_LONG).show();
	}

}
