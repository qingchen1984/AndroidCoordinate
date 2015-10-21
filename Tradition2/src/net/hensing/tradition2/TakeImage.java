package net.hensing.tradition2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


public class TakeImage extends Activity {

	String mCurrentPhotoPath, thumb_mCurrentPhotoPath;
	String fileAbsolutPath, thumb_fileAbsolutPath;
	String mFileName, thumb_mFileName;
	static final int REQUEST_TAKE_PHOTO = 1;
	static final int REQUEST_IMAGE_CAPTURE = 1;
	ImageView mImageView;
	Button uploadButton;
	Bitmap scaledBitmap = null;
	String group = null;
	String event = null;
	Handler handler_upload_failed;
	Handler handler_upload_success;




	private static int RESULT_LOAD_IMAGE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_take_image);

		Intent intent = getIntent();
		final String login_event = intent.getStringExtra(SelectEvent.EXTRA_MESSAGE_EVENT);
		final String login_group = intent.getStringExtra(SelectEvent.EXTRA_MESSAGE_GROUP);
		group = login_group;
		event = login_event;

		mImageView = (ImageView) findViewById(R.id.imgView);
		uploadButton = (Button) findViewById(R.id.button1);
		dispatchTakePictureIntent();

		// create the handlers:
		handler_upload_failed = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				uploadButton.setText("Upload Failed");
			}
		};

		handler_upload_success = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				uploadButton.setText("Upload complete");
			}
		};	


	}
	public void startUploadThread(){
		new Thread(new Runnable() {
			public void run() {
				UploadFile();
			}
		}).start();
	}


	private File createImageFile() throws IOException {
		// Create an image file name
		
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		
		String imageFileName = "JPEG_" + timeStamp + "_";
		
		File storageDir = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES);
		
		File image = File.createTempFile(
				imageFileName,  /* prefix */
				".jpg",         /* suffix */
				storageDir      /* directory */
				);
		

		// Save a file: path for use with ACTION_VIEW intents
		mCurrentPhotoPath = "file:" + image.getAbsolutePath();
		fileAbsolutPath = image.getAbsolutePath();
		mFileName = imageFileName+".jpg";
		return image;
	}
	private File createThumbFile(String origFile) throws IOException {
		// Create an image file name
		
		String imageFileName = "thumb_" + origFile.split("\\.")[0];
		File storageDir = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES);

		File image = File.createTempFile(
				imageFileName,  /* prefix */
				".jpg",         /* suffix */
				storageDir      /* directory */
				);

		// Save a file: path for use with ACTION_VIEW intents
		thumb_mCurrentPhotoPath = "file:" + image.getAbsolutePath();
		thumb_fileAbsolutPath = image.getAbsolutePath();
		thumb_mFileName = imageFileName+".jpg";
		return image;
	}


	private void dispatchTakePictureIntent() {
		
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			
			// Create the File where the photo should go
			File photoFile = null;
			try {
				photoFile = createImageFile();
			} catch (IOException ex) {
				// Error occurred while creating the File
				
			}
			// Continue only if the File was successfully created
			if (photoFile != null) {
				
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(photoFile));
				startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
			}
		}
	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

			Bitmap imageBitmap = BitmapFactory.decodeFile(fileAbsolutPath);

			try {
				ExifInterface exif = new ExifInterface(fileAbsolutPath);
				int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
				
				Matrix matrix = new Matrix();
				if (orientation == 6) {
					matrix.postRotate(90);
				}
				else if (orientation == 3) {
					matrix.postRotate(180);
				}
				else if (orientation == 8) {
					matrix.postRotate(270);
				}
				imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true); // rotating bitmap

				FileOutputStream out = null;
				File compressedFile = createImageFile();

				try {
					out = new FileOutputStream(compressedFile);

					//	          write the compressed bitmap at the destination specified by filename.


					

					final int maxSize = 2000;
					int outWidth;
					int outHeight;
					int inWidth = imageBitmap.getWidth();
					int inHeight = imageBitmap.getHeight();
					if(inWidth > inHeight){
						outWidth = maxSize;
						outHeight = (inHeight * maxSize) / inWidth; 
					} else {
						outHeight = maxSize;
						outWidth = (inWidth * maxSize) / inHeight; 
					}

					imageBitmap = Bitmap.createScaledBitmap(imageBitmap, outWidth, outHeight, false);


					imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
					

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			catch (Exception e) {

			}


			//reload to compressed version:
			imageBitmap = BitmapFactory.decodeFile(fileAbsolutPath);
			mImageView.setImageBitmap(imageBitmap);

			//create thumb
			Bitmap thumbBitmap = Bitmap.createScaledBitmap(imageBitmap, 400, 400, true);
			FileOutputStream out2 = null;
			try {
				File compressedFile2 = createThumbFile(mFileName);
				out2 = new FileOutputStream(compressedFile2);
				thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out2);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//enable upload button
			uploadButton.setVisibility(View.VISIBLE);


		}
		
	}

	public void uploadClick(View view){
		uploadButton.setText("Uploading...");
		uploadButton.setEnabled(false);
		startUploadThread();

	}

	public void UploadFile(){
		try {


			// Set your file path here
			
			FileInputStream fstrm = new FileInputStream(fileAbsolutPath);


			if (!(group==null) && !(event==null)){
				// Set your server page url (and the file title/description)
				HttpFileUpload hfu = new HttpFileUpload("http://77.66.108.128/php/upload_image.php", mFileName,group,event,"my file description",handler_upload_failed,handler_upload_success);

				hfu.Send_Now(fstrm);
			}
		} catch (FileNotFoundException e) {
			
			// Error: File not found
		}



		// send thumb:
		try {


			// Set your file path here
			
			FileInputStream fstrm_thumb = new FileInputStream(thumb_fileAbsolutPath);

			if (!(group==null) && !(event==null)){

				// Set your server page url (and the file title/description)
				ThumbHttpFileUpload hfu = new ThumbHttpFileUpload("http://77.66.108.128/php/upload_thumb.php", mFileName, thumb_mFileName,group,event,"my file description",handler_upload_failed,handler_upload_success);

				hfu.Send_Now(fstrm_thumb);
			}
		} catch (FileNotFoundException e) {
			
			// Error: File not found
		}



		handler_upload_success.sendEmptyMessage(0);

	}

	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
	 */
}
