package net.hensing.tradition2;

import java.io.IOException;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class SearchLocation extends FragmentActivity implements
OnMarkerClickListener{
	
	GoogleMap googleMap;
	MarkerOptions markerOptions;
	LatLng latLng;
	Intent intent;
	SharedPreferences sharedPref;
	SharedPreferences.Editor editor;
	String group;
	
	
	public static final String EXTRA_MESSAGE_LAT = "net.hensing.tradition2.MESSAGE_LAT";
	public static final String EXTRA_MESSAGE_LON = "net.hensing.tradition2.MESSAGE_LON";
	public static final String EXTRA_MESSAGE_GROUP = "net.hensing.tradition2.MESSAGE_GROUP";
	
	@Override
	public boolean onMarkerClick(Marker marker) {

		
		/*
		 * intent = new Intent(this, SelectEvent.class);

		intent.putExtra(EXTRA_MESSAGE_GROUP, groupName);
		intent.putExtra(EXTRA_MESSAGE_USER, user);

		*/
		
		LatLng pos = marker.getPosition();
		double lat = pos.latitude;
		double lng = pos.longitude;
		//Toast.makeText(getApplicationContext(),
		//		"Lat : " +lat + " Lng : " + lng, Toast.LENGTH_LONG)
		//		.show();
		intent = new Intent(this, CreateNewEvent.class);
		intent.putExtra(EXTRA_MESSAGE_LAT, String.valueOf(lat));
		intent.putExtra(EXTRA_MESSAGE_LON, String.valueOf(lng));
		intent.putExtra(EXTRA_MESSAGE_GROUP, group);
		
		//editor = sharedPref.edit();
		//editor.putString("Longitude", String.valueOf(lng));
		//editor.putString("Latitude", String.valueOf(lat));
		//		editor.commit();
		startActivity(intent);
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_location);
		
		Intent intent = getIntent();
		
		final String login_group = intent.getStringExtra(SelectEvent.EXTRA_MESSAGE_GROUP);
		group = login_group;
		
		SupportMapFragment supportMapFragment = (SupportMapFragment) 
				getSupportFragmentManager().findFragmentById(R.id.map);

		// Getting a reference to the map
		googleMap = supportMapFragment.getMap();
		googleMap.setOnMarkerClickListener(this);
		
		// Getting reference to btn_find of the layout activity_main
        Button btn_find = (Button) findViewById(R.id.btn_find);
        
        // Defining button click event listener for the find button
        btn_find.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				// Getting reference to EditText to get the user input location
				EditText etLocation = (EditText) findViewById(R.id.et_location);
				
				// Getting user input location
				String location = etLocation.getText().toString();
				
				if(location!=null && !location.equals("")){
					new GeocoderTask().execute(location);
				}
			}
		}
		);
		// Setting button click event listener for the find button
				
		
		
	}
/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_location, menu);
		return true;
	}
	*/
	
	// An AsyncTask class for accessing the GeoCoding Web Service
		private class GeocoderTask extends AsyncTask<String, Void, List<Address>>{

			@Override
			protected List<Address> doInBackground(String... locationName) {
				// Creating an instance of Geocoder class
				Geocoder geocoder = new Geocoder(getBaseContext());
				List<Address> addresses = null;
				
				try {
					// Getting a maximum of 3 Address that matches the input text
					addresses = geocoder.getFromLocationName(locationName[0], 3);
				} catch (IOException e) {
					e.printStackTrace();
				}			
				return addresses;
			}
			
			
			
			@Override
			protected void onPostExecute(List<Address> addresses) {			
		        
		        if(addresses==null || addresses.size()==0){
					Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
				}
		        
		        // Clears all the existing markers on the map
		        googleMap.clear();
				
		        // Adding Markers on Google Map for each matching address
				for(int i=0;i<addresses.size();i++){				
					
					Address address = (Address) addresses.get(i);
					
			        // Creating an instance of GeoPoint, to display in Google Map
			        latLng = new LatLng(address.getLatitude(), address.getLongitude());
			        
			        String addressText = String.format("%s, %s",
	                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
	                        address.getCountryName());

			        markerOptions = new MarkerOptions();
			        markerOptions.position(latLng);
			        markerOptions.title(addressText);
			        markerOptions.icon(BitmapDescriptorFactory.defaultMarker((float) 13));

			        googleMap.addMarker(markerOptions);
			        
			        
			        
			        // Locate the first location
			        if(i==0)			        	
						googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng)); 	
				}
				
				
			}		
		}
}
