package net.hensing.tradition2;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import android.preference.PreferenceManager;

public class SelectGroup extends ActionBarActivity {

	SharedPreferences sharedPref;

	DecimalFormat dec = new DecimalFormat("0.0000");
	private String send_message;
	private String user;
	int nr_of_groups;
	private ArrayList<String> groupList = new ArrayList<String>();
	Intent intent;
	public static final String EXTRA_MESSAGE_GROUP = "net.hensing.tradition2.MESSAGE_GROUP";
	public static final String EXTRA_MESSAGE_USER = "net.hensing.tradition2.MESSAGE_USER";	

	String response = "";
	ServerDataProvider sdp;
	Handler ok = null;
	Handler nok = null;

	public void print(String message) {
		//Log.d("MyLog ", "message: " + message);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_group);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}

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

			createHandlers();
			send_message = "GET_MY_GROUPS " + user;
			sdp = new ServerDataProvider(send_message,nok,ok);
			Thread thread = new Thread(sdp);
			thread.start();	

			
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
		if (scanner.findInLine("MY_GROUPS ") != null){

			nr_of_groups = msg.split(" ").length -1;
			String word;
			for (int i = 1; i<=nr_of_groups; i++){

				word = scanner.next();
				groupList.add(word);

			}
			populateButtons();


		}
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
