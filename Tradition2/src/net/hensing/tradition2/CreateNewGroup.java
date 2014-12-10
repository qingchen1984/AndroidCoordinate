package net.hensing.tradition2;

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
import android.widget.EditText;
import android.widget.Toast;

public class CreateNewGroup extends ActionBarActivity {

	Intent intent;
	String group,mMail;
	private String send_message, newGroupName;
	public static final String EXTRA_MESSAGE_GROUP = "net.hensing.tradition2.MESSAGE_GROUP";
	
	String response = "";
	ServerDataProvider sdp;
	Handler ok = null;
	Handler nok = null;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_new_group);
		
		intent = getIntent();
		//intent2 = getIntent();

		final String temp_mail = intent.getStringExtra(SelectGroup.EXTRA_MESSAGE_USER);
		mMail = temp_mail;
		
		createHandlers();

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
			sdp = new ServerDataProvider(send_message,nok,ok);
			Thread thread = new Thread(sdp);
			thread.start();	
		}
		else {

			Toast.makeText(getApplicationContext(),
					"Invalid Name", Toast.LENGTH_LONG)
					.show();

		}
	}

	
private void groupSuccess() {
		
		Toast.makeText(getApplicationContext(),
				"Group "+newGroupName+" created!", Toast.LENGTH_LONG)
				.show();
		intent = new Intent(this, AddUserToGroup.class);
		intent.putExtra(EXTRA_MESSAGE_GROUP, newGroupName);
		
		startActivity(intent);
		
		

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

public void showProblemMessage(){
	Toast.makeText(this, "Connection Problem", Toast.LENGTH_LONG).show();
}	

private void parser(String msg) {


	Scanner scanner = new Scanner(msg);
	//scanner.useDelimiter("=");
	if (scanner.findInLine("CREATE_GROUP ") != null){

		String word;
		word = scanner.next();
		if (word.equals("OK")){
			groupSuccess();
		}
		else {
			CharSequence text = "Invalid creation!";
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(this, text, duration);
			toast.show();


		}
	}
}

}
