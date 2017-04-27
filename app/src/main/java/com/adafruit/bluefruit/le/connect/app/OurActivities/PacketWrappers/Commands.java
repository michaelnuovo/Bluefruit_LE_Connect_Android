package com.adafruit.bluefruit.le.connect.app.OurActivities.PacketWrappers;

import com.adafruit.bluefruit.le.connect.app.UartActivity;
import com.adafruit.bluefruit.le.connect.app.UartInterfaceActivity;

/**
 * Created by michael on 4/26/17.
 */

public class Commands extends UartInterfaceActivity {

    public static void turnLightsOff(){
        byte[] packet = new UserCommand("off,0",null,null,null).toPacket();
        sendDataWithCRC(packet);
    }
}
