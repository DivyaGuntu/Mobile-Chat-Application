package com.tsu.echat;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
 
public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
     /* Set OnClickListner to the login button */
        Button login = (Button) findViewById(R.id.connect);
         
        login.setOnClickListener(new View.OnClickListener() {
    
   @Override
   public void onClick(View v) {
    try{ 
    EditText ip = (EditText) findViewById(R.id.address);  
    EditText port = (EditText) findViewById(R.id.port);
    EditText username = (EditText) findViewById(R.id.uid);
    String  server_address  = ip.getText().toString();
    String server_port = port.getText().toString();
    String uname = username.getText().toString();
    Intent intent  = new Intent(getApplicationContext(),chatActivity.class);
     
    intent.putExtra("ip",server_address );
    intent.putExtra("port", server_port); 
    intent.putExtra("uname", uname);
    startActivity(intent);
    }
    catch(Exception e){
    	
    }
      
   }
  });
    }
}