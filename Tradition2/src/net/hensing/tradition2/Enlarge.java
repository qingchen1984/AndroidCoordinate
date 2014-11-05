package net.hensing.tradition2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class Enlarge extends Activity {

	String path;
	Handler bitmapHandler;
	NetworkImage myNetworkImage;
	String urlToPaths = "http://90.226.9.91/php/get_image_paths.php";
	String urlToImages = "http://90.226.9.91/php/download_image.php";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enlarge);
		Intent intent = getIntent();
		final String mPath = intent.getStringExtra(GroupImages.EXTRA_MESSAGE_PATH);
		path = mPath;
		displayInfo(path);
		makeHandlers();
		myNetworkImage = new NetworkImage(urlToImages,path,bitmapHandler);
		Thread thread = new Thread(myNetworkImage);
        thread.start();
	}
    private void displayInfo(String filePath) {
    	
    	String[] pathArray = filePath.split("/");
    	String eventName = pathArray[4];
    	String fileName = pathArray[5];
    	String pictureDate = fileName.split("_")[1];
    	
    	Toast.makeText(this, "Date: " + pictureDate , Toast.LENGTH_LONG).show();
		
	}
	private void makeHandlers() {

    	bitmapHandler = new Handler() {
    		@Override
    		public void handleMessage(Message msg) {
    			showBitmap();
    		}
    	};		
		
	}
    

	protected void showBitmap() {
		// TODO Auto-generated method stub
		
		
		
		ImageView iv = (ImageView) findViewById(R.id.imageView1);
		Bitmap mBitmap = myNetworkImage.getBitmap();
		
		
		
		Matrix matrix = new Matrix();
		if (mBitmap.getWidth()> mBitmap.getHeight()) {
			matrix.postRotate(90);
		}

		mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true); // rotating bitmap
		iv.setImageBitmap(mBitmap);
		
		
	}
	public void imageClicked(View v) {
		
		displayInfo(path);
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.enlarge, menu);
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
