package com.adafruit.bluefruit.le.BLEcom.app.Main.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.adafruit.bluefruit.le.BLEcom.R;
import com.adafruit.bluefruit.le.BLEcom.app.CommonHelpActivity;
import com.adafruit.bluefruit.le.BLEcom.app.Main.Objects.BluetoothDeviceData;
import com.adafruit.bluefruit.le.BLEcom.app.Main.Packets.Commands;
import com.adafruit.bluefruit.le.BLEcom.app.Main.Packets.PacketUtils;
import com.adafruit.bluefruit.le.BLEcom.app.Main.Packets.Palette1;
import com.adafruit.bluefruit.le.BLEcom.app.Main.Packets.Palette2;
import com.adafruit.bluefruit.le.BLEcom.app.Main.Packets.Palette4;
import com.adafruit.bluefruit.le.BLEcom.app.Main.Packets.Palette8;
import com.adafruit.bluefruit.le.BLEcom.app.UartInterfaceActivity;
import com.adafruit.bluefruit.le.BLEcom.ble.BleManager;
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

    static protected ColorPicker mColorPicker;
    static public ArrayList<View> viewList;
    static protected String className;
    static protected int selectedViewId;

    static BluetoothDeviceData dataSelectedForColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        prefs = getDefaultSharedPreferences(this);
        selectedViewId = viewList.get(0).getId();

        Button randomizeButton = (Button) findViewById(R.id.randomizeButton);
        Button sendButton = (Button) findViewById(R.id.sendButton);

        setColorsFromObject(); // set view colors to connected device color defaults
        setColorPickerColor(); // Sets color picker color to color of selected view

        setColorViewClickListeners(); // Button changes default color view
        setRandomButtonClickListener(randomizeButton); // Button randomizes colors
        setSendButtonClickListener(sendButton);

        onReceivedPacket(); // Sets a listener for receiving packets
    }

    /**
     * Code for the color wheel
     */

    protected void setColorPickerColor(){
//        View defaultColorView = returnDefaultColorView(); // Get the default view
//        int colorPickerColor = loadIntFromDefaultPreferences( // Get the default view's color
//                this.getClass() + String.valueOf(defaultColorView.getId()));    // from the prefs
        int colorPickerColor = ((ColorDrawable)findViewById(selectedViewId).getBackground()).getColor();
        mColorPicker.setOldCenterColor(colorPickerColor); // Set color wheel
        mColorPicker.setColor(colorPickerColor);          // with that color
    }

    @Override
    public void onColorChanged(int color) {
        //View defaultColorView = returnDefaultColorView(); // Get the default view
        findViewById(selectedViewId).setBackgroundColor(color); // Apply the color to the default view
//        saveIntToDefaultPreferences(this.getClass() + // Map the view id to the color in the prefs
//                String.valueOf(defaultColorView.getId()),color);
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

        Log.v(TAG,"saveFirstDefaultColors()");

        // Get the first object in the device data list if there is one
        ArrayList<BluetoothDeviceData> myConnectedDeviceData = BleManager.myConnectedDeviceData;
        if(myConnectedDeviceData.size() != 0){
            Toast.makeText(this, "Loading colors", Toast.LENGTH_SHORT).show();
            BluetoothDeviceData myConnectedDeviceDatum = myConnectedDeviceData.get(myConnectedDeviceData.size()-1);

            // Save object colors as view colors
            for (int i = 0; i < viewList.size(); i++) {
                String handle = "";
                handle += this.getClass();
                handle += String.valueOf(viewList.get(i).getId());
                saveIntToDefaultPreferences(handle, myConnectedDeviceDatum.colorArray[i]);

            }

        } else {
            // else initialize the view colors to the grey
            Log.v(TAG,"No connections");
            Toast.makeText(this, "No connected devices", Toast.LENGTH_SHORT).show();
            for (int i = 0; i < viewList.size(); i++) {
                String handle = "";
                handle += this.getClass();
                handle += String.valueOf(viewList.get(i).getId());
                saveIntToDefaultPreferences(handle, Color.parseColor("#7c7c7c"));
            }
        }

//        for( View view : viewList){
//            String handle = "";
//            handle += this.getClass();
//            handle += String.valueOf(view.getId());
//            int colorVal = loadIntFromDefaultPreferences(handle);
//            if(colorVal == -1) saveIntToDefaultPreferences(handle, getRandomColor());
//        }

        Log.v(TAG,"END");
    }

    /**
     * The colors sent out to the connected and active devices are derived from the first object
     * in the device data lsit
     */
    protected void setColorsFromObject(){
        Log.v(TAG,"setColorsFromObject()");

        // Get the first object in the device data list if there is one
        ArrayList<BluetoothDeviceData> myConnectedDeviceData = BleManager.myConnectedDeviceData;
        if(myConnectedDeviceData.size() != 0){
            Toast.makeText(this, "Loading colors", Toast.LENGTH_SHORT).show();
            dataSelectedForColor= myConnectedDeviceData.get(myConnectedDeviceData.size()-1);
            dataSelectedForColor.putParamsToUi();
        } else {
            // else initialize the view colors to the grey
            Log.v(TAG,"No connections");
            Toast.makeText(this, "No connected devices", Toast.LENGTH_SHORT).show();
            for(View view : viewList) view.setBackgroundColor(Color.parseColor("#7c7c7c"));
//            for (int i = 0; i < viewList.size(); i++) {
//                String handle = "";
//                handle += this.getClass();
//                handle += String.valueOf(viewList.get(i).getId());
//                saveIntToDefaultPreferences(handle, Color.parseColor("#7c7c7c"));
//            }
        }

//        int color;
//        for( View view : viewList) {
//            color = loadIntFromDefaultPreferences(this.getClass() + String.valueOf(view.getId()));
//            Log.v(TAG,"Color is "+String.valueOf(color));
//            view.setBackgroundColor(color);
//            //view.setBackgroundColor(0xff4286f4);
//            Log.v(TAG,"View id is "+String.valueOf(view.getId()));
//        }
        Log.v(TAG,"END");
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
     * Code for sending & receiving data
     */

    protected void onClickSend() {

        byte[] palette = null;

        // Overwrite color fields in objects and initialize the palette;
        switch(viewList.size()) {
            case 1 : {
                int color1 = ((ColorDrawable)findViewById(viewList.get(0).getId()).getBackground()).getColor();
                palette = new Palette1(color1).packet;
                for(BluetoothDeviceData deviceData : BleManager.myConnectedDeviceData) {
                    if(deviceData.selectedForTransmit == true) deviceData.colorArray[0] = color1;
                }
                break;
            }
            case 2 : {
                int color1 = ((ColorDrawable)findViewById(viewList.get(0).getId()).getBackground()).getColor();
                int color2 = ((ColorDrawable)findViewById(viewList.get(1).getId()).getBackground()).getColor();
                palette = new Palette2(color1,color2).packet;
                for(BluetoothDeviceData deviceData : BleManager.myConnectedDeviceData) {
                    if(deviceData.selectedForTransmit == true){
                        deviceData.colorArray[0] = color1;
                        deviceData.colorArray[1] = color2;
                    }
                }
                break;
            }
            case 4 : {
                int color1 = ((ColorDrawable)findViewById(viewList.get(0).getId()).getBackground()).getColor();
                int color2 = ((ColorDrawable)findViewById(viewList.get(1).getId()).getBackground()).getColor();
                int color3 = ((ColorDrawable)findViewById(viewList.get(2).getId()).getBackground()).getColor();
                int color4 = ((ColorDrawable)findViewById(viewList.get(3).getId()).getBackground()).getColor();
                palette = new Palette4(color1,color2,color3,color4).packet;
                for(BluetoothDeviceData deviceData : BleManager.myConnectedDeviceData) {
                    if(deviceData.selectedForTransmit == true) {
                        deviceData.colorArray[0] = color1;
                        deviceData.colorArray[1] = color2;
                        deviceData.colorArray[2] = color3;
                        deviceData.colorArray[3] = color4;
                    }
                }
                break;
            }
            case 8 : {
                int color1 = ((ColorDrawable)findViewById(viewList.get(0).getId()).getBackground()).getColor();
                int color2 = ((ColorDrawable)findViewById(viewList.get(1).getId()).getBackground()).getColor();
                int color3 = ((ColorDrawable)findViewById(viewList.get(2).getId()).getBackground()).getColor();
                int color4 = ((ColorDrawable)findViewById(viewList.get(3).getId()).getBackground()).getColor();
                int color5 = ((ColorDrawable)findViewById(viewList.get(4).getId()).getBackground()).getColor();
                int color6 = ((ColorDrawable)findViewById(viewList.get(5).getId()).getBackground()).getColor();
                int color7 = ((ColorDrawable)findViewById(viewList.get(6).getId()).getBackground()).getColor();
                int color8 = ((ColorDrawable)findViewById(viewList.get(7).getId()).getBackground()).getColor();
                palette = new Palette8(color1,color2,color3,color4,color5,color6,color7,color8).packet;
                for(BluetoothDeviceData deviceData : BleManager.myConnectedDeviceData) {
                    if(deviceData.selectedForTransmit == true) {
                        deviceData.colorArray[0] = color1;
                        deviceData.colorArray[1] = color2;
                        deviceData.colorArray[2] = color3;
                        deviceData.colorArray[3] = color4;
                        deviceData.colorArray[4] = color5;
                        deviceData.colorArray[5] = color6;
                        deviceData.colorArray[6] = color7;
                        deviceData.colorArray[7] = color8;
                    }
                }
                break;
            }
        }

        Toast.makeText(this,"Saving", Toast.LENGTH_SHORT).show();

        PacketUtils.logByteArray(palette);
        sendDataWithCRC(palette);
    }

    // If this activity was passed a bundle, it means that a pallet packet was received by the phone
    protected void onReceivedPacket(){
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int[] values = extras.getIntArray("values");

            int j = 0; int i = 0;
            while(i < values.length){
                if((i != 0) && (i % 3 == 0)) j++;
                viewList.get(j).setBackgroundColor(Color.rgb(values[i],values[i+1],values[i+2]));
                i+=3;
            }

//            View defaultColorView = returnDefaultColorView();
//            defaultColorView.setBackgroundColor(Color.rgb(values[0],values[1],values[2]));
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

                for(View view : viewList){

                    view.setBackgroundColor(getRandomColor());

//                    Log.v(TAG,"View id is "+String.valueOf(view.getId()));
//
//                    String handle = "";
//                    handle += getClassName();
//                    handle += String.valueOf(view.getId());
//                    colorVal = getRandomColor();
//                    saveIntToDefaultPreferences(handle, colorVal);
//                    Log.v(TAG,"Random color is "+String.valueOf(colorVal));
                }

                //setColorsFromObject();
                setColorPickerColor();
            }
        });
    }

    protected void setSendButtonClickListener(final Button sendButton){
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickSend();
            }
        });
    }

    protected void setColorViewClickListeners(){
        for(final View view : viewList){
            view.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    selectedViewId = view.getId();
//                    setColorPickerColor();
//                    int id;
//                    id = view.getId();
//                    saveIntToDefaultPreferences(getClassName() + "defaultColorView",id);
//                    // Set the color pickers colors and knob positions to the new default colors
//                    Log.v(TAG,"Default color view ID is "+String.valueOf(v));
//                    setColorPickerColor();
                }
            });
        }
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

    protected String getClassName(){
        return className;
    }
}
