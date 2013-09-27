package com.wewrite;

import java.util.Stack;
import android.widget.EditText;
import android.content.Context;
import android.util.AttributeSet;
import android.text.Editable;
import android.text.TextWatcher;

class DocEditText extends EditText {
	public DocEditText(Context context, AttributeSet attrs, int defStyle) {
	      super(context, attrs, defStyle);

	  }

	  public DocEditText(Context context, AttributeSet attrs) 
	  {
	      super(context, attrs);
	  }

	  public DocEditText(Context context) 
	  {
	      super(context);
	  }
	
	  private class DocEditTextListener implements TextWatcher
	  {

	    @Override
	    public void onTextChanged(CharSequence s, int start, int before, int count) 
	    {
	    	//This method is called to notify you that, within s, the count characters 
	    	//beginning at start have just replaced old text that had length before.
	    
	    	String input = s.toString();
	    	//check if length of string to put in is longer/shorter/same as current and 	
	    
	    }

		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
			//This method is called to notify you that, somewhere within s, the text has been changed.
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// TODO Auto-generated method stub
			//This method is called to notify you that, within s, the count characters 
			//beginning at start are about to be replaced by new text with length after.
		}
	    
	  }
	  
	//get event
	  //display event locally
	  //put event on total event stack
	  //if from this user
	  	//put event on user's event stack
	  	//send to server
	
	//get event confirmation from server
	//while the current object at top of stack is not the confirmed one
		//pop off stack (keep track of these in order)
	  	//do opposite of that action
	  	//make sure to pop off and redo the action you're looking for
	
}