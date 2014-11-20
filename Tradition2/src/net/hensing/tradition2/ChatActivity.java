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
import java.util.UUID;

import net.hensing.tradition2.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
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
	private boolean isConnected = false;
	private Socket socket, socket2;
	private static final int SERVERPORT = 1234;
	//private static final String SERVER_IP = "10.0.2.2";
	private static final String SERVER_IP = "90.226.9.91";	
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
	String response = "";
	ServerDataProvider sdp;




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

	// Print to chat screen function, same base code as in PingActivity.java
	public void print(String message) {

		//Log.d("App ", "message: " + message);
		Time now = new Time();
		now.setToNow();
		String timeString = now.format("%H:%M:%S");
		final String line = message + "\n";
		//using .post function to send back to uiThread
		chat_area.post(new Runnable() {
			public void run() {
				chat_area.setText(chat_area.getText() + line);
				// below is for autoscroll if end of view is reached
				final int scrollAmount = chat_area.getLayout().getLineTop(chat_area.getLineCount()) - chat_area.getHeight();
				// if there is no need to scroll, scrollAmount will be <=0
				if (scrollAmount > 0)
					chat_area.scrollTo(0, scrollAmount);
				else
					chat_area.scrollTo(0, 0);
				chat_area.invalidate();
			}
		});
	}

	// Clearing send message field 
	public void clearChat() {
		sendChat.post(new Runnable() {
			//using .post function to send back to uiThread
			public void run() {
				sendChat.setText("");
				sendChat.invalidate();
			}
		});
	} 
	public void clearScreen() {
		chat_area.post(new Runnable() {
			//using .post function to send back to uiThread
			public void run() {
				chat_area.setText("");
				chat_area.invalidate();
			}
		});
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
		new Thread(new DisplayNameThread()).start();

		createHandlers();
		new Thread(new loopThread()).start();

		// Activating the Buttons on the ui and related functionality

		activateSend();

	}

	private void startMessage(){

		String send_to_server = "GET_CHAT "+group;
		PrintWriter out;

		try {
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
			out.println(send_to_server);
			out.flush();
			clearChat();


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


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
					new Thread(new Runnable(){

						public void run() {
							// TODO Auto-generated method stub
							String send_message = chatEdit.getText().toString();
							String send_to_server;
							send_to_server = "SEND_TO_CHAT " + group + " STARTMSG"
									+ "" + getTime() + displayName + ": " + send_message;
							try {
								InetAddress serverAddress = InetAddress.getByName(SERVER_IP);			
								socket = new Socket();
								socket.connect(new InetSocketAddress(serverAddress, SERVERPORT), 9000);
								isConnected = true;
								PrintWriter out;
								try {
									out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
									out.println(send_to_server);
									out.flush();
									//clearChat();

								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								BufferedReader buf_from_server = new BufferedReader(new InputStreamReader(socket.getInputStream()));
								while(isConnected && socket.isConnected()){
									String line_from_server = buf_from_server.readLine();
									if (line_from_server == null){break;}

									if (line_from_server.contains("I am the Python Pi-Server")){clearScreen();clearChat();}
									else{
										//print(line_from_server);
									}
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


					}).start();
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

	class DisplayNameThread implements Runnable {
		// thread to connect to socket and listen
		// for messages and then print them in chatbox.
		public void run(){
			try {
				send_message_for_display = "GET_MY_DISPLAYNAME " + user;
				InetAddress serverAddress = InetAddress.getByName(SERVER_IP);			
				socket = new Socket();
				socket.connect(new InetSocketAddress(serverAddress, SERVERPORT), 9000);
				isConnected = true;
				print("Connected to server");
				PrintWriter out;

				try {
					out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
					out.println(send_message_for_display);
					out.flush();
					//clearChat();
					print("Jag: " + send_message_for_display);

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
					parser2(line_from_server);
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

		private void parser2(String msg) {


			Scanner scanner = new Scanner(msg);
			//scanner.useDelimiter("=");
			if (scanner.findInLine("DISPLAYNAME ") != null){

				String word;
				word = scanner.next();
				displayName = word;
			}
			scanner.close();
		}
		public void print(String message) {

			//Log.d("MyLog ", "message: " + message);

		}

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

				try {
					Thread.sleep(10000);
				} 
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				}
				loopHandler.sendEmptyMessage(0);
			}
		}

	}
}
