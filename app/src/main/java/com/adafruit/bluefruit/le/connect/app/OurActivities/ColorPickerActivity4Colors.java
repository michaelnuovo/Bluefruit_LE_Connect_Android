package com.adafruit.bluefruit.le.connect.app.OurActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.adafruit.bluefruit.le.connect.R;
import com.adafruit.bluefruit.le.connect.app.CommonHelpActivity;
import com.adafruit.bluefruit.le.connect.app.OurActivities.PacketWrappers.Commands;
import com.adafruit.bluefruit.le.connect.app.OurActivities.PacketWrappers.Constants;
import com.adafruit.bluefruit.le.connect.app.OurActivities.PacketWrappers.PacketUtils;
import com.adafruit.bluefruit.le.connect.app.OurActivities.PacketWrappers.Palette2;
import com.adafruit.bluefruit.le.connect.app.OurActivities.PacketWrappers.Palette4;
import com.adafruit.bluefruit.le.connect.app.UartInterfaceActivity;
import com.adafruit.bluefruit.le.connect.ble.BleManager;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import java.util.ArrayList;
import java.util.Random;

public class ColorPickerActivity4Colors extends UartInterfaceActivity implements ColorPicker.OnColorChangedListener {
    // Log
    private final static String TAG = ColorPickerActivity2Colors.class.getSimpleName();

    // Constants
    private final static String classPrefs = ColorPickerActivity4Colors.class.getName();
    //private final static int defaultColor = 0xFFF0FF00;
    //private final static int defaultColor = 0xffffff00;
    private final static int defaultColor = 0xff4A14CC;


    // Widgets
    private ColorPicker mColorPicker; // This is the color wheel widget
    private ViewHolder viewHolder = new ViewHolder(); // A view holder for the color views

    // Data
    //int currentSelectedColor; // The current selected color of the color wheel

    // View holder class
    private class ViewHolder {

        public ArrayList<View> viewsList = new ArrayList<>();

        public View mRgbColorView1; // This will be the default color view if there is no default
        public View mRgbColorView2;
        public View mRgbColorView3;
        public View mRgbColorView4;

        public void pushViewsToList(){

            viewsList.add(mRgbColorView1);
            viewsList.add(mRgbColorView2);
            viewsList.add(mRgbColorView3);
            viewsList.add(mRgbColorView4);
        }
    }

