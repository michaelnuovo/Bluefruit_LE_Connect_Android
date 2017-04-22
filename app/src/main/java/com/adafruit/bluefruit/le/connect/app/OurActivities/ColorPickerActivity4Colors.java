package com.adafruit.bluefruit.le.connect.app.OurActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.adafruit.bluefruit.le.connect.R;
import com.adafruit.bluefruit.le.connect.app.CommonHelpActivity;
import com.adafruit.bluefruit.le.connect.app.UartInterfaceActivity;
import com.adafruit.bluefruit.le.connect.ble.BleManager;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

public class ColorPickerActivity4Colors extends UartInterfaceActivity implements ColorPicker.OnColorChangedListener {
    // Log
    private final static String TAG = ColorPickerActivity2Colors.class.getSimpleName();

    // Constants
    private final static String classPrefs = ColorPickerActivity2Colors.class.getName();
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

        onServicesDiscovered(); // Start services

        setClickListeners();

        Button randomizeButton = (Button) findViewById(R.id.randomizeButton);
        setRandomButtonClickListener(randomizeButton);
    }

    private void setRandomButtonClickListener(Button randButton){
        randButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Random rand = new Random();

                for(View view : viewHolder.viewsList){

                    int r = rand.nextInt(255); // [0,255]
                    int g = rand.nextInt(255); // [0,255]
                    int b = rand.nextInt(255); // [0,255]


                }
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
            Log.v("TAG","@#ER# "+String.valueOf(colorVal));
            if(colorVal == -1) saveToPreferences(stringyId, defaultColor);
            Log.v("TAG","@#ER# "+String.valueOf(loadFromPreferences(stringyId)));
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_color_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_help) {
            startHelp();
            return true;
        }

        return super.onOptionsItemSelected(item);
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

        int color;
        int bitIdx = -1;
        int numberOfColors = 4;

        byte[] byteArr = new byte[3 * numberOfColors];
        color = loadFromPreferences(String.valueOf(viewHolder.mRgbColorView1.getId()));
        bitIdx += 1;

        Log.v("TAG","col val is "+String.valueOf(color));

        byteArr[0 + 3 * bitIdx] = (byte) Color.red(color);
        byteArr[1 + 3 * bitIdx] = (byte) Color.green(color);
        byteArr[2 + 3 * bitIdx] = (byte) Color.blue(color);

        Log.v("TAG","@#$" +String.valueOf((byte) Color.blue(-11922228)));
        Log.v("TAg","String.valeOf()"+ String.valueOf(Color.blue(color)));

        color = loadFromPreferences(String.valueOf(viewHolder.mRgbColorView2.getId()));
        bitIdx += 1;

        byteArr[0 + 3 * bitIdx] = (byte) Color.red(color);
        byteArr[1 + 3 * bitIdx] = (byte) Color.green(color);
        byteArr[2 + 3 * bitIdx] = (byte) Color.blue(color);

        color = loadFromPreferences(String.valueOf(viewHolder.mRgbColorView3.getId()));
        bitIdx += 1;

        byteArr[0 + 3 * bitIdx] = (byte) Color.red(color);
        byteArr[1 + 3 * bitIdx] = (byte) Color.green(color);
        byteArr[2 + 3 * bitIdx] = (byte) Color.blue(color);

        color = loadFromPreferences(String.valueOf(viewHolder.mRgbColorView4.getId()));
        bitIdx += 1;

        byteArr[0 + 3 * bitIdx] = (byte) Color.red(color);
        byteArr[1 + 3 * bitIdx] = (byte) Color.green(color);
        byteArr[2 + 3 * bitIdx] = (byte) Color.blue(color);

        for(byte b : byteArr) Log.v("TAG","b is : "+String.valueOf(b));

        byte[] packet = PacketUtils.byteArrayToPacket(byteArr,PacketUtils.PacketTypes.PAL_2);
        sendDataWithCRC(packet);
    }
}
