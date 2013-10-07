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
  protected static int Id = new Random().nextInt();
  protected static cursorWatcher editTextArea;
  protected static boolean manuallySet = true;
  protected static int cursorLoc = 0;

  public static String finalText = "";

  public static int lastEditor = -1;
  public static boolean needToSynchronize = false;
  public static int numDiffMove = 0;

  protected static Stack<Commands> undoList = new Stack<Commands>(),
      redoList = new Stack<Commands>();

  public static Map<Integer, Integer> cursorList = new HashMap<Integer, Integer>();

  public enum Operation
  {
    ADD, DELETE, CURSOR, INIT
  }

  protected static void initialize()
  {
    manuallySet = true;
    cursorLoc = 0;

    finalText = "";
    lastEditor = -1;
    needToSynchronize = false;
    numDiffMove = 0;

    undoList = new Stack<Commands>();
    redoList = new Stack<Commands>();

    cursorList = new HashMap<Integer, Integer>();

    cursorList.put(Id, 0);
  }

  // update the displayed text with the proper text from finalText copy
  protected static void Synchronize()
  {
    Log.d("wewrite", "59");
    manuallySet = false;
    Log.d("wewrite", "61");
    editTextArea.setText(finalText);
    Log.d("wewrite", "63 " + cursorList.get(Id));
    editTextArea.setSelection(cursorList.get(Id));
    Log.d("wewrite", "65");
    cursorLoc = cursorList.get(Id);
    Log.d("wewrite", "67");

    needToSynchronize = false;
    lastEditor = -1;
    numDiffMove = 0;

  }

  /*
   * implementation only called for finalText on RECEIVING Events
   */
  // update the "finalText copy" when an add is received
  protected static void AddfinalText(int userId, int count, String msg)
  {
    int finalTextCursor = cursorList.get(userId);
    finalText = finalText.substring(0, finalTextCursor) + msg
        + finalText.substring(finalTextCursor, finalText.length());
    Log.d("finalText", finalText);
    for( Map.Entry entry : cursorList.entrySet() )
    {
      if( (Integer) entry.getValue() >= finalTextCursor )
        cursorList.put((Integer) entry.getKey(), (Integer) entry.getValue()
            + count);
    }
  }

  // update the "finalText copy" when a delete is received
  protected static void DeletefinalText(int userId, int count)
  {
    int finalTextCursor = cursorList.get(userId);

    if( finalTextCursor - count < 0 )
      finalText = finalText.substring(finalTextCursor, finalText.length());
    else
      finalText = finalText.substring(0, finalTextCursor - count)
          + finalText.substring(finalTextCursor, finalText.length());
    Log.d("finalText", finalText);
    for( Map.Entry entry : cursorList.entrySet() )
    {
      if( (Integer) entry.getValue() >= finalTextCursor )
        cursorList.put((Integer) entry.getKey(),
            Math.max((Integer) entry.getValue() - count, 0));
      else if( (Integer) entry.getValue() <= finalTextCursor - count )
      {
      }
      else
        cursorList.put((Integer) entry.getKey(),
            Math.max(finalTextCursor - count, 0));
    }
  }

  // update the "finalText copy" when a cursor change is received
  protected static void CursorChangefinalText(int userId, int offset)
  {
    int toPosition = cursorList.get(userId) + offset;

    if( toPosition < 0 )
      toPosition = 0;
    else if( toPosition > finalText.length() )
      toPosition = finalText.length();

    cursorList.put(userId, toPosition);
  }

  /*
   * undo//redo when button pushed
   */
  protected static Commands Undo()
  {
    if( !undoList.empty() )
    {
      Commands com = undoList.lastElement();
      redoList.push(undoList.pop());
      return com;
    }
    else
      // if undo list is empty
      return null;
  }

  protected static Commands Redo()
  {
    if( !redoList.empty() )
    {
      Commands com = redoList.lastElement();
      undoList.push(redoList.pop());
      return com;
    }
    else
      // redo list is empty
      return null;
  }

}
