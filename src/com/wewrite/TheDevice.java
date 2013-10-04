package com.wewrite;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import com.wewrite.Commands;

//import android.annotation.SuppressLint;
import android.widget.EditText;


public class TheDevice 
{
  protected static int Id = new Random().nextInt(); 
  protected static EditText to_broadcast;
  protected static boolean isTextSetManually = true;
  protected static int cursorLoc = 0;

//a string only updated when an event is received
//thus, it is always the "correct" string
  public static String shadow = ""; 

  public static int lastsubId = -1;
  public static boolean needToSynchronize = false;  
  public static int numDiffMove = 0;


  protected static Stack<Commands> 
      undoList = new Stack<Commands> (), 
      redoList = new Stack<Commands> ();

  //@SuppressLint("UseSparseArrays")
  public static Map<Integer, Integer> cursorList = new HashMap<Integer, Integer>(); 


  public enum Operation
  {
    ADD, DELETE, CURSOR, INIT
  }


  protected static void initialize()
  { 
    isTextSetManually = true;
    cursorLoc = 0;

    shadow = "";
    lastsubId = -1;
    needToSynchronize = false;  
    numDiffMove = 0;

      undoList = new Stack<Commands> (); 
      redoList = new Stack<Commands> ();

      cursorList = new HashMap<Integer, Integer>();

      cursorList.put(Id, 0);
  }


  //update the displayed text with the proper text from shadow copy
  protected static void Synchronize()
  {
    isTextSetManually = false;
    to_broadcast.setText(shadow);
    to_broadcast.setSelection(cursorList.get(Id));
    cursorLoc =  cursorList.get(Id);

    needToSynchronize = false;
    lastsubId = -1;
    numDiffMove = 0;

  }



  /*
   * implementation only called for SHADOW on RECEIVING Events
   */
  //update the "shadow copy" when an add is received
  protected static void AddShadow(int userId, int count, String msg)
  { 
    int shadowCursor = cursorList.get(userId);
    shadow = 
      shadow.substring(0,shadowCursor) + msg + shadow.substring(shadowCursor, shadow.length());

    for (Map.Entry entry : cursorList.entrySet())
    { 
      if ((Integer)entry.getValue() >= shadowCursor)
      {
        cursorList.put((Integer)entry.getKey(), (Integer)entry.getValue()+count);
      }
      System.out.println(cursorList.get((Integer)entry.getKey()) );
    }

    System.out.println("ADD from user " + userId + ": " + msg + " @ " + shadowCursor + " in shadow");
    System.out.println("shadow status: cursor: " + cursorList.get(Id) + " content: " + shadow);
  } 

 
  //update the "shadow copy" when a delete is received
  protected static void DeleteShadow(int userId, int count) 
  {
    int shadowCursor = cursorList.get(userId);
    if (shadowCursor - count < 0)
    {
      shadow = shadow.substring(shadowCursor, shadow.length() );
    }
    else
    {
      shadow = shadow.substring(0, shadowCursor-count) + shadow.substring(shadowCursor, shadow.length());
    }

    for (Map.Entry entry : cursorList.entrySet())
    { 
      if ((Integer)entry.getValue() >= shadowCursor)
      {
        cursorList.put( (Integer)entry.getKey(), Math.max( (Integer)entry.getValue()-count, 0) );
      }
      else if ((Integer)entry.getValue() <= shadowCursor - count)
      {}
      else
      {
        cursorList.put((Integer)entry.getKey(), Math.max(shadowCursor-count, 0) );
      }
    }


    System.out.println("DELETE from user " + userId + "of length " + count + " @ " + shadowCursor + " in shadow");
    System.out.println("shadow status: cursor: " + cursorList.get(Id) + " content: " + shadow);
  }
  
  
   //update the "shadow copy" when a cursor change is received
  protected static void CursorChangeShadow(int userId, int offset)
  {
    int toPosition = cursorList.get(userId) + offset;
    if (toPosition < 0)
      toPosition = 0;
    else if (toPosition > shadow.length())
      toPosition = shadow.length();
    cursorList.put(userId, toPosition);

    System.out.println("CURSOR CHANGE from user " + userId + " in shadow");
    System.out.println("shadow status: cursor: " + cursorList.get(Id) + " content: " + shadow);
  }



  /*
   * undo//redo when button pushed
   */
  protected static Commands Undo()
    { 
      if (!undoList.empty())
      {
        Commands com = undoList.lastElement();   
        System.out.println("user send UNDO: " + com.operation + com.mes + com.offset);
        redoList.push(undoList.pop());
        return com;
      } 
      else // if undo list is empty
      {
        System.out.print("Nothing to undo! ");
        System.out.println("# of undo/redo left: " + undoList.size() + " / " + redoList.size());        
        return null;
      }
    }
  
  
  
  protected static Commands Redo()
  {  
    if (!redoList.empty())
    {
      Commands com = redoList.lastElement();    
      System.out.println("user send REDO: " + com.operation + com.mes + com.offset);
      undoList.push(redoList.pop());      
      return com;
    } 
    else // redo list is empty
    {
      System.out.print("Nothing to redo! "); 
      System.out.println("Num of undo/redo left: " + undoList.size() + " / " + redoList.size());
      return null;
    }
  }
  
}