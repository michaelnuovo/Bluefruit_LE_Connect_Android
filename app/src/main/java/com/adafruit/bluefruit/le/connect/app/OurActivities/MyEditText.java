package com.adafruit.bluefruit.le.connect.app.OurActivities;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.support.v7.widget.AppCompatEditText;

public class MyEditText extends AppCompatEditText {

    public MyEditText( Context context )
    {
        super( context );
    }

    public MyEditText( Context context, AttributeSet attribute_set )
    {
        super( context, attribute_set );
    }

    public MyEditText( Context context, AttributeSet attribute_set, int def_style_attribute )
    {
        super( context, attribute_set, def_style_attribute );
    }

    @Override
    public boolean onKeyPreIme( int key_code, KeyEvent event )
    {
//        if ( event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP )
//            this.clearFocus();

        return super.onKeyPreIme( key_code, event );
    }


}
