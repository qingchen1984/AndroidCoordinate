package net.hensing.tradition2;

import java.text.DecimalFormat;
import java.util.ArrayList;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class ViewMember extends ActionBarActivity {
	
	String group;
	int nr_of_members;

	DecimalFormat dec = new DecimalFormat("0.0000");
	private String send_message;
	private ArrayList<String> memberList = new ArrayList<String>();
	
	String response = "";
	ServerDataProvider sdp;
	Handler ok = null;
	Handler nok = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_member);
		
		createHandlers();

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}
	
	public void onResume() {
        super.onResume();
        Intent intent = getIntent();
		final String login_group = intent.getStringExtra(SelectEvent.EXTRA_MESSAGE_GROUP);
		group = login_group;
		print(group);
		send_message = "GET_GROUP_MEMBERS " + group;

		sdp = new ServerDataProvider(send_message,nok,ok);
		Thread thread = new Thread(sdp);
		thread.start();	
        
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_member, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_view_member,
					container, false);
			return rootView;
		}
	}
	public void print(String message) {

		//Log.d("MyLog ", "message: " + message);

	}
	
	private void populateButtons() {
		TableLayout table = (TableLayout) findViewById(R.id.tableForMembers);

		for (int member = 0; member < nr_of_members; member++){
			TableRow tableRow = new TableRow(this);
			table.addView(tableRow);
			TextView textview = new TextView(this);
			textview.setTextSize(20);
			textview.setText(memberList.get(member));


			tableRow.addView(textview);
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
		if (scanner.findInLine("MEMBERS ") != null){

			nr_of_members = msg.split(" ").length -1;
			String word;
			for (int i = 1; i<=nr_of_members; i++){

				word = scanner.next();
				memberList.add(word);

			}
			populateButtons();


		}
		scanner.close();
	}  

}
