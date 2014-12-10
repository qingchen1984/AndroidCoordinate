package net.hensing.tradition2;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.entity.StringEntity;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ServerDataProvider implements Runnable {

	String urlString = "http://90.226.9.91/php/webClientDataForm.php";
	URL connectURL;
	String responseString;
	FileInputStream fileInputStream = null;
	String paramName1 = "query";
	String paramValue1;

	private Handler handler_upload_failed;
	private Handler handler_upload_success;

	ServerDataProvider(String request, Handler handler_failed, Handler handler_success){

		paramValue1 = request;
		handler_upload_failed = handler_failed;
		handler_upload_success = handler_success;
	}

	void Sending(){

		try
		{
			Log.e("qwerty","Starting Http File Sending to URL");


			// Open a HTTP connection to the URL
			connectURL = new URL(urlString);
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
			conn.setRequestProperty("Connection", "charset=UTF-8");
			

			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
			
			String body = paramName1+"="+paramValue1;

			//dos.write(body.getBytes("UTF-8"));
			
			dos.writeBytes(body);

			dos.flush();
			dos.close();
			
			int responseCode = conn.getResponseCode();

			Log.e("qwerty","Post Sent, ResponseCode: "+String.valueOf(conn.getResponseCode()));

			//Good case
			if (responseCode == 200){
				InputStream is = conn.getInputStream();

				responseString = readIt(is,1000);
				
				Log.d("qwerty","in SDP: " + responseString);
				
				HttpOk(responseString);
			}
			//Bad case
			else {
				HttpNok();
			}
			
			
		}
		catch (MalformedURLException ex)
		{
			HttpNok();
		}

		catch (IOException ioe)
		{
			HttpNok();
		}
	}

	public void HttpOk(String result){
		if (result.contains("Error")){
			handler_upload_failed.sendEmptyMessage(0);
		}
		else{
			
			Log.d("qwerty","calling handler success ");
			
			Message message = handler_upload_success.obtainMessage();
			message.obj = responseString;
			
			handler_upload_success.sendMessage(message);
		}
	}
	
	public void HttpNok(){
		handler_upload_failed.sendEmptyMessage(0);
	}
	
	public String getResponse(){
		return responseString;
	}
	
	public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		byte[] bytesToRead = new byte[4096];

		int actuallyRead = 0;

		while (true) {
			int currentlyRead = stream.read(bytesToRead, 0,
					bytesToRead.length);

			if (currentlyRead <= 0)
				break;

			bs.write(bytesToRead, 0, currentlyRead);
			actuallyRead += currentlyRead;

		}

		bytesToRead = null;
		stream.close();

		bytesToRead = bs.toByteArray();

		String responseFromServer = new String(bytesToRead, "UTF-8");
		return responseFromServer.trim();
	}
	
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		
		Sending();
	}
}