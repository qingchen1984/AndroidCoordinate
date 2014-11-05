package net.hensing.tradition2;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.util.ByteArrayBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

public class NetworkImage implements Runnable {
	URL connectURL;
	String responseString;
	String Title;
	byte[ ] dataToServer;
	String paramName1 = "group";
	String paramValue1;
	Handler myHandler;
	Bitmap myBitmap;

	NetworkImage(String url, String imagePath, Handler h){

		try{
			connectURL = new URL(url);
			paramValue1 = imagePath;
			myHandler = h;
		}catch(Exception ex){
			//Log.i("Httpimagelist","URL Malformatted");
		}
	}

	@Override
	public void run() {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try{
			// Open a HTTP connection to the URL
			HttpURLConnection conn = (HttpURLConnection)connectURL.openConnection();

			// Allow Inputs
			conn.setDoInput(true);

			// Allow Outputs
			conn.setDoOutput(true);

			// Don't use a cached copy.
			conn.setUseCaches(false);

			// Use a post method.
			conn.setRequestMethod("POST");

			conn.setRequestProperty("Connection", "Keep-Alive");

			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

			String urlParameters = "ImagePath="+paramValue1;

			dos.writeBytes(urlParameters);

			dos.flush();
			dos.close();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is,1024);
			ByteArrayBuffer baf = new ByteArrayBuffer(1024);
			//get the bytes one by one
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}
			//convert it back to an image
			ByteArrayInputStream imageStream = new ByteArrayInputStream(baf.toByteArray());
			myBitmap = BitmapFactory.decodeStream(imageStream);
		}
		catch (MalformedURLException ex)
		{
			//Log.e(Tag, "URL error: " + ex.getMessage(), ex);
		}

		catch (IOException ioe)
		{
			//Log.e(Tag, "IO error: " + ioe.getMessage(), ioe);
		}

		//call Handler
		myHandler.sendEmptyMessage(0);
	}

	Bitmap getBitmap() {
		return myBitmap;
	}

}
