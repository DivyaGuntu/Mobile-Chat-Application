package tsu.edu;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserManager extends Thread {

    // contains information about the current user
    private User user;
    // the socket that links the user(client) to this server
    private Socket socket;
    private PrintWriter bufferSender;
    // flag used to stop the read operation
    private boolean running;
    // used to notify certain user actions like receiving a message or disconnect
    private UserManagerDelegate managerDelegate;
    private InputStream is = null;
    private OutputStream os = null;
   // private OutputStream os = null;
    int bytesRead;
    int current = 0;
    int i = 1;
   // FileOutputStream fos = null;
   // BufferedOutputStream bos = null;
    
    public UserManager(Socket socket, UserManagerDelegate managerDelegate) {
        this.user = new User();
        this.socket = socket;
        this.managerDelegate = managerDelegate;
        running = true;
    }

    public User getUser() {
        return user;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        super.run();

        System.out.println("S: Receiving...");
  
        
        try {
        	os = socket.getOutputStream();
            //sends the message to the client
            bufferSender = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)), true);
        	is = socket.getInputStream();
            //read the message received from client
             BufferedReader in = new BufferedReader(new InputStreamReader(is /*socket.getInputStream()*/));
            //in this while we wait to receive messages from client (it's an infinite loop)
            //this while it's like a listener for messages
            while (running) {
                String file = "null";
                String message = null;
                String messageCheck = null;
                try{
                	messageCheck = in.readLine();
                	message = messageCheck;
                	if(messageCheck.contains("SendingFile")){
                		System.out.println("first");
                		//System.out.println(messageCheck);
                		String[] filename = messageCheck.split(" ");
                		running = false;
                		file = filename[1];
                		receiveFile(file);}
                }
                catch (IOException e) {
                	//getSocket();
                    //System.out.println("Error reading message: " + e);
                }
             
                if (hasCommand(message)) {
                    continue;
                }

                if (message != null && managerDelegate != null) {
                    user.setMessage(message);
                    // notify message received action
                    managerDelegate.messageReceived(user, null);
                		}
            	
           
            }

        } catch (Exception e) {
            System.out.println("S: Error");
            e.printStackTrace();
        }
    }
    
    
    public void receiveFile(String filename){
        int filesize = 602238600; // filesize temporary hardcoded
        long start = System.currentTimeMillis();
        int bytesRead;
        int current = 0;
    	try{ 
	      byte [] mybytearray  = new byte [filesize];
          InputStream is = socket.getInputStream();
          FileOutputStream fos = new FileOutputStream("C:/Users/user/Desktop/echat/"+filename); // destination path and name of file
          BufferedOutputStream bos = new BufferedOutputStream(fos);
          bytesRead = is.read(mybytearray,0,mybytearray.length);
          current = bytesRead;
          do {
             bytesRead =
                is.read(mybytearray, current, (mybytearray.length-current));
             if(bytesRead >= 0) current += bytesRead;
          } while(bytesRead > -1);

          bos.write(mybytearray, 0 , current);
          bos.flush();
          long end = System.currentTimeMillis();
          System.out.println(end-start);
          bos.close();
          fos.close();
    	}
    	catch(Exception e){
    		System.out.println(e);
    	}
    	
    }
    

    /**
     * Close the server
     */
    public void close() {

        running = false;

        if (bufferSender != null) {
            bufferSender.flush();
            bufferSender.close();
            bufferSender = null;
        }

        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("S: User " + user.getUsername() + " left the room.");
        socket = null;

        //todo close all user connections

    }

    /**
     * Method to send the messages from server to client
     *
     * @param message the message sent by the server
     */
    public void sendMessage(String message) {
        if (bufferSender != null && !bufferSender.checkError()) {
            bufferSender.println(message);
            bufferSender.flush(); 
           if(message.contains("SendingFile")){
            	String[] filename = message.split(" ");
        		String file = filename[filename.length-1];
            	sendFile(file);
            }
        }
    }
    
    public void sendFile(String filename) {
    	
    	try{		
    	 File myFile = new File ("C:/Users/user/Desktop/echat/"+filename);	
    	 FileInputStream fis = new FileInputStream(myFile);
         BufferedInputStream bis = new BufferedInputStream(fis); 
         OutputStream os = socket.getOutputStream();      
         byte[] contents;
         long fileLength = myFile.length(); 
         long current = 0;
         while(current!=fileLength){ 
             int size = 200000;
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
             System.out.println(contents);
             System.out.print("Sending file ... "+ (current*100)/fileLength+" % complete!\n ");
                }   
         //os.write(stop);
         os.flush(); 
         socket.close();
        // System.out.println("second time going to run method");
	    // run();
	   // running = true;
    	}
    	catch(Exception e){
    		
    	}
    }
    
    public boolean hasCommand(String message) {
        if (message != null) {
            if (message.contains(Constants.CLOSED_CONNECTION)) {
                close();
                // let the server know that this user was disconnected
                managerDelegate.userDisconnected(this);
                return true;
            } else if (message.contains(Constants.LOGIN_NAME)) {
                // parse the username
                user.setUsername(message.replaceAll(Constants.LOGIN_NAME, ""));
                user.setUserID(socket.getPort());
                // send a user connected message
                managerDelegate.userConnected(user);
                return true;
            }
        }

        return false;
    }

    /**
     * Used to talk with the TcpServer class or whoever wants to receive notifications from this manager
     */
    public interface UserManagerDelegate {

        /**
         * Called whenever a user is connected to the server
         *
         * @param connectedUser the connected user
         */
        public void userConnected(User connectedUser);

        /**
         * Called when a user is disconnected from the server
         *
         * @param userManager the manager of the disconnected user
         */
        public void userDisconnected(UserManager userManager);

        /**
         * Called when the manager receives a new message from the client
         *
         * @param fromUser the user that sent the message
         * @param toUser   the user that should receive the message
         */
        public void messageReceived(User fromUser, User toUser);

    }

}
