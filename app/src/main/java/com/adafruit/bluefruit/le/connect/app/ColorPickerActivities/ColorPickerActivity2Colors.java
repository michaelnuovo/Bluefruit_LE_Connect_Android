package com.adafruit.bluefruit.le.connect.app.ColorPickerActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.adafruit.bluefruit.le.connect.R;
import com.adafruit.bluefruit.le.connect.app.CommonHelpActivity;
import com.adafruit.bluefruit.le.connect.app.UartInterfaceActivity;
import com.adafruit.bluefruit.le.connect.ble.BleManager;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public class ColorPickerActivity2Colors extends UartInterfaceActivity implements ColorPicker.OnColorChangedListener {

    // Log
    private final static String TAG = ColorPickerActivity2Colors.class.getSimpleName();

    // Constants
    private final static String classPrefs = ColorPickerActivity2Colors.class.getName();
    private final static int defaultColor = 0x0000ff;

    // Widgets
    private ColorPicker mColorPicker; // This is the color wheel widget
    private ViewHolder viewHolder = new ViewHolder(); // A view holder for the color views

    // Data
    int currentSelectedColor; // The current selected color of the color wheel

    // View holder class
    private class ViewHolder {
        private View mRgbColorView1; // This will be the default color view if there is no default
        private View mRgbColorView2;
        private TextView mRgbTextView1; // This will be the default text view if there is no default
        private TextView mRgbTextView2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker_2colors);

        mBleManager = BleManager.getInstance(this);

        // UI
        viewHolder.mRgbColorView1 = findViewById(R.id.rgbColorView1);
        viewHolder.mRgbColorView2 = findViewById(R.id.rgbColorView2);

        viewHolder.mRgbTextView1 = (TextView) findViewById(R.id.rgbTextView1);
        viewHolder.mRgbTextView2 = (TextView) findViewById(R.id.rgbTextView2);

        SaturationBar mSaturationBar = (SaturationBar) findViewById(R.id.saturationbar);
        ValueBar mValueBar = (ValueBar) findViewById(R.id.valuebar);
        mColorPicker = (ColorPicker) findViewById(R.id.colorPicker);

        if (mColorPicker != null) { // Prevents null reference error?
            mColorPicker.addSaturationBar(mSaturationBar);
            mColorPicker.addValueBar(mValueBar);
            mColorPicker.setOnColorChangedListener(this);
        }

        // Sets the defaults the FIRST time the activity opens
        saveDefaultColor();
        saveDefaultColorView();
        saveDefaultText();
        saveDefaultTextView();

        // Sets background colors and text to their defaults EVERY time the activity opens
        setBackgroundColors();
        setText();

        int defaultColor = returnDefaultColor();
        mColorPicker.setOldCenterColor(defaultColor);
        mColorPicker.setColor(defaultColor); // Sets position of color wheel

        Log.v("TAG","default color here is "+String.valueOf(defaultColor));

        onColorChanged(defaultColor); // Sets the text and color in the interface method

        onServicesDiscovered(); // Start services

        setClickListeners();
    }

    private void setBackgroundColors(){

    }

    private void setText(){

    }

    private void setClickListeners(){
        setListener(viewHolder.mRgbColorView1);
        setListener(viewHolder.mRgbColorView2);
    }

    private void setListener(View colorView){
        colorView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Log.v("TAG","Click event handled");
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

    private void saveDefaultColor(){
        int colorVal = loadFromPreferences("defaultColor");
        if(colorVal == -1)
            saveToPreferences("defaultColor", 0x0000FF);
        Log.v("TAG","colorVal is "+String.valueOf(colorVal));
    }

    private void saveDefaultColor(int color){
        saveToPreferences("defaultColor", color);
    }

    private int returnDefaultColor(){
        int colorVal = loadFromPreferences("defaultColor");
        return colorVal;
    }

    private void saveDefaultTextView(){
        int viewId = loadFromPreferences("defaultTextView");
        if(viewId == -1)
            saveToPreferences("defaultTextView", viewHolder.mRgbTextView1.getId());
    }

    private void saveDefaultText(){
        int colorVal = loadFromPreferences("defaultText");
        if(colorVal == -1)
            saveToPreferences("defaultText", defaultColor);
    }

    private TextView returnDefaultTextView(){
        int viewId = loadFromPreferences("defaultTextView");
        return (TextView) findViewById(viewId);
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

        final int r = (color >> 16) & 0xFF;
        final int g = (color >> 8) & 0xFF;
        final int b = (color >> 0) & 0xFF;

        final String text = String.format(getString(R.string.colorpicker_rgbformat), r, g, b);

        Log.v("TAG","text is "+text);

        currentSelectedColor = color;

        View colorView = returnDefaultColorView();
        colorView.setBackgroundColor(color);

        TextView textView = returnDefaultTextView();
        textView.setText(text);
    }



    @Override
    public void onStop() {
        // If the user selects a new colorView and closes the activity we need to save the
        // currentSelectedColor as the defaultColor so that when the activity is re-opened
        // the currentSelectedColor will be set as the default color.
        saveDefaultColor(currentSelectedColor);

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
        // Set the old color
        mColorPicker.setOldCenterColor(currentSelectedColor);

        // Send selected color !Crgb
        byte r = (byte) ((currentSelectedColor >> 16) & 0xFF);
        byte g = (byte) ((currentSelectedColor >> 8) & 0xFF);
        byte b = (byte) ((currentSelectedColor >> 0) & 0xFF);

        ByteBuffer buffer = ByteBuffer.allocate(2 + 3 * 1).order(java.nio.ByteOrder.LITTLE_ENDIAN);

        // prefix
        String prefix = "!C";
        buffer.put(prefix.getBytes());

        // values
        buffer.put(r);
        buffer.put(g);
        buffer.put(b);

        byte[] result = buffer.array();
        sendDataWithCRC(result);
    }
}
