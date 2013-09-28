package com.wewrite;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import edu.umich.imlc.android.common.Utils;
import edu.umich.imlc.collabrify.client.CollabrifyAdapter;
import edu.umich.imlc.collabrify.client.CollabrifyClient;
import edu.umich.imlc.collabrify.client.CollabrifyListener;
import edu.umich.imlc.collabrify.client.CollabrifySession;
import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;


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
        Log.i(getTAG(), "disconnected");
        runOnUiThread(new Runnable()
        {

          @Override
          public void run()
          {
            // createSession.setTitle("Create Session");
          }
        });
      }

      @Override
      public void onReceiveEvent(final long orderId, int subId,
          String eventType, final byte[] data)
      {
        Utils.printMethodName(getTAG());
        Log.d(getTAG(), "RECEIVED SUB ID:" + subId);

        // if subid == mainactivity nextrevision
        // do something

        runOnUiThread(new Runnable()
        {
          @Override
          public void run()
          {
            // handle events
          }
        });
      }

      @Override
      public void onReceiveSessionList(final List<CollabrifySession> sessionList)
      {
        if( sessionList.isEmpty() )
        {
          Log.i(getTAG(), "No session available");
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
                  setSessionId(sessionList.get(which).id());
                  setSessionName(sessionList.get(which).name());
                  getMyClient().joinSession(getSessionId(), null);
                }
                catch( CollabrifyException e )
                {
                  Log.e(getTAG(), "error on choose session", e);
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
        Log.i(getTAG(), "Session created, id: " + id);
        setSessionId(id);
        runOnUiThread(new Runnable()
        {

          @Override
          public void run()
          {
            // createSession.setEnabled(false);
          }
        });
      }

      @Override
      public void onError(CollabrifyException e)
      {
        Log.e(getTAG(), "error line 166 ", e);
      }

      @Override
      public void onSessionJoined(long maxOrderId, long baseFileSize)
      {
        Log.i(getTAG(), "Session Joined");
        if( baseFileSize > 0 )
        {
          // initialize buffer to receive base file
          setBaseFileReceiveBuffer(new ByteArrayOutputStream((int) baseFileSize));
        }
        runOnUiThread(new Runnable()
        {

          @Override
          public void run()
          {
            // createSession.setTitle(sessionName);
          }
        });
      }
    };

    try
    {
      setMyClient(new CollabrifyClient(this, "csyoon@umich.edu", "csyoon",
          "441fall2013@umich.edu", "XY3721425NoScOpE", getLatestEvent,
          collabrifyListener));
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
          setSessionName("amchr.csyoon " + rand.nextInt(Integer.MAX_VALUE));

          /*
           * if( false) { initialize basefile data for this example we will use
           * the session name as the data baseFileBuffer = new
           * ByteArrayInputStream(sessionName.getBytes());
           * 
           * myClient.createSessionWithBase(sessionName, tags, null, 0); } else
           * {
           */


          getMyClient().createSession(getSessionName(), tags, null, 0);
          // createSession.setTitle(sessionName);
          Log.i(getTAG(), "Session name is " + getSessionName());
        }
        catch( CollabrifyException e )
        {
          Log.e(getTAG(), "error in create session ", e);
        }
        return true;

      case R.id.joinSession:
        try
        {
          getMyClient().requestSessionList(tags);
        }
        catch( Exception e )
        {
          Log.e(getTAG(), "error in join session", e);
        }
        return true;
      case R.id.leaveSession:
        try
        {
          if( getMyClient().inSession() )
          {
            getMyClient().leaveSession(false);
          }
        }
        catch( CollabrifyException e )
        {
          Log.e(getTAG(), "error in leave session", e);
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

  public static String getTAG()
  {
    return TAG;
  }

  public static void setTAG(String tAG)
  {
    TAG = tAG;
  }

  public long getSessionId()
  {
    return sessionId;
  }

  public void setSessionId(long sessionId)
  {
    this.sessionId = sessionId;
  }

  public String getSessionName()
  {
    return sessionName;
  }

  public void setSessionName(String sessionName)
  {
    this.sessionName = sessionName;
  }

  public CollabrifyClient getMyClient()
  {
    return myClient;
  }

  public void setMyClient(CollabrifyClient myClient)
  {
    this.myClient = myClient;
  }

  public ByteArrayOutputStream getBaseFileReceiveBuffer()
  {
    return baseFileReceiveBuffer;
  }

  public void setBaseFileReceiveBuffer(
      ByteArrayOutputStream baseFileReceiveBuffer)
  {
    this.baseFileReceiveBuffer = baseFileReceiveBuffer;
  }

}
