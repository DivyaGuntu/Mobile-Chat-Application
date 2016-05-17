package com.tsu.echat;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;

import com.tsu.echat.chatActivity.ConnectTask;

public class TcpClient {

  //  public static final String SERVER_IP = "192.168.0.14"; //your computer IP address
   // public static final int SERVER_PORT = 100;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    String filepath  = null;
    Socket socket;
    OutputStream os = null;
    InputStream is = null;
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(String message) {
        if (mBufferOut != null && !mBufferOut.checkError()) {
            mBufferOut.println(message);
            System.out.println(message);
            mBufferOut.flush();
        }
    }
    public int sendAttachments(String ip, String port, String username, String path) {
    	//filepath = path;
    	//run(ip,port,username,path);
    	int i = 1;
    	try{
    	 File myFile = new File (path);
    	 FileInputStream fis = new FileInputStream(myFile);
         BufferedInputStream bis = new BufferedInputStream(fis); 
                 
         //Read File Contents into contents array 
         byte[] contents;
         long fileLength = myFile.length(); 
         long current = 0;
          
         while(current!=fileLength){ 
             int size = 1000000;
             if(fileLength - current >= size)
                 current += size;    
             else{ 
                 size = (int)(fileLength - current); 
                 current = fileLength;
             } 
             contents = new byte[size]; 
             bis.read(contents, 0, size); 
             os.write(contents);
             System.out.println(size);
             System.out.print("Sending file ... "+ (current*100)/fileLength+" % complete!\n ");
                }   
         
         os.flush(); 
         socket.close();
    	}
    	catch(Exception e){
    		
    	}
    	finally{
    		  //sendMessage("SendingFileDone");
    	}
        /*if (mBufferOut != null && !mBufferOut.checkError()) {
            mBufferOut.println(path);
            mBufferOut.flush();
        }*/
		return i;
    }
    public int receiveAttachments(String file){
    	  mRun = false;
    	  int filesize = 200000; // filesize temporary hardcoded
          int bytesRead;
          int current = 0;int i = 1;
        try{	
         String[] filesplit = file.split(" ");
         int length = filesplit.length;
         String filename = filesplit[length -1];
         File dir = new File(Environment.getExternalStorageDirectory() + "/TsuEchat/");
         dir.mkdir();
        // String filename = "Screenshot_2016-02-09-08-13-03.png";
	      byte [] mybytearray  = new byte [filesize];
          InputStream is = socket.getInputStream();
          FileOutputStream fos = new FileOutputStream(dir+"/"+filename); // destination path and name of file
          BufferedOutputStream bos = new BufferedOutputStream(fos);
          bytesRead = is.read(mybytearray,0,mybytearray.length);
          current = bytesRead;
          do {
             bytesRead =
                is.read(mybytearray, current, (mybytearray.length-current));
             System.out.println(bytesRead);
             if(bytesRead >= 0) current += bytesRead;
          } while(bytesRead > -1);

          bos.write(mybytearray, 0 , current);
          bos.flush();
          bos.close();
          fos.close();
      }
      catch(Exception e){
    	  System.out.println(e);
      }
      return i;
    }
    
  
    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        // send mesage that we are closing the connection

        mRun = false;
        try{
        if (fis != null) fis.close();
        if (bis != null) bis.close();
        }
        catch(Exception e){
        	
        }

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    public void run(String SERVER_IP, String SERVER_PORT, String USERNAME) {
        int port = Integer.parseInt(SERVER_PORT);
        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            Log.e("TCP Client", "Client Connecting...");

            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, port);
            //Log.d("TCP",""+PATH);
            try {

                //sends the message to the server
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                os = socket.getOutputStream();
                //receives the message which the server sends back
               
                is = socket.getInputStream();
               
               // System.out.println(is);
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                sendMessage("login_name"+ USERNAME);
                //if(PATH!= null){
                //Log.e("TCP","Please transfer this file"+PATH);
                
               // }
          
                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    
                    mServerMessage = mBufferIn.readLine();
          
                    if (mServerMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                    	String serverMessage = mServerMessage;
                    	System.out.println(mServerMessage);
                     if(serverMessage.contains("SendingFile")){
                    		System.out.println(serverMessage);
                    		String sm = serverMessage;
                    		int i = receiveAttachments(serverMessage);
                    		//System.out.println("i value"+1);
                    		serverMessage = sm;
                    		}
                     mMessageListener.messageReceived(serverMessage); 
                             
                        }
                    }                                      	
                        
                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");

            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);

        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}
