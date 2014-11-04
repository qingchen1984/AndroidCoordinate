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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.os.Build;

public class MapActivity extends Activity implements
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener,
LocationListener, com.google.android.gms.location.LocationListener{


	private boolean isConnected = false;
	private boolean zoomedToEvent = false;
	private Socket socket;
	private static final int SERVERPORT = 1234;
	//private static final String SERVER_IP = "10.0.2.2";
	private static final String SERVER_IP = "90.226.9.91";	
	DecimalFormat dec = new DecimalFormat("0.0000");


	private ArrayList<Member> userList = new ArrayList<Member>();
	private String send_message;
	private String user = "unRegistred";
	private String group = "GroupNotSet";
	LocationRequest mLocationRequest;
	LocationClient mLocationClient;
	boolean mUpdatesRequested = true;
	public static Context c;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	public static final String EXTRA_MESSAGE_USER = "net.hensing.tradition2.MESSAGE_USER";
	public static final String EXTRA_MESSAGE_GROUP = "net.hensing.tradition2.MESSAGE_GROUP";
	String eventID;
	String event;


	// Google Map
	private GoogleMap googleMap;

	// latitude and longitude slottet
	double latitude;
	double longitude;
	double latitude_slottet = 59.3268;
	double longitude_slottet = 18.0718; 
	double latitude_event;
	double longitude_event;

	// Handle to SharedPreferences for this app
	SharedPreferences mPrefs;

	// Handle to a SharedPreferences editor
	SharedPreferences.Editor mEditor;


	// Milliseconds per second
	private static final int MILLISECONDS_PER_SECOND = 1000;
	// Update frequency in seconds
	public static final int UPDATE_INTERVAL_IN_SECONDS = 30;
	// Update frequency in milliseconds
	private static final long UPDATE_INTERVAL =
			MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
	// The fastest update frequency, in seconds
	private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
	// A fast frequency ceiling in milliseconds
	private static final long FASTEST_INTERVAL =
			MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;



	// create marker
	//MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude_slottet, longitude_slottet)).title("Slottet");

	// adding marker
	class Member {
		String name;
		String id;
		double lat;
		double lon;
		MarkerOptions mark;
		Marker m;

		public Member(String inputName, double inputLat, double inputLon){
			name = inputName;
			lat = inputLat;
			lon = inputLon;
			mark = new MarkerOptions().position(new LatLng(lat, lon)).title(name).icon(BitmapDescriptorFactory.defaultMarker((float) 13));;
			//m = googleMap.addMarker(mark);
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
					parser(line_from_server);
					print(line_from_server);
				}

				handler_add.sendEmptyMessage(0);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				print("ERROR No conn est...");
			}

		}  
	}

	public void parser(String message) {
		//message = "StartMsg 2 namn=simon lat=1 long=1 namn=Pontus lat=2 long=2";
		//use a second Scanner to parse the content of each line 
		Scanner scanner = new Scanner(message);
		//scanner.useDelimiter("=");
		if (scanner.findInLine("POSITIONS ") != null){
			String persons = scanner.next();
			int pers = 0;	    	
			boolean parsable = true;
			try{
				pers = Integer.parseInt(persons);
			}
			catch(NumberFormatException e){
				parsable = false;
				print("ERROR nr of persons NOT integer");
			}

			message = "";
			String temp = "";
			String name = "";
			String lat = "";
			String lon = "";
			String id = "";

			//assumes the line has a certain structure
			for (int i = 1; i<=pers; i++){
				//temp = scanner.findInLine("namn=");
				name = scanner.next();
				//temp = scanner.findInLine("lat=");
				lat = scanner.next();
				Double latDouble = Double.parseDouble(lat); 
				//temp = scanner.findInLine("long=");
				lon = scanner.next();
				Double lonDouble = Double.parseDouble(lon); 
				//temp = scanner.findInLine("id=");
				//id = scanner.next();
				
				if (i == 1 && !zoomedToEvent){
					latitude_event = Double.parseDouble(lat);
					longitude_event = Double.parseDouble(lon);
					handler_zoom.sendEmptyMessage(0);
					zoomedToEvent = true;
				}

				userList.add(new Member(name, latDouble, lonDouble));
			}
		}
	}

	public void print(String message) {

		//Log.d("MyLog ", "message: " + message);

	}

	// Functions--- a lot of them---
	public String GetPhoneId(){
		final String androidId;

		androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		return androidId;

	}
	
	
	public void get_last_location_and_use_once(){
		double[] gps = new double[2];
		gps = getGPS();
		double gps_lat = gps[0];
		double gps_long = gps[1];
		print("prel gps "+String.valueOf(gps_lat)+" "+String.valueOf(gps_long));
		latitude = gps_lat;
		longitude = gps_long;
		whenConnected();

	}
	private double[] getGPS() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);  
		List<String> providers = lm.getProviders(true);

		/* Loop over the array backwards, and if you get an accurate location, then break out the loop*/
		Location l = null;

		for (int i=providers.size()-1; i>=0; i--) {
			l = lm.getLastKnownLocation(providers.get(i));
			if (l != null) break;
		}

		double[] gps = new double[2];
		if (l != null) {
			gps[0] = l.getLatitude();
			gps[1] = l.getLongitude();
		}
		return gps;
	}

	public void whenConnected(){
		
		// FORMAT: "UPDATE_POSITION mail lat long eventID"
		String stringLat = String.valueOf(latitude);
		String stringLong = String.valueOf(longitude);
		
		send_message = "UPDATE_POSITION " +user +" " +stringLat +" " +stringLong +" " + eventID;  
		//send_message = (user+" long "+dec.format(longitude)+" lat "+dec.format(latitude)+" ID "+GetPhoneId() + " GROUP " + group);
		clearUserList();
		new Thread(new ClientThread()).start();


	}

	public void clearUserList(){
		for (int i = 0;  i < userList.size();  i++) {
			Member mbr = (Member)userList.get(i);
			mbr.m.remove();
		}
		userList.clear();
	}

	public void activateUserList(){

		for (int i = 0;  i < userList.size();  i++) {
			Member mbr = (Member)userList.get(i);
			mbr.m = googleMap.addMarker(mbr.mark);
		}
	}

	Handler handler_add = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			activateUserList();
		}
	};

	Handler handler_remove = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			clearUserList();
		}
	};		
	Handler handler_zoom = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			CameraPosition cameraPosition = new CameraPosition.Builder().target(
					new LatLng(latitude_event, longitude_event)).zoom(11).build();

			googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

		}
	};	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		Intent intent = getIntent();
		final String login_user = intent.getStringExtra(SelectEvent.EXTRA_MESSAGE_USER);
		final String login_event = intent.getStringExtra(SelectEvent.EXTRA_MESSAGE_EVENT);
		final String login_group = intent.getStringExtra(SelectEvent.EXTRA_MESSAGE_GROUP);
		user = login_user;
		eventID = login_event;
		group = login_group;
		get_last_location_and_use_once();
		// Open the shared preferences
        mPrefs = getSharedPreferences("SharedPreferences",
                Context.MODE_PRIVATE);
        // Get a SharedPreferences editor
        mEditor = mPrefs.edit();


		mLocationClient = new LocationClient(this, this, this);


		// Create the LocationRequest object
		mLocationRequest = LocationRequest.create();
		// Use high accuracy
		mLocationRequest.setPriority(
				LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the update interval to 30 seconds
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		// Set the fastest update interval to 1 second
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);


		//activateGPS();

		try {
			// Loading map
			initilizeMap();
			//googleMap.addMarker(marker);
			CameraPosition cameraPosition = new CameraPosition.Builder().target(
					new LatLng(latitude_slottet, longitude_slottet)).zoom(11).build();

			googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void onStart() {
		mLocationClient.connect();
		super.onStart();

	}


	/**
	 * function to load map. If map is not created it will create it for you
	 * */
	private void initilizeMap() {
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();


			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mPrefs.contains("KEY_UPDATES_ON")) {
			mUpdatesRequested =
					mPrefs.getBoolean("KEY_UPDATES_ON", false);

			// Otherwise, turn off location updates
		} else {
			mEditor.putBoolean("KEY_UPDATES_ON", false);
			mEditor.commit();
		}


		initilizeMap();
	}

	protected void onDestroy() {
		
		
		super.onDestroy();
		finish();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu){

		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);   
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_settings:

			return true;

		case R.id.id_akt_chat:
			Intent Act_2 = new Intent(MapActivity.this, ChatActivity.class);
			Act_2.putExtra(EXTRA_MESSAGE_USER, user);
			Act_2.putExtra(EXTRA_MESSAGE_GROUP, group);
			startActivity(Act_2);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	@Override
	public void onConnected(Bundle dataBundle) {
		// Display the connection status
		Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		// If already requested, start periodic updates
		print("onConnected");
		//if (mUpdatesRequested) {
		if(true){
			mLocationClient.requestLocationUpdates(mLocationRequest, this);
			print(" in if mUpdatesRequested");
		}
	}

	@Override
	protected void onPause() {
		// Save the current setting for updates
		mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
		mEditor.commit();
		super.onPause();
	}
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects.
		 * If the error has a resolution, try sending an Intent to
		 * start a Google Play services activity that can resolve
		 * error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(
						this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			/*
			 * If no resolution is available, display a dialog to the
			 * user with the error.
			 */
			//showErrorDialog(connectionResult.getErrorCode());
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		print("onLocationChanged activated");
		longitude = location.getLongitude();
		latitude = location.getLatitude();
		whenConnected();

		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		print("GPSEnabled");
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		print("GPSDisabled");
		
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
	@Override
    protected void onStop() {
        // If the client is connected
        if (mLocationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
            mLocationClient.removeLocationUpdates(this);
        }
        /*
         * After disconnect() is called, the client is
         * considered "dead".
         */
        mLocationClient.disconnect();
        super.onStop();
    }


}
