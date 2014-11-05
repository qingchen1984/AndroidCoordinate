package net.hensing.tradition2;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

public class Paths implements Runnable {
	private Handler myHandler;
	private String myGroup;
	URL connectURL;
	String responseString;
	String Title;
	String Description;
	byte[ ] dataToServer;
	String paramName1 = "group";
	String paramValue1;
	JSONArray jarray;

	Paths(String url, String group, Handler h){
		myHandler = h;
		myGroup = group;
		try{
			connectURL = new URL(url);
			paramValue1 = group;
		}catch(Exception ex){
			//Log.i("Httpimagelist","URL Malformatted");
		}

	}
	Bitmap recieve() {
		return null;
	}


	@Override
	public void run() {
		String iFileName = Title;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        String Tag="fSnd";
        StringBuilder builder = new StringBuilder();
        try
        {
                

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

                String urlParameters = "group="+paramValue1;
                
                dos.writeBytes(urlParameters);
                
                
                //Log.e(Tag,"Headers are written");

                    
                dos.flush();
                dos.close();
                    
                //Log.e(Tag,"File Sent, Response: "+String.valueOf(conn.getResponseCode()));
                     
                InputStream is = conn.getInputStream();
                    
                // retrieve the response from server
                int ch;

                StringBuffer b =new StringBuffer();
                while( ( ch = is.read() ) != -1 ){ b.append( (char)ch ); }
                String s=b.toString();
                //Log.i("Response", "response from server: "+s);
                dos.close();
                
            	if (s.contains("Error")){
            		//
            		
            	}
            	else{
            		//
            		try {
            			jarray = new JSONArray(s);
            			//Log.i("Response","json: "+jarray);
            		} catch (JSONException e) {
            			//Log.e("JSON Parser", "Error parsing data " + e.toString());
            		}

            	}
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
	JSONArray getJsonArray(){
		return jarray;
	
	}
	

}

