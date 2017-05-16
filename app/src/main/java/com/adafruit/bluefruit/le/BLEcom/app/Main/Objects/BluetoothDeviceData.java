package com.adafruit.bluefruit.le.BLEcom.app.Main.Objects;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.graphics.Color;
import android.view.View;

import com.adafruit.bluefruit.le.BLEcom.app.Main.Activities.ColorPickerActivity;

import java.util.ArrayList;
import java.util.UUID;



public class BluetoothDeviceData {

    // Michael's variables
    public int mConnectionState = 0;
    public boolean selectedForTransmit = false;
    public BluetoothGatt connection = null;

    public int[] colorArray = new int[8];

    public BluetoothDeviceData(){
        colorArray[0] = Color.parseColor("#f44268");
        colorArray[1] = Color.parseColor("#ff008c");
        colorArray[2] = Color.parseColor("#7141f4");
        colorArray[3] = Color.parseColor("#00f6ff");
        colorArray[4] = Color.parseColor("#00ff33");
        colorArray[5] = Color.parseColor("#fffa00");
        colorArray[6] = Color.parseColor("#ff0c00");
        colorArray[7] = Color.parseColor("#ff008c");
    }

    public void getParamsFromUi(){
        // retrieves palette size and HSV values from user interface
    }

    public void putParamsToUi(){
        // puts palette size and HSV values to user interface
        ArrayList<View> viewList = ColorPickerActivity.viewList;
        for(int i = 0;i < viewList.size(); i++)
            viewList.get(i).setBackgroundColor(colorArray[i]);
    }

    /**
     * BluetoothDevice represents a remote Bluetooth device. A BluetoothDevice lets you create a connection with
     * the respective device or query information about it, such as the name, address, class,
     * and bonding state. This class is really just a thin wrapper for a Bluetooth hardware
     * address. Objects of this class are immutable. Operations on this class are performed on
     * the remote Bluetooth hardware address, using the BluetoothAdapter that was used to create
     * this BluetoothDevice. To get a BluetoothDevice, use BluetoothAdapter.getRemoteDevice(String)
     * to create one representing a device of a known MAC address (which you can get through
     * device discovery with BluetoothAdapter) or get one from the set of bonded devices
     * returned by BluetoothAdapter.getBondedDevices(). You can then open a BluetoothSocket for
     * communication with the remote device, using createRfcommSocketToServiceRecord(UUID).
     */
    public BluetoothDevice device;

    public int rssi; // RSSI (Received Signal Strength Indicator) is a common name for the
    // signal strength in a wireless network environment. It is a measure of
    // the power level that a RF client device is receiving from an access
    // point, for example.
    public byte[] scanRecord;
    public String advertisedName;           // Advertised name
    public String cachedNiceName;
    private String cachedName;

    // Decoded scan record (update R.array.scan_devicetypes if this list is modified)
    public static final int kType_Unknown = 0;
    public static final int kType_Uart = 1;
    public static final int kType_Beacon = 2;
    public static final int kType_UriBeacon = 3;



    public int type;
    public int txPower;
    public ArrayList<UUID> uuids;

    public String getName() {
        if (cachedName == null) {
            cachedName = device.getName();
            if (cachedName == null) {
                cachedName = advertisedName;      // Try to get a name (but it seems that if
                // device.getName() is null, this is also null)
            }
        }

        return cachedName;
    }

    public String getNiceName() {
        if (cachedNiceName == null) {
            cachedNiceName = getName();
            if (cachedNiceName == null) {
                cachedNiceName = device.getAddress();
            }
        }

        return cachedNiceName;
    }

}

//Methods declared inside interface are implicitly public
//And all variables declared in the interface are implicitly public static final (constants).
interface UiParams {
    void getParamsFromUi();
    void putParamsToUi();
}


class Suspenders extends BluetoothDeviceData implements UiParams {

    public int paletteSize; // which size palette is active?
    public int hsv; // what is the data type of HSV

    public void getParamsFromUi(){
        // retreives palette size and HSV values from user interface
    }

    public void putParamsToUi(){
        // puts palette size and HSV values to user interface
    }
}