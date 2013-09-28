package com.wewrite;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import android.widget.Button;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import edu.umich.imlc.android.common.Utils;
import edu.umich.imlc.collabrify.client.CollabrifyAdapter;
import edu.umich.imlc.collabrify.client.CollabrifyClient;
import edu.umich.imlc.collabrify.client.CollabrifyListener;
import edu.umich.imlc.collabrify.client.CollabrifySession;
import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;

import MainMenu.src.com.wewrite.EventProtos.java;


public class MainActivity extends Activity
{
  private static String TAG = "WeWrite";
  private String sessionName;
  private CollabrifyClient myClient;
  private CheckBox withBaseFile;
  private CollabrifyListener collabrifyListener;
  private ArrayList<String> tags = new ArrayList<String>();
  private MenuItem createSession;
  private MenuItem joinSession;
  private MenuItem leaveSession;
  private long sessionId;
  private ByteArrayInputStream baseFileBuffer;
  private ByteArrayOutputStream baseFileReceiveBuffer;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Intent intent = getIntent();
    setContentView(R.layout.activity_main);
    tags.add("amchr.csyoon");
    createSession = (MenuItem) findViewById(R.id.createSession);
    joinSession = (MenuItem) findViewById(R.id.joinSession);
    leaveSession = (MenuItem) findViewById(R.id.leaveSession);

    // do something with this later
    // withBaseFile = (CheckBox) findViewById((Integer) null);


    boolean getLatestEvent = false;
    
    
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
            //createSession.setTitle("Create Session");
          }
        });
      }

      @Override
      public void onReceiveEvent(final long orderId, int subId,
          String eventType, final byte[] data)
      {
        Utils.printMethodName(TAG);
        Log.d(TAG, "RECEIVED SUB ID:" + subId);
        
        //if subid == mainactivity nextrevision
        // do something
        
        runOnUiThread(new Runnable()
        {
          @Override
          public void run()
          {
             //handle events
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
            MainActivity.this);

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
                  Log.e(TAG, "error on choose session", e);
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
            //createSession.setEnabled(false);
          }
        });
      }

      @Override
      public void onError(CollabrifyException e)
      {
        Log.e(TAG, "error line 166 ", e);
      }

      @Override
      public void onSessionJoined(long maxOrderId, long baseFileSize)
      {
        Log.i(TAG, "Session Joined");
        if( baseFileSize > 0 )
        {
          // initialize buffer to receive base file
          baseFileReceiveBuffer = new ByteArrayOutputStream((int) baseFileSize);
        }
        runOnUiThread(new Runnable()
        {

          @Override
          public void run()
          {
            //createSession.setTitle(sessionName);
          }
        });
      }
    };

    try
    {
      myClient = new CollabrifyClient(this, "csyoon@umich.edu", "csyoon",
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


  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    createSession = (MenuItem) findViewById(R.id.createSession);
    joinSession = (MenuItem) findViewById(R.id.joinSession);
    leaveSession = (MenuItem) findViewById(R.id.leaveSession);
    switch ( item.getItemId() )
    {

      case R.id.createSession:
        try
        {
          Random rand = new Random();
          sessionName = "amchr.csyoon " + rand.nextInt(Integer.MAX_VALUE);

          /*
           * if( false) { initialize basefile data for this example we will use
           * the session name as the data baseFileBuffer = new
           * ByteArrayInputStream(sessionName.getBytes());
           * 
           * myClient.createSessionWithBase(sessionName, tags, null, 0); } else
           * {
           */


          myClient.createSession(sessionName, tags, null, 0);
          //createSession.setTitle(sessionName);
          Log.i(TAG, "Session name is " + sessionName);
        }
        catch( CollabrifyException e )
        {
          Log.e(TAG, "error in create session ", e);
        }
        return true;

      case R.id.joinSession:
        try
        {
          myClient.requestSessionList(tags);
        }
        catch( Exception e )
        {
          Log.e(TAG, "error in join session", e);
        }
        return true;
      case R.id.leaveSession:
        try
        {
          if( myClient.inSession() )
          {
            myClient.leaveSession(false);
          }
        }
        catch( CollabrifyException e )
        {
          Log.e(TAG, "error in leave session", e);
        }
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }


  public boolean onKeyUp(int keyCode, KeyEvent event)
  {
    return false;
  }
  
  Stack<EventProtos> redoStack = new Stack<EventProtos>();
  Stack<EventProtos> undoStack = new Stack<EventProtos>();
  
  Button redoButton = (Button) findViewById(R.id.redo);
  Button undoButton = (Button) findViewById(R.id.undo);

  public void redo(View v)
  {
    if(!redoStack.isEmpty())
    {
      EventProtos event = redoStack.pop();
      undoStack.push(event);
      event.run();
    }
  }
  
  public void undo(View v)
  {
    if(!undoStack.isEmpty())
    {
      EventProtos event = undoStack.pop();
      redoStack.push(event);
      event.run();
    }
  }
}
