package com.adafruit.bluefruit.le.BlEcom.app.OurActivities;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.support.v7.widget.AppCompatEditText;

public class MyEditText extends AppCompatEditText {

    /* Must use this constructor in order for the layout files to instantiate the class properly */
    public MyEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean onKeyPreIme (int keyCode, KeyEvent event)
    {
        // Return true if I handle the event:
        // In my case i want the keyboard to not be dismissible so i simply return true
        // Other people might want to handle the event differently
        System.out.println("onKeyPreIme " +event);
        return true;
    }


}
