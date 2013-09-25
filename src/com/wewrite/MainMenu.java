package com.wewrite;



import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.umich.imlc.collabrify.client.CollabrifyClient;
import edu.umich.imlc.collabrify.client.CollabrifyListener;
import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;

public class MainMenu extends Activity
{
  private static String TAG = "WeWrite";
  
  private CollabrifyClient myClient;
  private CollabrifyListener collabrifyListener;
  boolean getLatestEvent = false;
  private String sessionName;
  private Button createSession;
  private ArrayList<String> tags = new ArrayList<String>();
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.welcome_screen_layout);
    createSession = (Button) findViewById(R.id.createSession);
    createSession.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        try
        {
          Random rand = new Random();
          sessionName = "csyoon.amchr " + rand.nextInt(Integer.MAX_VALUE);         
          myClient.createSession(sessionName, tags, null, 0);
          Log.i(TAG, "session name is " + sessionName);
        }
        catch( CollabrifyException e )
        {
          Log.e(TAG, "error", e);
        }
      }
    });
    
    try
    {
      myClient = new CollabrifyClient(this, "user email", "user display name",
          "441fall2013@umich.edu", "XY3721425NoScOpE", getLatestEvent,
          collabrifyListener);
    }
    catch( CollabrifyException e )
    {
      e.printStackTrace();
    }
  }
  
 

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }
  
  public void openEditor(View view)
  {
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
  }

}
