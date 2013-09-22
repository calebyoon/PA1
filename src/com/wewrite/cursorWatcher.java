package com.wewrite;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
//import android.widget.Toast;

public class cursorWatcher extends EditText
{

  public cursorWatcher(Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs, defStyle);

  }

  public cursorWatcher(Context context, AttributeSet attrs)
  {
    super(context, attrs);

  }

  public cursorWatcher(Context context)
  {
    super(context);

  }


  @Override
  protected void onSelectionChanged(int selStart, int selEnd)
  {
    //Toast.makeText(getContext(),
    //    "selStart is " + selStart + "selEnd is " + selEnd, Toast.LENGTH_LONG)
    //    .show();
  }
}
