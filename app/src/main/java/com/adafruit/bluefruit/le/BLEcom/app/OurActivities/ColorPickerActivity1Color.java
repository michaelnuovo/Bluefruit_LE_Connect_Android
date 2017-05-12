package com.adafruit.bluefruit.le.BLEcom.app.OurActivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.adafruit.bluefruit.le.BLEcom.R;
import com.adafruit.bluefruit.le.BLEcom.app.CommonHelpActivity;
import com.adafruit.bluefruit.le.BLEcom.app.OurActivities.PacketWrappers.Commands;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import java.util.ArrayList;

public class ColorPickerActivity1Color extends ColorPickerActivity {

    private final static String TAG = ColorPickerActivity2Colors.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker_1color);

        className = this.getClass().toString();

        viewList = new ArrayList<>();
        viewList.add(findViewById(R.id.rgbColorViewOne));

        mColorPicker = (ColorPicker) findViewById(R.id.colorPicker);
        SaturationBar mSaturationBar = (SaturationBar) findViewById(R.id.saturationbar);
        ValueBar mValueBar = (ValueBar) findViewById(R.id.valuebar);
        if (mColorPicker != null) { // Prevents null reference error?
            mColorPicker.addSaturationBar(mSaturationBar);
            mColorPicker.addValueBar(mValueBar);
            mColorPicker.setOnColorChangedListener(this);
        }

        Button randomizeButton = (Button) findViewById(R.id.randomizeButton);

        saveFirstDefaultColors(); // generates random colors and saves them to preferences
        setDefaultBackgroundColors(); // sets the randomly generated colors to the views

        saveFirstDefaultColorView(viewList.get(0)); // saves the default view id to memory
        setAndSaveColorPickerColors(); // sets and saves color picker color to memory

        setColorViewClickListeners(); // Button changes default color view

        setRandomButtonClickListener(randomizeButton); // Button randomizes colors

        useReceivedPacketValues(); // Sets a listener for receiving packets
    }

    @Override
    public void onStop() {

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
}
