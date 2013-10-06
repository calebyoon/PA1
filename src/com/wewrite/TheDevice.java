package com.wewrite;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import com.wewrite.Commands;

import android.util.Log;
import android.widget.EditText;


public class TheDevice
{
  protected static int deviceId = new Random().nextInt(); 
  protected static cursorWatcher editTextArea;
  protected static boolean setOnDevice = true;
  protected static int cursorPos = 0;
  public static String correctText = ""; 
  public static int lastsubId = -1;
  public static boolean unmatched = false;  
  public static int numDiffMove = 0;
  
  public static Map<Integer, Integer> cursors = new HashMap<Integer, Integer>(); 
  protected static Stack<Commands> undoList = new Stack<Commands> (); 
  protected static Stack<Commands> redoList = new Stack<Commands> ();
  
  public enum EventType
  {
    ADD, DELETE, CURSOR, INIT
  }

  protected static void initialize()
  { 
      setOnDevice = true;
      cursorPos = 0;
      correctText = "";
      lastsubId = -1;
      unmatched = false;  
      numDiffMove = 0;
      
      undoList = new Stack<Commands> (); 
      redoList = new Stack<Commands> ();
      
      cursors = new HashMap<Integer, Integer>();
      cursors.put(deviceId, 0);
  }

  protected static void match()
  {
    setOnDevice = false;
    editTextArea.setText(correctText);
    editTextArea.setSelection(cursors.get(deviceId));
    cursorPos =  cursors.get(deviceId);

    unmatched = false;
    lastsubId = -1;
    numDiffMove = 0;

  }

  protected static Commands Undo()
  { 
      if (!undoList.empty())
      {
        Commands com = undoList.lastElement();   
        redoList.push(undoList.pop());
        return com;
      } 
      else         
        return null;
 }
   
  protected static Commands Redo()
  {  
    if (!redoList.empty())
    {
      Commands com = redoList.lastElement();    
      undoList.push(redoList.pop());      
      return com;
    } 
    else 
      return null;
  }
  
  protected static void addToCorrectText(int userId, int count, String msg)
  { 
    int shadowCursor = cursors.get(userId);
    correctText = correctText.substring(0,shadowCursor) + msg + correctText.substring(shadowCursor, correctText.length());

    for (Map.Entry entry : cursors.entrySet())
    { 
      if ((Integer)entry.getValue() >= shadowCursor)
        cursors.put((Integer)entry.getKey(), (Integer)entry.getValue() + count);
    }
  } 

  protected static void deleteFromCorrectText(int userId, int count) 
  {
    int cursor = cursors.get(userId);
    
    if (cursor - count < 0)
      correctText = correctText.substring(cursor, correctText.length());
    else
      correctText = correctText.substring(0, cursor - count) + correctText.substring(cursor, correctText.length());

    for (Map.Entry entry : cursors.entrySet())
    { 
      if ((Integer)entry.getValue() >= cursor)
        cursors.put( (Integer)entry.getKey(), Math.max((Integer)entry.getValue() - count, 0) );
      else if ((Integer)entry.getValue() <= cursor - count)
      {}
      else
        cursors.put((Integer)entry.getKey(), Math.max(cursor - count, 0) );
    } 
  }
  
  protected static void changeCursorCorrectText(int userId, int offset)
  {
    int dest = cursors.get(userId) + offset;
    
    if (dest < 0)
      dest = 0;
    else if (dest > correctText.length())
      dest = correctText.length();
    
    cursors.put(userId, dest);
  }  
}