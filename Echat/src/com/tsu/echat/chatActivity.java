package com.tsu.echat;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class chatActivity extends Activity {

    private ListView mList;
    private ArrayList<String> arrayList;
    private ClientListAdapter mAdapter;
    private TcpClient mTcpClient;
    String ip = null;
    String port = null;
    String path = null;
    String username = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        ip  = intent.getStringExtra("ip");
        port   = intent.getStringExtra("port");
        username = intent.getStringExtra("uname");

        arrayList = new ArrayList<String>();

        final EditText editText = (EditText) findViewById(R.id.editText);
        Button send = (Button) findViewById(R.id.send_button);
        Button attach = (Button) findViewById(R.id.attach_button);

        //relate the listView from java to the one created in xml
        mList = (ListView) findViewById(R.id.list);
        mAdapter = new ClientListAdapter(this, arrayList);
        mList.setAdapter(mAdapter);
        
        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                   showFileChooser(); }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString();

                //add the text in the arrayList
                arrayList.add(username+" says " + message);

                //sends the message to the server
                if (mTcpClient != null) {
                    mTcpClient.sendMessage(message);
                }

                //refresh the list
                mAdapter.notifyDataSetChanged();
                editText.setText("");
            }
        });
        new ConnectTask().execute("");
    }

 /*   @Override
    protected void onPause() {
        super.onPause();

        if (mTcpClient != null) {
            // disconnect
            mTcpClient.stopClient();
            mTcpClient = null;
        }

    }*/

    private static final int FILE_SELECT_CODE = 0;

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
        intent.setType("*/*"); 
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.", 
                    Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
            if (resultCode == RESULT_OK) {
                // Get the Uri of the selected file 
                Uri uri = data.getData();
                // Get the path
                String path = null;
				try {
					path = FileUtils.getPath(this, uri);
					
					
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                Log.d("", "File Path: " + path);
                String[] splitpath = path.split("/");
                int length = splitpath.length;
                String filename = splitpath[length-1];
                System.out.println(filename);
                
                mTcpClient.sendMessage("SendingFile "+filename);
                arrayList.add("Me : " + "Sent attachment to all clients");
               // File myFile = new File (path);
               // int size = (int)myFile.length();
                //mTcpClient.sendMessage(""+size);
                int i =  mTcpClient.sendAttachments(ip,port,username,path);
                // File file = new File(path);
                // Initiate the upload
                //Toast.makeText(getBaseContext(), "File Sent Successfully", Toast.LENGTH_SHORT);
               if(i == 1){
            	  new ConnectTask().execute(""); 
               }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
                  
               

    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        @Override
        protected TcpClient doInBackground(String... message) {

            //we create a TCPClient object and
            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                    if(message.contains("SendingFile")){
                     mTcpClient.run(ip,port,username);
                    	}
                }
            });
            Log.d("Checking",""+path);
            mTcpClient.run(ip,port,username);

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //in the arrayList we add the messaged received from server
            arrayList.add(values[0]);
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
            mAdapter.notifyDataSetChanged();
        }
    }
}
