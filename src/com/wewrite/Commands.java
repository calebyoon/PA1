package com.wewrite;

import android.util.Log;

import com.wewrite.EventProtos.Event;

public class Commands
{
  TheDevice.EventType operation;
  public int offset;
  public String mes;
  
  public Commands(TheDevice.EventType operationT, String mesT, int offsetT)
  {
    operation = operationT;
    mes = mesT;
    offset = offsetT;  
  }
  
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
    {
      if (undo == 1)
      {
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
      }
    }
    return event;  
  }
 }