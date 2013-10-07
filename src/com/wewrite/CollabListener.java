package com.wewrite;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wewrite.EventProtos.Event;
import com.wewrite.Commands;
import com.wewrite.TheDevice;
import com.wewrite.TheDevice.EventType;

import edu.umich.imlc.android.common.Utils;
import edu.umich.imlc.collabrify.client.CollabrifyAdapter;
import edu.umich.imlc.collabrify.client.CollabrifySession;
import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;

public class CollabListener extends CollabrifyAdapter
{
  private MainActivity collabActivity;

  public CollabListener(MainActivity mainActivity)
  {
    this.collabActivity = mainActivity;
  }

  @Override
  public void onDisconnect()
  {
    Log.i(MainActivity.getTAG(), "disconnected");
    collabActivity.runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        // createSession.setTitle("Create Session");
      }
    });
  }

  @Override
  public void onReceiveEvent(final long orderId, final int subId, String eventType, final byte[] data)
  {
    Utils.printMethodName(MainActivity.getTAG());

    collabActivity.runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          String moveData;
          Event newEvent = Event.parseFrom(data);
          int currentDevice = newEvent.getUserId();
          int moveType = newEvent.getMoveType();
          int offsetValue = newEvent.getCursorChange();
          int undoValue = newEvent.getUndo();

          if( currentDevice != TheDevice.deviceId )
          {
            if( !TheDevice.undoList.empty() )
              TheDevice.undoList.pop();
          }

          if( !TheDevice.cursors.containsKey(currentDevice) ) 
            TheDevice.cursors.put(currentDevice, TheDevice.cursors.get(TheDevice.deviceId));

          if( moveType == 1 )
          {
            moveData = newEvent.getData();
            
            if( currentDevice == TheDevice.deviceId && undoValue != 1 ) 
            {
              Commands com = new Commands(TheDevice.EventType.ADD, moveData, offsetValue);
              TheDevice.undoList.add(com);
            }
            
            TheDevice.addToCorrectText(currentDevice, offsetValue, moveData);
          }
          else if( moveType == 2 )
          {
            moveData = newEvent.getData();
            
            if( currentDevice == TheDevice.deviceId && undoValue != 1 ) 
            {
              Commands com = new Commands(TheDevice.EventType.DELETE, moveData, offsetValue);
              TheDevice.undoList.add(com);
            }
            
            TheDevice.deleteFromCorrectText(currentDevice, offsetValue);
          }
          else
          {
            if( currentDevice == TheDevice.deviceId && undoValue != 1 ) 
            {
              Commands com = new Commands(TheDevice.EventType.CURSOR, null, offsetValue);
              TheDevice.undoList.add(com);
            }
           
            TheDevice.changeCursorCorrectText(currentDevice, offsetValue);
          }

          if( currentDevice != TheDevice.deviceId || undoValue != 0 )
            TheDevice.numDiffMove++;

          if( TheDevice.lastsubId == subId ) 
          {
            if( collabActivity.getContinuousCount() == 0 && TheDevice.numDiffMove > 0 ) 
              TheDevice.match();
            else if( TheDevice.numDiffMove > 0 )
              TheDevice.unmatched = true;
            else
              TheDevice.lastsubId = -1;
          }

        }
        catch( InvalidProtocolBufferException e )
        {
          Log.i("failed", "bad parse attempt: " + e);
          e.printStackTrace();
        }
      }
    });
  }

  @Override
  public void onReceiveSessionList(final List<CollabrifySession> sessionList)
  {
    if( sessionList.isEmpty() )
    {
      Log.i(MainActivity.getTAG(), "No session available");
      return;
    }
    
    List<String> sessionNames = new ArrayList<String>();
    
    for( CollabrifySession s : sessionList )
    {
      sessionNames.add(s.name());
    }

    final AlertDialog.Builder builder = new AlertDialog.Builder(collabActivity);

    builder.setTitle("Choose Session").setItems(sessionNames.toArray(new String[sessionList.size()]),
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
              Log.e(MainActivity.getTAG(), "error on choose session", e);
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
    Log.i(MainActivity.getTAG(), "Session created, id: " + id);
    collabActivity.setSessionId(id);
    collabActivity.runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        TheDevice.initialize();

        collabActivity.getEditTextArea().setText("");
        collabActivity.setTheText("");
        collabActivity.setContinuousCount(0);
      }
    });
  }

  @Override
  public void onError(CollabrifyException e)
  {
    Log.e(MainActivity.getTAG(), "error line 166 ", e);
  }

  @Override
  public void onSessionJoined(long maxOrderId, long baseFileSize)
  {
    Log.i(MainActivity.getTAG(), "Session Joined");
   /* if( baseFileSize > 0 )
    {
      collabActivity.setBaseFileReceiveBuffer(new ByteArrayOutputStream((int) baseFileSize));
    }*/
    collabActivity.runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        TheDevice.initialize();
        TheDevice.setOnDevice = false; 
        TheDevice.correctText = "";
        collabActivity.setTheText("");
        collabActivity.setContinuousCount(0);
      }
    });
  }
}
