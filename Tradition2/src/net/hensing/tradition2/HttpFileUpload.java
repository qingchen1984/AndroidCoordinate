package net.hensing.tradition2;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.Handler;
	
public class HttpFileUpload implements Runnable{
	

	
	
        URL connectURL;
        String responseString;
        String Title;
        String Description;
        byte[ ] dataToServer;
        FileInputStream fileInputStream = null;
    	String paramName1 = "group";
    	String paramName2 = "eventid";
    	String paramValue1;
    	String paramValue2;
    	private Handler handler_upload_failed;
    	private Handler handler_upload_success;

        HttpFileUpload(String urlString, String vTitle, String groupName, String eventId,String vDesc, Handler handler_failed, Handler handler_success){
        	//Log.d("logging","debugging - starting HTTPFILEUPLOAD");

                try{
                        connectURL = new URL(urlString);
                        Title= vTitle;
                        Description = vDesc;
                        paramValue1 = groupName;
                        paramValue2 = eventId;
                        handler_upload_failed = handler_failed;
                        handler_upload_success = handler_success;
                }catch(Exception ex){
                    //Log.i("HttpFileUpload","URL Malformatted");
                }
        }
	
        void Send_Now(FileInputStream fStream){
                fileInputStream = fStream;
                Sending();
        }
	
        void Sending(){
                String iFileName = Title;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                String Tag="fSnd";
                try
                {
                        //Log.e(Tag,"Starting Http File Sending to URL");
	
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
	
                        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

                        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                        
               
                        
                        
                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        //value1:
                        dos.writeBytes("Content-Disposition: form-data; name=\"group\""+lineEnd+lineEnd+paramValue1+lineEnd);
                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        //value2:
                        dos.writeBytes("Content-Disposition: form-data; name=\"eventid\""+lineEnd+lineEnd+paramValue2+lineEnd);
                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        // file
                        dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";type=\"image/jpeg\";filename=\"" + iFileName +"\"" + lineEnd); 
                        dos.writeBytes("Content-Type: image/jpeg" + lineEnd);
 
                        dos.writeBytes(lineEnd);
                        
      
                        //Log.e(Tag,"Headers are written");
	
                        // create a buffer of maximum size
                        int bytesAvailable = fileInputStream.available();
	                        
                        int maxBufferSize = 1024;
                        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        byte[ ] buffer = new byte[bufferSize];
	
                        // read file and write it into form...
                        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
	
                        while (bytesRead > 0)
                        {
                                dos.write(buffer, 0, bufferSize);
                                bytesAvailable = fileInputStream.available();
                                bufferSize = Math.min(bytesAvailable,maxBufferSize);
                                bytesRead = fileInputStream.read(buffer, 0,bufferSize);
                        }
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	
                        // close streams
                        fileInputStream.close();
	                        
                        dos.flush();
	                        
                        //Log.e(Tag,"File Sent, Response: "+String.valueOf(conn.getResponseCode()));
	                         
                        InputStream is = conn.getInputStream();
	                        
                        // retrieve the response from server
                        int ch;
	
                        StringBuffer b =new StringBuffer();
                        while( ( ch = is.read() ) != -1 ){ b.append( (char)ch ); }
                        String s=b.toString();
                        //Log.i("Response",s);
                        dos.close();
                        useResult(s);
                }
                catch (MalformedURLException ex)
                {
                        //Log.e(Tag, "URL error: " + ex.getMessage(), ex);
                }
	
                catch (IOException ioe)
                {
                        //Log.e(Tag, "IO error: " + ioe.getMessage(), ioe);
                }
        }
        
        public void useResult(String result){
        	if (result.contains("Error")){
        		handler_upload_failed.sendEmptyMessage(0);
        	}
        	else{
        		
        	}
        	
        }
	
        @Override
        public void run() {
                // TODO Auto-generated method stub
        }
}