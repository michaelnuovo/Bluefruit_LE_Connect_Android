package com.adafruit.bluefruit.le.connect.app.OurActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.adafruit.bluefruit.le.connect.R;
import com.adafruit.bluefruit.le.connect.app.CommonHelpActivity;
import com.adafruit.bluefruit.le.connect.app.UartInterfaceActivity;
import com.adafruit.bluefruit.le.connect.ble.BleManager;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import java.nio.ByteBuffer;

public class ColorPickerActivity2Colors extends UartInterfaceActivity implements ColorPicker.OnColorChangedListener {

    // Log
    private final static String TAG = ColorPickerActivity2Colors.class.getSimpleName();

    // Constants
    private final static String classPrefs = ColorPickerActivity2Colors.class.getName();
    private final static int defaultColor = 0xFFF0FF00;

    // Widgets
    private ColorPicker mColorPicker; // This is the color wheel widget
    private ViewHolder viewHolder = new ViewHolder(); // A view holder for the color views

    // Data
    int currentSelectedColor; // The current selected color of the color wheel

    // View holder class
    private class ViewHolder {
        private View mRgbColorView1; // This will be the default color view if there is no default
        private View mRgbColorView2;
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
        setContentView(R.layout.activity_color_picker_2colors);

        mBleManager = BleManager.getInstance(this);

        // UI
        viewHolder.mRgbColorView1 = findViewById(R.id.rgbColorViewOne);
        viewHolder.mRgbColorView2 = findViewById(R.id.rgbColorViewTwo);

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
        saveDefaultColorView();

        // Sets background colors and text to their defaults EVERY time the activity opens
        setBackgroundColors();

        setColorsPickerColors();

        onServicesDiscovered(); // Start services

        setClickListeners();
    }

    private void setColorsPickerColors(){
        View defaultColorView = returnDefaultColorView();
        int colorPickerColor = loadFromPreferences(String.valueOf(defaultColorView.getId()));
        mColorPicker.setOldCenterColor(colorPickerColor);
        mColorPicker.setColor(colorPickerColor); // Sets position of color wheel
    }

    private void setBackgroundColors(){
        viewHolder.mRgbColorView1.setBackgroundColor(loadFromPreferences(String.valueOf(viewHolder.mRgbColorView1.getId())));
        viewHolder.mRgbColorView2.setBackgroundColor(loadFromPreferences(String.valueOf(viewHolder.mRgbColorView2.getId())));
    }

    private void saveDefaultColors(){

        String stringyId;
        int colorVal;

        stringyId = String.valueOf(viewHolder.mRgbColorView1.getId());
        colorVal = loadFromPreferences(stringyId);
        if(colorVal == -1) saveToPreferences(stringyId, defaultColor);

        stringyId = String.valueOf(viewHolder.mRgbColorView2.getId());
        colorVal = loadFromPreferences(stringyId);
        if(colorVal == -1) saveToPreferences(stringyId, defaultColor);
    }

    private void setClickListeners(){
        setListener(viewHolder.mRgbColorView1);
        setListener(viewHolder.mRgbColorView2);
    }

    private void setListener(final View colorView){
        colorView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                int id;
                int colorVal;
                String stringyId;

                // Save current selected color as default color before switching views
                id = returnDefaultColorView().getId();
                stringyId = String.valueOf(id);
                colorVal = currentSelectedColor;
                saveToPreferences(stringyId,colorVal);

                // Overwrite new default color view id
                id = colorView.getId();
                saveToPreferences("defaultColorView",id);

                // Set the color pickers colors and knob positions to the new default colors
                setColorsPickerColors();
            }
        });
    }

    private void saveDefaultColorView(){
        int viewId = loadFromPreferences("defaultColorView");
        if(viewId == -1)
            saveToPreferences("defaultColorView", viewHolder.mRgbColorView1.getId());

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
        currentSelectedColor = color;
        returnDefaultColorView().setBackgroundColor(color);
    }



    @Override
    public void onStop() {
        // If the user selects a new colorView and closes the activity we need to save the
        // currentSelectedColor as the defaultColor so that when the activity is re-opened
        // the currentSelectedColor will be set as the default color.
        //saveDefaultColor(currentSelectedColor);
        saveToPreferences(String.valueOf(returnDefaultColorView().getId()),currentSelectedColor);

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

        //mColorPicker.setOldCenterColor(currentSelectedColor);  // Set the old color
        Log.v("TAG","currentSelectedColor value is "+String.valueOf(currentSelectedColor));  // Set the old color

        int color1 = loadFromPreferences(String.valueOf(viewHolder.mRgbColorView1.getId()));
        int color2 = loadFromPreferences(String.valueOf(viewHolder.mRgbColorView2.getId()));

        int numberOfColors = 2;

        byte[] byteArr = new byte[3 * numberOfColors];

        byteArr[0] = (byte) ((color1 >> (16 + 8 * 0)) & 0xFF);
        byteArr[1] = (byte) ((color1 >> (16 + 8 * 0)) & 0xFF);
        byteArr[2] = (byte) ((color1 >> (16 + 8 * 0)) & 0xFF);

        byteArr[0] = (byte) ((color2 >> (16 + 8 * 1)) & 0xFF);
        byteArr[1] = (byte) ((color2 >> (16 + 8 * 1)) & 0xFF);
        byteArr[2] = (byte) ((color2 >> (16 + 8 * 1)) & 0xFF);

        byte[] packet = PacketUtils.byteArrayToPacket(byteArr,PacketUtils.PacketTypes.PAL_2);
        sendDataWithCRC(packet);
    }
}
