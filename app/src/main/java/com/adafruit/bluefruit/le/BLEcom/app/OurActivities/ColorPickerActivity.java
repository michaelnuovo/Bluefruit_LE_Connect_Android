package com.adafruit.bluefruit.le.BLEcom.app.OurActivities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.adafruit.bluefruit.le.BLEcom.app.OurActivities.PacketWrappers.Commands;
import com.adafruit.bluefruit.le.BLEcom.app.OurActivities.PacketWrappers.Constants;
import com.adafruit.bluefruit.le.BLEcom.app.OurActivities.PacketWrappers.PacketUtils;
import com.adafruit.bluefruit.le.BLEcom.app.OurActivities.PacketWrappers.Palette1;
import com.adafruit.bluefruit.le.BLEcom.app.UartInterfaceActivity;
import com.larswerkman.holocolorpicker.ColorPicker;

import java.util.ArrayList;
import java.util.Random;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Created by michael on 5/10/17.
 */

public class ColorPickerActivity extends UartInterfaceActivity implements ColorPicker.OnColorChangedListener {

    private final static String TAG = ColorPickerActivity.class.getSimpleName();

    SharedPreferences prefs;

    protected ColorPicker mColorPicker;
    protected ArrayList<View> viewList;
    protected String className;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getDefaultSharedPreferences(this);
    }

    /**
     * Code for the color wheel
     */

    protected void setAndSaveColorPickerColors(){
        View defaultColorView = returnDefaultColorView(); // Get the default view
        int colorPickerColor = loadIntFromDefaultPreferences( // Get the default view's color
                this.getClass() + String.valueOf(defaultColorView.getId()));    // from the prefs
        mColorPicker.setOldCenterColor(colorPickerColor); // Set color wheel
        mColorPicker.setColor(colorPickerColor);          // with that color
    }

    @Override
    public void onColorChanged(int color) {
        View defaultColorView = returnDefaultColorView(); // Get the default view
        defaultColorView.setBackgroundColor(color); // Apply the color to the default view
        saveIntToDefaultPreferences(this.getClass() + // Map the view id to the color in the prefs
                String.valueOf(defaultColorView.getId()),color);
    }

    /**
     * Code for managing activity preferences
     */

    protected void saveIntToDefaultPreferences(String stringHandle, int intVal){
        Log.v(TAG,"saveIntToDefaultPreferences()");
        Log.v(TAG,"Handle is "+stringHandle);
        Log.v(TAG,"intVal "+String.valueOf(intVal));
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(stringHandle, intVal);
        editor.apply();
    }

    protected int loadIntFromDefaultPreferences(String stringHandle){
        return prefs.getInt(stringHandle,-1); // -1 is the default value
    }

    /**
     * Code for managing activity defaults
     */

    protected View returnDefaultColorView(){
        String handle = "";
        handle += this.getClass() + "defaultColorView";
        int viewId = loadIntFromDefaultPreferences(handle); // returns the saved view ID of the default view
        return findViewById(viewId); // return a reference to the default view
    }

    protected void saveFirstDefaultColorView(View view){
        String defaultViewHandle = this.getClass() + "defaultColorView";
        int viewId = loadIntFromDefaultPreferences(defaultViewHandle);
        if(viewId == -1) saveIntToDefaultPreferences(defaultViewHandle, view.getId());

    }

    protected void saveFirstDefaultColors(){

        String handle = "";
        int colorVal;

        for( View view : viewList){
            handle += this.getClass();
            handle += String.valueOf(view.getId());
            colorVal = loadIntFromDefaultPreferences(handle);
            if(colorVal == -1) saveIntToDefaultPreferences(handle, getRandomColor());
            Log.v(TAG,"colorVal is "+colorVal);
            Log.v(TAG,"color saved to preference is "+String.valueOf(loadIntFromDefaultPreferences(handle)));
            Log.v(TAG,"String handle is "+handle);
            Log.v(TAG,"View id is "+String.valueOf(view.getId()));
        }
    }

    protected void setDefaultBackgroundColors(){
        Log.v(TAG,"setDefaultBackgroundColors()");
        int color;
        for( View view : viewList) {
            color = loadIntFromDefaultPreferences(this.getClass() + String.valueOf(view.getId()));
            Log.v(TAG,"Color is "+String.valueOf(color));
            view.setBackgroundColor(color);
            //view.setBackgroundColor(0xff4286f4);
            Log.v(TAG,"View id is "+String.valueOf(view.getId()));
        }
    }

    private int getRandomColor() {

        Random rand = new Random();

        int r = rand.nextInt(255); // [0,255]
        int g = rand.nextInt(255); // [0,255]
        int b = rand.nextInt(255); // [0,255]

        int color = Color.rgb(r,g,b);

        return color;
    }

    /**
     * Coding for sending & receiving data
     */

    protected void onClickSend() {
        int color1 = loadIntFromDefaultPreferences(String.valueOf(viewList.get(0).getId()));
        byte[] palettePacket = new Palette1(color1).packet;
        PacketUtils.logByteArray(palettePacket);
        sendDataWithCRC(palettePacket);
        if(Constants.turnLEDSOffAfterSendingPallet == true) Commands.turnLightsOff();
    }

    protected void useReceivedPacketValues(){
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int[] values = extras.getIntArray("values");

            View defaultColorView = returnDefaultColorView();
            defaultColorView.setBackgroundColor(Color.rgb(values[0],values[1],values[2]));
            saveFirstDefaultColors();
        }
    }

    /**
     * Click event handlers
     */

    protected void setRandomButtonClickListener(Button randButton){

        randButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Log.v(TAG,"onClick()");

                int colorVal;
                String handle = "";

                for(View view : viewList){

                    Log.v(TAG,"View id is "+String.valueOf(view.getId()));

                    handle += getClassName();
                    handle += String.valueOf(view.getId());
                    colorVal = getRandomColor();
                    saveIntToDefaultPreferences(handle, colorVal);
                    Log.v(TAG,"Random color is "+String.valueOf(colorVal));
                }

                setDefaultBackgroundColors();
                setAndSaveColorPickerColors();
            }
        });
    }

    protected void setColorViewClickListeners(){
        for(final View view : viewList){
            view.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int id;
                    id = view.getId();
                    saveIntToDefaultPreferences(this.getClass() + "defaultColorView",id);
                    // Set the color pickers colors and knob positions to the new default colors
                    setAndSaveColorPickerColors();
                }
            });
        }
    }

    protected String getClassName(){
        return className;
    }
}
