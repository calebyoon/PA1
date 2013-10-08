package com.wewrite;

import android.util.Log;

import com.wewrite.EventProtos.Event;

public class Commands
{
  TheDevice.EventType operation;
  public int offset;
  public String mes;
<<<<<<< HEAD

  public Commands(TheDevice.Operation operationT, String mesT, int offsetT)
=======
  
  public Commands(TheDevice.EventType operationT, String mesT, int offsetT)
>>>>>>> c624e9cddff429ce37da161ff83459444382a2ca
  {
    operation = operationT;
    mes = mesT;
    offset = offsetT;
  }
<<<<<<< HEAD

  // create a move in the protocol buffer format
  // to be sent to the Collabrify Client
  public Event generateMoveMes(int undo)
  {
    Event event;
    if( undo != 1 && this.operation == TheDevice.Operation.ADD
        || (undo == 1 && this.operation == TheDevice.Operation.DELETE) )
    // either an add, or an undo on a delete
    {
      event = Event.newBuilder().setUserId(TheDevice.Id).setMoveType(1)
          .setData(this.mes).setCursorChange(this.offset).setUndo(undo).build();
    }
    else if( undo != 1 && this.operation == TheDevice.Operation.DELETE
        || (undo == 1 && this.operation == TheDevice.Operation.ADD) )
    // either a delete, or an undo on an add
    {
      event = Event.newBuilder().setUserId(TheDevice.Id).setMoveType(2)
          .setData(this.mes).setCursorChange(this.offset).setUndo(undo).build();
    }
    else
    // a cursor move
=======
  
  public Event generateMoveMes(int undo)
  {
    Event event;
    
    if(undo != 1 && this.operation == TheDevice.EventType.DELETE || 
        (undo == 1 && this.operation == TheDevice.EventType.ADD))
    {
      event = Event.newBuilder()
          .setUserId(TheDevice.deviceId)
          .setMoveType(2)
          .setData(this.mes)
          .setCursorChange(this.offset)
          .setUndo(undo)
          .build();
    }
    else if (undo != 1 && this.operation == TheDevice.EventType.ADD || 
      (undo == 1  && this.operation == TheDevice.EventType.DELETE)) 
    {
      event = Event.newBuilder()
          .setUserId(TheDevice.deviceId) 
          .setMoveType(1)
          .setData(this.mes)
          .setCursorChange(this.offset)
          .setUndo(undo)
          .build();
    }
    else 
>>>>>>> c624e9cddff429ce37da161ff83459444382a2ca
    {
      if( undo == 1 )
      {
<<<<<<< HEAD
        event = Event.newBuilder().setUserId(TheDevice.Id).setMoveType(3)
            .setCursorChange(-this.offset).setUndo(undo).build();
      }
      else
      {
        event = Event.newBuilder().setUserId(TheDevice.Id) // need a user id
            .setMoveType(3).setCursorChange(this.offset).setUndo(undo).build();
=======
        event = Event.newBuilder()
            .setUserId(TheDevice.deviceId)
            .setMoveType(3)
            .setCursorChange(-this.offset)
            .setUndo(undo)
            .build();
      }
      else
      {
        event = Event.newBuilder()
        .setUserId(TheDevice.deviceId) 
        .setMoveType(3)
        .setCursorChange(this.offset)
        .setUndo(undo)
        .build();
>>>>>>> c624e9cddff429ce37da161ff83459444382a2ca
      }
    }
    return event;
  }
}
