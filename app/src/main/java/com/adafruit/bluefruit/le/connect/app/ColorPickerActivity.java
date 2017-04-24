package com.adafruit.bluefruit.le.connect.app;

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
import com.adafruit.bluefruit.le.connect.ble.BleManager;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import java.nio.ByteBuffer;

public class ColorPickerActivity extends UartInterfaceActivity implements ColorPicker.OnColorChangedListener {
    // Log
    private final static String TAG = ColorPickerActivity.class.getSimpleName();

    // Constants
    private final static boolean kPersistValues = true;
    private final static String kPreferences = "ColorPickerActivity_prefs";
    private final static String kPreferences_color = "color";

    private final static int kFirstTimeColor = 0x0000ff;

    // UI
    private ColorPicker mColorPicker; // The circular hue selector
    private View mRgbColorView; // The rectangular color display
    private TextView mRgbTextView; // Text view displaying RGB values of selected color

    private int mSelectedColor; // The int value of the selected color

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);

        mBleManager = BleManager.getInstance(this);

        // UI
        mRgbColorView = findViewById(R.id.rgbColorView);
        mRgbTextView = (TextView) findViewById(R.id.rgbTextView);

        SaturationBar mSaturationBar = (SaturationBar) findViewById(R.id.saturationbar);
        ValueBar mValueBar = (ValueBar) findViewById(R.id.valuebar);
        mColorPicker = (ColorPicker) findViewById(R.id.colorPicker);

        if (mColorPicker != null) { // Prevents null reference error?
            mColorPicker.addSaturationBar(mSaturationBar);
            mColorPicker.addValueBar(mValueBar);
            mColorPicker.setOnColorChangedListener(this);
        }

        if (kPersistValues) {
            SharedPreferences preferences = getSharedPreferences(kPreferences, Context.MODE_PRIVATE);
            mSelectedColor = preferences.getInt(kPreferences_color, kFirstTimeColor);
        } else {
            mSelectedColor = kFirstTimeColor;
        }

        mColorPicker.setOldCenterColor(mSelectedColor);
        mColorPicker.setColor(mSelectedColor);
        onColorChanged(mSelectedColor);

        // Start services
        onServicesDiscovered();
    }

    @Override
    public void onStop() {
        // Preserve values
        if (kPersistValues) {
            SharedPreferences settings = getSharedPreferences(kPreferences, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(kPreferences_color, mSelectedColor);
            editor.apply();
        }

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
        mSelectedColor = color;

        // Update UI
        mRgbColorView.setBackgroundColor(color);

        final int r = (color >> 16) & 0xFF;
        final int g = (color >> 8) & 0xFF;
        final int b = (color >> 0) & 0xFF;
        final String text = String.format(getString(R.string.colorpicker_rgbformat), r, g, b);
        mRgbTextView.setText(text);
    }

    public void onClickSend(View view) {

        // The color we're sending becomes the old color, so let's set the old color to the new color we're sending
        mColorPicker.setOldCenterColor(mSelectedColor);

        // Send selected color !Crgb
        byte r = (byte) ((mSelectedColor >> 16) & 0xFF);
        byte g = (byte) ((mSelectedColor >> 8) & 0xFF);
        byte b = (byte) ((mSelectedColor >> 0) & 0xFF);

        // red
        byte r1 = (byte) 0xFF;
        byte g1 = (byte) 0x00;
        byte b1 = (byte) 0x00;

        // Creates a buffer of size 2 + 3 = 5
        // Each byte in the buffer reads from left to right (big endian), not right to left (little endian)
        ByteBuffer buffer = ByteBuffer.allocate(2 + 3 * 1).order(java.nio.ByteOrder.LITTLE_ENDIAN);

        // old prefix
        String prefix = "!C";
        buffer.put(prefix.getBytes()); // insert this
        buffer.put(r);
        buffer.put(g);
        buffer.put(b);

        String DELIMETER = "#";

        //  PAL_1 => 8
        buffer.put(DELIMETER.getBytes()); // Pushes bytes to the array
        buffer.put((byte) 8); // Pushes bytes to the array
        buffer.put(r);
        buffer.put(g);
        buffer.put(b);

        //  PAL_2 => 9
        buffer.put(DELIMETER.getBytes()); // Pushes bytes to the array
        buffer.put((byte) 9); // Pushes bytes to the array
        buffer.put(r);
        buffer.put(g);
        buffer.put(b);
        buffer.put(r1);
        buffer.put(g1);
        buffer.put(b1);

        //  PAL_4 => 10
        buffer.put(DELIMETER.getBytes()); // Pushes bytes to the array
        buffer.put((byte) 10); // Pushes bytes to the array
        buffer.put(r);
        buffer.put(g);
        buffer.put(b);
        buffer.put(r1);
        buffer.put(g1);
        buffer.put(b1);
        buffer.put(r);
        buffer.put(g);
        buffer.put(b);
        buffer.put(r1);
        buffer.put(g1);
        buffer.put(b1);

        //  PAL_8 => 11
        buffer.put(DELIMETER.getBytes()); // Pushes bytes to the array
        buffer.put((byte) 11); // Pushes bytes to the array
        buffer.put(r);
        buffer.put(g);
        buffer.put(b);
        buffer.put(r1);
        buffer.put(g1);
        buffer.put(b1);
        buffer.put(r);
        buffer.put(g);
        buffer.put(b);
        buffer.put(r1);
        buffer.put(g1);
        buffer.put(b1);
        buffer.put(r);
        buffer.put(g);
        buffer.put(b);
        buffer.put(r1);
        buffer.put(g1);
        buffer.put(b1);
        buffer.put(r);
        buffer.put(g);
        buffer.put(b);
        buffer.put(r1);
        buffer.put(g1);
        buffer.put(b1);


        byte[] result = buffer.array(); // Converts the buffer into a byte array
        sendDataWithCRC(result);
    }
}


