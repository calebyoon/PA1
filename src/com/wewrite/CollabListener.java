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

public class CollabListener extends CollabrifyAdapter {
  
  private MainActivity collabActivity;
  
  public CollabListener(MainActivity x)
  {
    this.collabActivity = x;
  }
  
  @Override
  public void onDisconnect()
  {
    Log.i(collabActivity.getTAG(), "disconnected");
    collabActivity.runOnUiThread(new Runnable()
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
    Utils.printMethodName(collabActivity.getTAG());
    Log.d(collabActivity.getTAG(), "RECEIVED SUB ID:" + subId);
    
    //if subid == mainactivity nextrevision
    // do something
    
    collabActivity.runOnUiThread(new Runnable()
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
      Log.i(collabActivity.getTAG(), "No session available");
      return;
    }
    List<String> sessionNames = new ArrayList<String>();
    for( CollabrifySession s : sessionList )
    {
      sessionNames.add(s.name());
    }

    final AlertDialog.Builder builder = new AlertDialog.Builder(collabActivity);

    builder.setTitle("Choose Session").setItems(
        sessionNames.toArray(new String[sessionList.size()]),
        new DialogInterface.OnClickListener()
        {
          @Override
          public void onClick(DialogInterface dialog, int which)
          {
            try
            {
              collabActivity.setSessionId(sessionList.get(which).id());
              collabActivity.setSessionName(sessionList.get(which).name());
              collabActivity.getMyClient().joinSession(collabActivity.getSessionId(), null);
            }
            catch( CollabrifyException e )
            {
              Log.e(collabActivity.getTAG(), "error on choose session", e);
            }
          }
        });

    collabActivity.runOnUiThread(new Runnable()
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
    Log.i(collabActivity.getTAG(), "Session created, id: " + id);
    collabActivity.setSessionId(id);
    collabActivity.runOnUiThread(new Runnable()
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
    Log.e(collabActivity.getTAG(), "error line 166 ", e);
  }

  @Override
  public void onSessionJoined(long maxOrderId, long baseFileSize)
  {
    Log.i(collabActivity.getTAG(), "Session Joined");
    if( baseFileSize > 0 )
    {
      // initialize buffer to receive base file
      collabActivity.setBaseFileReceiveBuffer(new ByteArrayOutputStream((int) baseFileSize));
    }
    collabActivity.runOnUiThread(new Runnable()
    {

      @Override
      public void run()
      {
        //createSession.setTitle(sessionName);
      }
    });
  }
}