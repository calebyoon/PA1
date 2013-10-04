package com.wewrite;

import com.wewrite.EventProtos.Event;

//create user or variant that edits and synchronizes the text


public class Commands
{
  TheDevice.Operation operation;
  public int offset;
  public String mes;
  
  public Commands(TheDevice.Operation operationT, String mesT, int offsetT)
  {
    operation = operationT;
    mes = mesT;
    offset = offsetT;  
  }
  
  //create a move in the protocol buffer format
  //to be sent to the Collabrify Client
  public Event generateMoveMes(int undo)
  {
    Event event;
    if (undo != 1 && this.operation == TheDevice.Operation.ADD || 
      (undo == 1  && this.operation == TheDevice.Operation.DELETE)) 
    //either an add, or an undo on a delete
    {
      event = Event.newBuilder()
          .setUserId(TheDevice.Id) 
          .setMoveType(1)
          .setData(this.mes)
          .setCursorChange(this.offset)
          .setUndo(undo)
          .build();
    }
    else if(undo != 1 && this.operation == TheDevice.Operation.DELETE || 
        (undo == 1 && this.operation == TheDevice.Operation.ADD))
      //either a delete, or an undo on an add
    {
      event = Event.newBuilder()
          .setUserId(TheDevice.Id)
          .setMoveType(2)
          .setData(this.mes)
          .setCursorChange(this.offset)
          .setUndo(undo)
          .build();
    }
    else //a cursor move
    {
      if (undo == 1)
      {
        event = Event.newBuilder()
            .setUserId(TheDevice.Id)
            .setMoveType(3)
            .setCursorChange(-this.offset)
            .setUndo(undo)
            .build();
      }
      else
      {
        event = Event.newBuilder()
        .setUserId(TheDevice.Id) //need a user id
        .setMoveType(3)
        .setCursorChange(this.offset)
        .setUndo(undo)
        .build();
      }
    }
    return event;  
  }
 }