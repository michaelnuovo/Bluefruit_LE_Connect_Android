package com.adafruit.bluefruit.le.BLEcom.app.Main.Packets;

import android.view.View;

import com.adafruit.bluefruit.le.BLEcom.app.Main.Activities.ColorPickerActivity;
import com.adafruit.bluefruit.le.BLEcom.app.UartInterfaceActivity;

/**
 * Created by michael on 4/26/17.
 */

public class Commands extends UartInterfaceActivity {

    public static void turnLightsOff(){
        byte[] packet = new UserCommand("off,0",null,null,null).toPacket();
        sendDataWithCRC(packet);
    }
}
