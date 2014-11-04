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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class LocationService extends Service 
implements
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener,
LocationListener, com.google.android.gms.location.LocationListener
{
	

	private boolean isConnected = false;
	private boolean zoomedToEvent = false;
	private Socket socket;
	private static final int SERVERPORT = 1234;
	//private static final String SERVER_IP = "10.0.2.2";
	private static final String SERVER_IP = "90.226.9.91";	
	DecimalFormat dec = new DecimalFormat("0.0000");

	private String send_message;
	LocationRequest mLocationRequest;
	LocationClient mLocationClient;
	boolean mUpdatesRequested = true;
	public static Context c;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	public static final String EXTRA_MESSAGE_USER = "net.hensing.tradition2.MESSAGE_USER";
	public static final String EXTRA_MESSAGE_GROUP = "net.hensing.tradition2.MESSAGE_GROUP";
	String eventID;
	String event;
	String user;
	boolean firstTimeAccess;
	SharedPreferences sharedPref2;



	// latitude and longitude slottet
	double latitude;
	double longitude;
	double latitude_slottet = 59.3268;
	double longitude_slottet = 18.0718; 


	// Milliseconds per second
	private static final int MILLISECONDS_PER_SECOND = 1000;
	// Update frequency in seconds
	public static final int UPDATE_INTERVAL_IN_SECONDS = 60;
	// Update frequency in milliseconds
	private static final long UPDATE_INTERVAL =
			MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
	// The fastest update frequency, in seconds
	private static final int FASTEST_INTERVAL_IN_SECONDS = 10;
	// A fast frequency ceiling in milliseconds
	private static final long FASTEST_INTERVAL =
			MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;


	SharedPreferences sharedPref;
	
	int mStartMode;       // indicates how to behave if the service is killed
    IBinder mBinder;      // interface for clients that bind
    boolean mAllowRebind; // indicates whether onRebind should be used

    @Override
    public void onCreate() {
        // The service is being created
    	//Toast.makeText(this, "onCreate - lets start thread", Toast.LENGTH_SHORT).show();
    	firstTimeAccess = true;
    	
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

    	
    	
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
    	
    	//Toast.makeText(this, "firstTimeAccess = "+firstTimeAccess, Toast.LENGTH_SHORT).show();


		
		
		if (firstTimeAccess){
			
			sharedPref2 = PreferenceManager.getDefaultSharedPreferences(this);
			String savedUser = sharedPref2.getString("UserName", "");
			
			final String login_user = savedUser;
			user = login_user;
			if (user==null){
				user = "null";
			}
			mLocationClient.connect();
			firstTimeAccess = false;
		}
		
    	
    	// If we get killed, after returning from here, restart
        return START_STICKY;


    }
    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return mAllowRebind;
    }
    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }
    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
    	//Toast.makeText(this, "service done - onDestroy", Toast.LENGTH_SHORT).show();
    }
	@Override
	public void onConnected(Bundle dataBundle) {
		// Display the connection status
		//Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		// If already requested, start periodic updates
		print("onConnected");
		//if (mUpdatesRequested) {
		if(true){
			mLocationClient.requestLocationUpdates(mLocationRequest, this);
			print(" in if mUpdatesRequested");
		}
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
	public void print(String message) {

		//Log.d("MyLog ", "message: " + message);

	}
	
	public void whenConnected(){
		// FORMAT: "UPDATE_POSITION mail lat long eventID"
		String stringLat = String.valueOf(latitude);
		String stringLong = String.valueOf(longitude);
		
		send_message = "UPDATE_MY_POSITION " +user +" " +stringLat +" " +stringLong;  
		//send_message = (user+" long "+dec.format(longitude)+" lat "+dec.format(latitude)+" ID "+GetPhoneId() + " GROUP " + group);

		new Thread(new ClientThread()).start();
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
					//parser(line_from_server);
					print(line_from_server);
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
	}
}

