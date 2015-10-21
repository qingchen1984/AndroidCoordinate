package net.hensing.tradition2;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;


public class GroupImages extends Activity {

	JSONArray myJSONArray;
	int jsonIndex = 0;
	Bitmap gBitMap = null;
	Handler pathHandler;
	Handler bitmapHandler;
	NetworkImage myNetworkImage;
	Paths myPaths;
	String urlToPaths = "http://77.66.108.128/php/get_image_paths.php";
	String urlToImages = "http://77.66.108.128/php/download_image.php";
	String group = "testAppGroup";
	int jsonTotal;
	TableRow tableRow = null;
	public static final String EXTRA_MESSAGE_PATH = "net.hensing.allpictures.MESSAGE_PATH";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_images);
		
		Intent intent = getIntent();
		final String login_group = intent.getStringExtra(SelectGroup.EXTRA_MESSAGE_GROUP);
		group = login_group;

		
		
		makeHandlers();
		myPaths = new Paths(urlToPaths,group,pathHandler);
		Thread thread = new Thread(myPaths);
		thread.start();
	}

	private void makeHandlers() {
		pathHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				getPathsReady();
			}
		};

		bitmapHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				addBitMap();
			}
		};		

	}

	// This function is called when the thread to get picture is ready.
	@SuppressLint("NewApi") //the bitmap is fetched from the global instance myGlobalImage. 
	//if there are more pictures in the Jsonarray, a new thread is started to get the image from Internet.
	@SuppressWarnings("deprecation")
	protected void addBitMap() {

		//fetch the table and create new row if there are zero or multiple of three images already.
		TableLayout table = (TableLayout) findViewById(R.id.tableForPictures);
		if (jsonIndex % 3 == 0){
			tableRow = new TableRow(this);
			table.addView(tableRow);
		}

		//Get displaysize and make thumbnail slightly less than a third of that.
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		
		int width = 0; 
		//int height = 0;
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			display.getSize(size);
			width = size.x;
		//	height = size.y;
		}
		else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

			width=display.getWidth();
		//	height=display.getHeight();
		}
		else {
		    //Should not happen
		}
		double ImagesInRow = 3.3;
		int h = (int) (width/ImagesInRow); // Height in pixels
		int w = (int) (width/ImagesInRow); // Width in pixels  


		//create view, get bitmap from global instance, round corners and set view and add to table.
		ImageView iv = new ImageView(this);		
		Bitmap mBitmap = myNetworkImage.getBitmap();
		mBitmap = Bitmap.createScaledBitmap(mBitmap, h, w, true);
		mBitmap = ImageRounder.getRoundedCornerBitmap(mBitmap, 20);
		iv.setImageBitmap(mBitmap);
		iv.setPadding(0, 10, 0, 10);
		tableRow.addView(iv);


		//get the full image path, create onclick listener
		String lastPath = null;
		try {
			JSONObject mkeyValuePair = myJSONArray.getJSONObject(jsonIndex);
			lastPath = mkeyValuePair.getString("ImagePath");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final String FINAL_PATH = lastPath;
		iv.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				listImageClicked(FINAL_PATH);

			}

		});

		//if there are more images in the jsonarray, get next in the thread.
		JSONObject keyValuePair;
		String picturePath = null;
		jsonIndex++;
		if(jsonIndex<jsonTotal){
			try {
				keyValuePair = myJSONArray.getJSONObject(jsonIndex);
				picturePath = keyValuePair.getString("ThumbPath");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			myNetworkImage = new NetworkImage(urlToImages,picturePath,bitmapHandler);
			Thread thread = new Thread(myNetworkImage);
			thread.start();
		}
	}

	//Start activity to show big image.
	protected void listImageClicked(String path) {
		Intent intent = new Intent(this, Enlarge.class);
		intent.putExtra(EXTRA_MESSAGE_PATH, path);
		startActivity(intent);
	}

	// when json with paths exist, start the thread that get an image from path.
	protected void getPathsReady() {
		
		myJSONArray = myPaths.getJsonArray();
		jsonTotal = myJSONArray.length();
		String picturePath = null;
		if(jsonIndex<jsonTotal){
			try {
				JSONObject keyValuePair = myJSONArray.getJSONObject(jsonIndex);
				picturePath = keyValuePair.getString("ThumbPath");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			myNetworkImage = new NetworkImage(urlToImages,picturePath,bitmapHandler);
			Thread thread = new Thread(myNetworkImage);
			thread.start();

		}
		else{
			// What if no pics
			Toast.makeText(this, "There are no images for the group" , Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.group_images, menu);
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
}
