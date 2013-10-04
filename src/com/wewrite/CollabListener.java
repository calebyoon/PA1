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
import com.wewrite.TheDevice.Operation;

import edu.umich.imlc.android.common.Utils;
import edu.umich.imlc.collabrify.client.CollabrifyAdapter;
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
    Log.i(MainActivity.getTAG(), "disconnected");
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
  public void onReceiveEvent(final long orderId, final int subId,
      String eventType, final byte[] data)
  {
    Utils.printMethodName(MainActivity.getTAG());
    Log.d(MainActivity.getTAG(), "RECEIVED SUB ID:" + subId);
    
    //if subid == mainactivity nextrevision
    // do something
    
    collabActivity.runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
         //handle events
        {
          try 
          {
            //pull all the data from the protocol buffer
            Event latestMove = Event.parseFrom(data);  
            int userWhoMadeMove = latestMove.getUserId();
            //if another user edits the document, all previous undo/redos
            //can not be guaranteed to be legal moves
            if (userWhoMadeMove != TheDevice.Id) 
            {
              TheDevice.undoList.clear();
              TheDevice.redoList.clear();
            }
            
            String moveData;
            int moveType = latestMove.getMoveType();
            int offsetValue = latestMove.getCursorChange();
            boolean undoValue = latestMove.getUndo();
            

            if (!TheDevice.cursorList.containsKey(userWhoMadeMove)) // new user
            {
              TheDevice.cursorList.put(userWhoMadeMove, TheDevice.cursorList.get(TheDevice.Id) );
            }
            
            // ---add----
            if (moveType == 1) 
            {
              moveData = latestMove.getData();
              if (userWhoMadeMove == TheDevice.Id && !undoValue) //local move, so add to UndoList
              {
                Commands com = new Commands(TheDevice.Operation.ADD, moveData, offsetValue);
                TheDevice.undoList.add(com);
              }
              TheDevice.AddShadow(userWhoMadeMove, offsetValue,
                  moveData);
            }
            // ---delete----
            else if (moveType == 2) 
            {
              moveData = latestMove.getData();
              if (userWhoMadeMove == TheDevice.Id && !undoValue) //local move, so add to UndoList
              {
                Commands com = new Commands(TheDevice.Operation.DELETE, moveData, offsetValue);
                TheDevice.undoList.add(com);
              }
              TheDevice.DeleteShadow(userWhoMadeMove, offsetValue);
            }
            // ---cursorChange----
            else
            {
              if (userWhoMadeMove == TheDevice.Id && !undoValue) //local move, so add to UndoList
              {
                Commands com = new Commands(TheDevice.Operation.CURSOR, null, offsetValue);
                TheDevice.undoList.add(com);
              }
              TheDevice.CursorChangeShadow(userWhoMadeMove,
                  offsetValue);
            }

            // if synchronize texteditor is needed
            if (userWhoMadeMove != TheDevice.Id || undoValue)
            {
              TheDevice.numDiffMove++;
            }
            
            
            if (TheDevice.lastsubId == subId) //come back to this. changed to final. might be a problem later
            {
              if (collabActivity.getContinuousCount() == 0 && TheDevice.numDiffMove > 0) // if local user is not typing
              {
                TheDevice.Synchronize();
              }
              else if (TheDevice.numDiffMove > 0)// if local user is typing, sync later
              {               
                TheDevice.needToSynchronize = true;
              }
              else //nothing is different from shadow
              {
                TheDevice.lastsubId = -1;
              }
            }
            
          } 
          catch (InvalidProtocolBufferException e) {
            Log.i("failed", "bad parse attempt: " + e);
            e.printStackTrace();
          }
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
        //createSession.setEnabled(false);
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