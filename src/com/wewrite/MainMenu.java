package com.wewrite;



import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.umich.imlc.android.common.Utils;
import edu.umich.imlc.collabrify.client.CollabrifyAdapter;
import edu.umich.imlc.collabrify.client.CollabrifyClient;
import edu.umich.imlc.collabrify.client.CollabrifyListener;
import edu.umich.imlc.collabrify.client.CollabrifySession;
import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;

public class MainMenu extends Activity
{
  private static String TAG = "WeWrite";
  private long sessionId;
  private CollabrifyClient myClient;
  private CollabrifyListener collabrifyListener;
  private String sessionName;
  private Button createSession;
  private Button getSessionButton;
  private ArrayList<String> tags = new ArrayList<String>();
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.welcome_screen_layout);
    createSession = (Button) findViewById(R.id.createSession);
    getSessionButton = (Button) findViewById(R.id.createSession);
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
    
    getSessionButton.setOnClickListener(new OnClickListener()
    {

      @Override
      public void onClick(View v)
      {
        try
        {
          myClient.requestSessionList(tags);
        }
        catch( Exception e )
        {
          Log.e(TAG, "error", e);
        }
      }
    });
    
    collabrifyListener = new CollabrifyAdapter()
    {

      @Override
      public void onDisconnect()
      {
        Log.i(TAG, "disconnected");
        runOnUiThread(new Runnable()
        {

          @Override
          public void run()
          {
            createSession.setText("CreateSession");
          }
        });
      }
      
      @Override
      public void onReceiveEvent(final long orderId, int subId,
          String eventType, final byte[] data)
      {
        Utils.printMethodName(TAG);
        Log.d(TAG, "RECEIVED SUB ID:" + subId);
        runOnUiThread(new Runnable()
        {
          @Override
          public void run()
          {
            //IMPORTANT FIGURE OUT WHAT TO DO WITH EVENTS
            
            //Utils.printMethodName(TAG);
            //String message = new String(data);
            //broadcastedText.setText(message);
          }
        });
      }
      
      @Override
      public void onReceiveSessionList(final List<CollabrifySession> sessionList)
      {
        if( sessionList.isEmpty() )
        {
          Log.i(TAG, "No session available");
          return;
        }
        List<String> sessionNames = new ArrayList<String>();
        for( CollabrifySession s : sessionList )
        {
          sessionNames.add(s.name());
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(
            MainMenu.this);
        builder.setTitle("Choose Session").setItems(
            sessionNames.toArray(new String[sessionList.size()]),
            new DialogInterface.OnClickListener()
            {
              @Override
              public void onClick(DialogInterface dialog, int which)
              {
                try
                {
                  sessionId = sessionList.get(which).id();
                  sessionName = sessionList.get(which).name();
                  myClient.joinSession(sessionId, null);
                }
                catch( CollabrifyException e )
                {
                  Log.e(TAG, "error", e);
                }
              }
            });

        runOnUiThread(new Runnable()
        {

          @Override
          public void run()
          {
            builder.show();
          }
        });
      }
      
      @Override
      public void onSessionCreated(long id)
      {
        Log.i(TAG, "Session created, id: " + id);
        sessionId = id;
        runOnUiThread(new Runnable()
        {

          @Override
          public void run()
          {
            createSession.setText(sessionName);
          }
        });
      }
      
      @Override
      public void onError(CollabrifyException e)
      {
        Log.e(TAG, "error", e);
      }
      
      @Override
      public void onSessionJoined(long maxOrderId, long baseFileSize)
      {
        Log.i(TAG, "Session Joined");
        runOnUiThread(new Runnable()
        {
          @Override
          public void run()
          {
            createSession.setText(sessionName);
          }
        });
      }
      
    };
    
    boolean getLatestEvent = false;
    
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
