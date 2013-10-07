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
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import edu.umich.imlc.android.common.Utils;
import edu.umich.imlc.collabrify.client.CollabrifyAdapter;
import edu.umich.imlc.collabrify.client.CollabrifyClient;
import edu.umich.imlc.collabrify.client.CollabrifyListener;
import edu.umich.imlc.collabrify.client.CollabrifySession;
import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;

import com.wewrite.EventProtos;
import com.wewrite.EventProtos.Event;


@SuppressWarnings("unused")
public class MainActivity extends Activity
{
  private String theText = "";
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
  private cursorWatcher editTextArea;
  private long startTime;
  private int continuousCount = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Intent intent = getIntent();
    setContentView(R.layout.activity_main);
    tags.add("amchr.csyoon test");
    createSession = (MenuItem) findViewById(R.id.createSession);
    joinSession = (MenuItem) findViewById(R.id.joinSession);
    leaveSession = (MenuItem) findViewById(R.id.leaveSession);

    // setEditTextArea((cursorWatcher) findViewById(R.id.editTextSimple));

    editTextArea = (cursorWatcher) findViewById(R.id.editTextSimple);
    editTextArea.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    TheDevice.editTextArea = editTextArea;
    TheDevice.cursors.put(TheDevice.deviceId, 0);
    Button redoButton = (Button) findViewById(R.id.redoButton);
    Button undoButton = (Button) findViewById(R.id.undoButton);

    new Thread()
    {
      public void run()
      {
        startTime = System.currentTimeMillis();
        while( true )
        {
          if( getContinuousCount() != 0 && System.currentTimeMillis() - startTime >= 600 )
            insertDeleteActions();
          else if( getContinuousCount() == 0 && TheDevice.unmatched )
            TheDevice.match();
        }
      }
    }.start();

    undoButton.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        if( continuousCount != 0 )
          insertDeleteActions();

        Commands com = TheDevice.Undo();
        if( com != null )
        {
          Event retmove = com.generateMoveMes(1);
          broadcastText(retmove, "undo");
        }
      }
    });
    redoButton.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        if( continuousCount != 0 )
          insertDeleteActions();

        Commands com = TheDevice.Redo(); 
        if( com != null )
        {
          Event retmove = com.generateMoveMes(2);
          broadcastText(retmove, "redo");
        }
      }
    });

    getEditTextArea().setOnClickListener(new View.OnClickListener()
    { 
          @Override
          public void onClick(View v)
          {
            if( continuousCount != 0 )
              insertDeleteActions();
              
            int cursorNewLoc = getEditTextArea().getSelectionEnd();
            int offset = cursorNewLoc - TheDevice.cursorPos;

            if( offset != 0 )
            {
              getEditTextArea().setSelection(cursorNewLoc);
              TheDevice.cursorPos = cursorNewLoc;

              Commands com = new Commands(TheDevice.EventType.CURSOR, null, offset);
              Event retmove = com.generateMoveMes(0); 
              broadcastText(retmove, "cur");
              TheDevice.redoList.clear();
            }
          }
        });

    getEditTextArea().addTextChangedListener(new TextWatcher()
    {      
      @Override
      public void afterTextChanged(Editable arg0)
      {
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after)
      {
        if( TheDevice.setOnDevice && count > after)
        {
            if( getContinuousCount() > 0 )
              insertDeleteActions();

            startTime = System.currentTimeMillis();
            continuousCount--;
            theText = s.toString().substring(start, start + count) + theText;
        }
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count)
      {
        if( TheDevice.setOnDevice && count > before)
        {
            if( getContinuousCount() < 0 )
              insertDeleteActions();

            startTime = System.currentTimeMillis();
            continuousCount++;
            theText += s.toString().substring(start, start + count);
        }
        else if( !TheDevice.setOnDevice)
          TheDevice.setOnDevice = true;
      }
    });
    
    collabrifyListener = new CollabListener(this);
    boolean getLatestEvent = false;
    
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

  private void insertDeleteActions()
  {
    Event action;
    if( continuousCount > 0 ) 
    {
      TheDevice.cursorPos += continuousCount;
      Commands com = new Commands(TheDevice.EventType.ADD, getTheText(), continuousCount);
      action = com.generateMoveMes(0);
      broadcastText(action, "add");
    }
    else
    {
      TheDevice.cursorPos += continuousCount;
      Commands com = new Commands(TheDevice.EventType.DELETE, getTheText(), -continuousCount);
      action = com.generateMoveMes(0);
      broadcastText(action, "del");
    }

    continuousCount = 0;
    setTheText(getTheText().substring(0, 0));
    TheDevice.redoList.clear();
  }

  private void broadcastText(Event retMove, String op)
  {
    try
    {
      TheDevice.lastsubId = myClient.broadcast(retMove.toByteArray(), op);
      TheDevice.setOnDevice = false;
      Log.i("success", op + " broadcasting success");
    }
    catch( CollabrifyException e )
    {
      Log.i("failed", "broadcasting failed");
      e.printStackTrace();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
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
          setSessionName("amchr.csyoon test " + rand.nextInt(Integer.MAX_VALUE));
          getMyClient().createSession(getSessionName(), tags, null, 0);
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
            getMyClient().leaveSession(false);
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

  public int getContinuousCount()
  {
    return continuousCount;
  }

  public void setContinuousCount(int continuousCount)
  {
    this.continuousCount = continuousCount;
  }

  public String getTheText()
  {
    return theText;
  }

  public void setTheText(String theText)
  {
    Log.i(getTAG(), " 440 " + theText);
    this.theText = theText;
  }

  public cursorWatcher getEditTextArea()
  {
    return editTextArea;
  }

  public void setEditTextArea(cursorWatcher editTextArea)
  {
    this.editTextArea = editTextArea;
  }
}
