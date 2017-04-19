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

import java.nio.ByteBuffer;

public class ColorPickerActivity2Colors extends UartInterfaceActivity implements ColorPicker.OnColorChangedListener {
    // Log
    private final static String TAG = ColorPickerActivity2Colors.class.getSimpleName();

    // Constants
//    private final static boolean kPersistValues = true;
    private final static String kPreferences = "ColorPickerActivity_prefs";
    private final static String kPreferences_color = "color";

    private final static int kFirstTimeColor = 0x0000ff;
    private final static int defaultColor = 0x0000ff;

    // UI
    private ColorPicker mColorPicker; // The circular hue selector
    private View mRgbColorView1; // The rectangular color display
    private View mRgbColorView2;
    private TextView mRgbTextView1; // Text view displaying RGB values of selected color
    private TextView mRgbTextView2;

    private int mSelectedColor;
    private String mySelectedView;

    private int mSelectedColor1; // The int value of the selected color
    private int mSelectedColor2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker_2colors);

        mBleManager = BleManager.getInstance(this);

        // UI
        mRgbColorView1 = findViewById(R.id.rgbColorView1);
        mRgbColorView2 = findViewById(R.id.rgbColorView2);


        mRgbTextView1 = (TextView) findViewById(R.id.rgbTextView1);
        mRgbTextView2 = (TextView) findViewById(R.id.rgbTextView2);

        SaturationBar mSaturationBar = (SaturationBar) findViewById(R.id.saturationbar);
        ValueBar mValueBar = (ValueBar) findViewById(R.id.valuebar);
        mColorPicker = (ColorPicker) findViewById(R.id.colorPicker);

        if (mColorPicker != null) { // Prevents null reference error?
            mColorPicker.addSaturationBar(mSaturationBar);
            mColorPicker.addValueBar(mValueBar);
            mColorPicker.setOnColorChangedListener(this);
        }

        // Shared preferences

        SharedPreferences preferences = getSharedPreferences(kPreferences, Context.MODE_PRIVATE);
        mSelectedColor1 = preferences.getInt(kPreferences_color, kFirstTimeColor); // kFirstTimeColor is the default value to return

        mySelectedView = preferences.getString(mySelectedView, ""); // default return value is an empty string
        mSelectedColor = preferences.getInt(mySelectedView, defaultColor); // if mySelectedView is an empty string, the default color will be returned



        mColorPicker.setOldCenterColor(mSelectedColor);
        mColorPicker.setColor(mSelectedColor);
        onColorChanged(mSelectedColor);

        // Start services
        onServicesDiscovered();
    }

    private class mView {

        String ID;
        int color;


    }

    @Override
    public void onStop() {
        // Preserve values

            SharedPreferences settings = getSharedPreferences(kPreferences, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(kPreferences_color, mSelectedColor1);
            editor.apply();

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

    @Override
    public void onColorChanged(int color) {

        // Save selected color
        mSelectedColor1 = color;

        // Update UI
        mRgbColorView1.setBackgroundColor(color);

        final int r = (color >> 16) & 0xFF;
        final int g = (color >> 8) & 0xFF;
        final int b = (color >> 0) & 0xFF;
        final String text = String.format(getString(R.string.colorpicker_rgbformat), r, g, b);
        mRgbTextView1.setText(text);
    }

    public void onClickSend(View view) {
        // Set the old color
        mColorPicker.setOldCenterColor(mSelectedColor1);

        // Send selected color !Crgb
        byte r = (byte) ((mSelectedColor1 >> 16) & 0xFF);
        byte g = (byte) ((mSelectedColor1 >> 8) & 0xFF);
        byte b = (byte) ((mSelectedColor1 >> 0) & 0xFF);

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
