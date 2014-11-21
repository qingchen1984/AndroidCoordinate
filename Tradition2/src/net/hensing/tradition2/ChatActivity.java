package net.hensing.tradition2;

import java.text.DecimalFormat;
import java.util.Scanner;
import net.hensing.tradition2.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends Activity {

	boolean inDisplay;
	private TextView chat_area;
	private EditText sendChat;
	DecimalFormat dec = new DecimalFormat("0.0000");
	private String user = "oregistrerad";
	private String group = "oregistreradGrupp";
	String displayName = "notReady";
	String send_message_for_display;
	Handler ok = null;
	Handler nok = null;
	Handler loopHandler = null;
	Handler displayNameOk = null;
	Handler displayNameNok = null;
	Handler sendChatOk = null;
	Handler sendChatNok = null;
	String response = "";
	ServerDataProvider sdp;
	ServerDataProvider displayNameSdp;
	ServerDataProvider sendChatSdp;

	public String GetPhoneId(){
		final String androidId;

		androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		return androidId;

	}
	public String getTime(){
		Time nowTime = new Time();
		nowTime.setToNow();
		String returnString = nowTime.format("%H:%M:%S")+ " ";
		return returnString;

	}

	// Clearing send message field 
	public void clearChat() {

		sendChat.setText("");
		sendChat.invalidate();

	} 
	public void clearScreen() {

		chat_area.setText("");
		chat_area.invalidate();

	}
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_chat);
	}    
	
	@Override
	protected void onPause() {
		super.onPause();
		inDisplay = false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		inDisplay = true;
		chat_area = (TextView)findViewById(R.id.outputChat);
		chat_area.setMovementMethod(new ScrollingMovementMethod());
		sendChat = (EditText)findViewById(R.id.myChat);
		Intent intent = getIntent();
		final String login_user = intent.getStringExtra(MapActivity.EXTRA_MESSAGE_USER);
		final String login_group = intent.getStringExtra(MapActivity.EXTRA_MESSAGE_GROUP);
		user = login_user;
		group = login_group;
		createHandlers();
		getDisplayName();
		new Thread(new loopThread()).start();
		// Activating the Buttons on the ui and related functionality
		activateSend();
	}

	private void activateSend() {
		// Activate Send Button
		final Button connectButton = (Button) findViewById(R.id.button3);
		final EditText chatEdit   = (EditText)findViewById(R.id.myChat);

		connectButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method
				if (displayName!="notReady"){
					// Create thread that take what is in Send and send over socket
					sendChatMessage();
					connectButton.setEnabled(false);
				}
				else{
					Log.d("qwerty3", "disp name: "+displayName);
				}
			}
		});
	}

	public void getChat(){
		String send_to_server2;
		send_to_server2 = "GET_CHAT " + group;
		sdp = new ServerDataProvider(send_to_server2,nok,ok);
		Thread thread = new Thread(sdp);
		thread.start();	
	}    	

	public void getDisplayName(){
		
		String send_to_server2;
		send_to_server2 = "GET_MY_DISPLAYNAME " + user;
		displayNameSdp = new ServerDataProvider(send_to_server2,displayNameNok,displayNameOk);
		Thread thread = new Thread(displayNameSdp);
		thread.start();		
	}

	public void sendChatMessage(){
		
		EditText chatEdit   = (EditText)findViewById(R.id.myChat);
		String send_message = chatEdit.getText().toString();
		String send_to_server;
		send_to_server = "SEND_TO_CHAT " + group + " STARTMSG"
				+ "" + getTime() + displayName + ": " + send_message;
		sendChatSdp = new ServerDataProvider(send_to_server,sendChatNok,sendChatOk);
		Thread thread = new Thread(sendChatSdp);
		thread.start();		
	}

	public void createHandlers(){
		
		ok = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				response = sdp.getResponse();
				Log.d("qwerty2","in ok handler");
				chatUpdate(response);

			}
		};
		nok = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				showProblemMessage();
			}
		};   	
		loopHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				getChat();
			}
		};
		displayNameOk = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				displayNameParser(displayNameSdp.getResponse());
				Log.d("qwerty3", "disp name in handle: "+displayNameSdp.getResponse());
			}
		};
		displayNameNok = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				showProblemMessage();
			}
		};
		sendChatOk = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String sendChatResponse = sendChatSdp.getResponse();
				chatUpdate(sendChatResponse);
				clearChat();
				Button connectButton = (Button) findViewById(R.id.button3);
				connectButton.setEnabled(true);
			}
		};
		sendChatNok = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				showProblemMessage();
			}
		};
	}

	private void displayNameParser(String msg) {

		Scanner scanner = new Scanner(msg);
		//scanner.useDelimiter("=");
		if (scanner.findInLine("DISPLAYNAME ") != null){

			String word;
			word = scanner.next();
			displayName = word;

			Log.d("qwerty3", "disp name in scanner: "+word);
		}
		scanner.close();
	}

	public void chatUpdate(String response2) {

		Log.d("qwerty2",response2);
		chat_area.setText(response2);
		// below is for autoscroll if end of view is reached
		final int scrollAmount = chat_area.getLayout().getLineTop(chat_area.getLineCount()) - chat_area.getHeight();
		// if there is no need to scroll, scrollAmount will be <=0
		if (scrollAmount > 0)
			chat_area.scrollTo(0, scrollAmount);
		else
			chat_area.scrollTo(0, 0);
		chat_area.invalidate();



	}
	public void showProblemMessage(){
		Toast.makeText(this, "Connection Problem", Toast.LENGTH_LONG).show();
	}

	class loopThread implements Runnable {

		public void run(){
			while (true){
				loopHandler.sendEmptyMessage(0);
				try {
					Thread.sleep(10000);
				} 
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			}
		}

	}
}