    /**
     *  Structure of of class preferences
     *
     *  String defaultColorView : int id  <-- default color view string handle maps to the view id of type int
     *  String view_1_id : int colorValue <-- each view has the string value of its id mapped to a color value of type int
     *  String view_2_id : int colorValue
     *  ....
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker_4colors);

        mBleManager = BleManager.getInstance(this);

        // UI
        viewHolder.mRgbColorView1 = findViewById(R.id.rgbColorViewOne);
        viewHolder.mRgbColorView2 = findViewById(R.id.rgbColorViewTwo);
        viewHolder.mRgbColorView3 = findViewById(R.id.rgbColorViewThree);
        viewHolder.mRgbColorView4 = findViewById(R.id.rgbColorViewFour);

        viewHolder.pushViewsToList();

        SaturationBar mSaturationBar = (SaturationBar) findViewById(R.id.saturationbar);
        ValueBar mValueBar = (ValueBar) findViewById(R.id.valuebar);
        mColorPicker = (ColorPicker) findViewById(R.id.colorPicker);

        if (mColorPicker != null) { // Prevents null reference error?
            mColorPicker.addSaturationBar(mSaturationBar);
            mColorPicker.addValueBar(mValueBar);
            mColorPicker.setOnColorChangedListener(this);
        }

        // Sets the defaults the FIRST time the activity opens
        saveDefaultColors();
        setBackgroundColors();
        saveDefaultColorView();

        // Sets background colors and text to their defaults EVERY time the activity opens
        setColorsPickerColors();



        setClickListeners();

        Button randomizeButton = (Button) findViewById(R.id.randomizeButton);
        setRandomButtonClickListener(randomizeButton);

        //onServicesDiscovered(); // Start services
    }

    private void setRandomButtonClickListener(Button randButton){
        randButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Random rand = new Random();

                for(View view : viewHolder.viewsList){

                    int r = rand.nextInt(255); // [0,255]
                    int g = rand.nextInt(255); // [0,255]
                    int b = rand.nextInt(255); // [0,255]

                    int color = Color.rgb(r,g,b);
                    saveToPreferences(String.valueOf(view.getId()),color);
                }

                setBackgroundColors();;
            }
        });
    }

    private void setColorsPickerColors(){
        View defaultColorView = returnDefaultColorView();
        int colorPickerColor = loadFromPreferences(String.valueOf(defaultColorView.getId()));
        mColorPicker.setOldCenterColor(colorPickerColor);
        mColorPicker.setColor(colorPickerColor); // Sets position of color wheel
    }

    private void setBackgroundColors(){
        for( View view : viewHolder.viewsList)
            view.setBackgroundColor(loadFromPreferences(String.valueOf(view.getId())));
    }

    private void saveDefaultColors(){

        String stringyId;
        int colorVal;

        for( View view : viewHolder.viewsList){
            stringyId = String.valueOf(view.getId());
            colorVal = loadFromPreferences(stringyId);
            if(colorVal == -1) saveToPreferences(stringyId, defaultColor);
        }
    }

    private void setClickListeners(){
        for( View view : viewHolder.viewsList)
            setListener(view);
    }

    private void setListener(final View colorView){
        colorView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                int id;

                id = colorView.getId();
                saveToPreferences("defaultColorView",id);

                // Set the color pickers colors and knob positions to the new default colors
                setColorsPickerColors();
            }
        });
    }

    private void saveDefaultColorView(){
        int viewId = loadFromPreferences("defaultColorView");
        if(viewId == -1) saveToPreferences("defaultColorView", viewHolder.mRgbColorView1.getId());

    }

    private View returnDefaultColorView(){
        int viewId = loadFromPreferences("defaultColorView");
        return findViewById(viewId);
    }

    private int returnDefaultColor(){
        return loadFromPreferences(String.valueOf(returnDefaultColorView().getId()));
    }

    private void saveToPreferences(String stringHandle, int intVal){
        SharedPreferences preferences = getSharedPreferences(classPrefs, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(stringHandle, intVal);
        editor.apply();
    }

    private int loadFromPreferences(String stringHandle){
        SharedPreferences preferences = getSharedPreferences(classPrefs, Context.MODE_PRIVATE);
        return preferences.getInt(stringHandle,-1);
    }

    // This method is called when the color on the color wheel (color picker) is changed
    @Override
    public void onColorChanged(int color) {
        //currentSelectedColor = color;
        View defaultColorView = returnDefaultColorView();
        defaultColorView.setBackgroundColor(color);
        saveToPreferences(String.valueOf(defaultColorView.getId()),color);
    }

    @Override
    public void onStop() {
        // If the user selects a new colorView and closes the activity we need to save the
        // currentSelectedColor as the defaultColor so that when the activity is re-opened
        // the currentSelectedColor will be set as the default color.
        //saveDefaultColor(currentSelectedColor);
        //saveToPreferences(String.valueOf(returnDefaultColorView().getId()),currentSelectedColor);

        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_color_picker, menu);
        MenuItem myButton = menu.findItem(R.id.button);
        AppCompatButton button = (AppCompatButton) myButton.getActionView();
        button.setText("OFF");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Commands.turnLightsOff();
            }
        });
        return true;
    }


    private void startHelp() {
        // Launch help activity
        Intent intent = new Intent(this, CommonHelpActivity.class);
        intent.putExtra("title", getString(R.string.colorpicker_help_title));
        intent.putExtra("help", "colorpicker_help.html");
        startActivity(intent);
    }

    @Override
    public void onDisconnected() {
        super.onDisconnected();
        Log.d(TAG, "Disconnected. Back to previous activity");
        setResult(-1);      // Unexpected Disconnect
        finish();
    }

    public void onClickSend(View view) {

        int color1 = loadFromPreferences(String.valueOf(viewHolder.mRgbColorView1.getId()));
        int color2 = loadFromPreferences(String.valueOf(viewHolder.mRgbColorView2.getId()));
        int color3 = loadFromPreferences(String.valueOf(viewHolder.mRgbColorView3.getId()));
        int color4 = loadFromPreferences(String.valueOf(viewHolder.mRgbColorView4.getId()));

        byte[] palettePacket = new Palette4(color1, color2, color3, color4).packet;
        sendDataWithCRC(palettePacket);
        if(Constants.turnLEDSOffAfterSendingPallet == true) Commands.turnLightsOff();
    }
}
