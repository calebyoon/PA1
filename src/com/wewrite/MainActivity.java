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

import com.wewrite.EventProtos;


@SuppressWarnings("unused")
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
    Button redoButton = (Button) findViewById(R.id.redo);
    Button undoButton = (Button) findViewById(R.id.undo);
    tags.add("amchr.csyoon");
    createSession = (MenuItem) findViewById(R.id.createSession);
    joinSession = (MenuItem) findViewById(R.id.joinSession);
    leaveSession = (MenuItem) findViewById(R.id.leaveSession);
    
    Stack<EventProtos> redoStack = new Stack<EventProtos>();
    Stack<EventProtos> undoStack = new Stack<EventProtos>();

    // do something with this later
    // withBaseFile = (CheckBox) findViewById((Integer) null);


    boolean getLatestEvent = false;


    collabrifyListener = new CollabListener(this);
    

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
  
  Stack<EventProtos> redoStack = new Stack<EventProtos>();
  Stack<EventProtos> undoStack = new Stack<EventProtos>();

  public void redo(View v)
  {
    if(!redoStack.isEmpty())
    {
      EventProtos event = redoStack.pop();
      undoStack.push(event);
      //event.run();
    }
  }
  
  public void undo(View v)
  {
    if(!undoStack.isEmpty())
    {
      EventProtos event = undoStack.pop();
      redoStack.push(event);
      //event.run();
    }
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
