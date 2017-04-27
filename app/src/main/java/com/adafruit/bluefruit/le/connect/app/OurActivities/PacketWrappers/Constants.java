package com.adafruit.bluefruit.le.connect.app.OurActivities.PacketWrappers;

/**
 * Created by michael on 4/20/17.
 */

public class Constants {

    public static final char DELIMITER = '#';

    static public final int DELIMTER_ONE = 0xAA;
    static public final int DELIMTER_TWO = 0x55;


    static public boolean turnLEDSOffAfterSendingPallet = false;

    public enum PacketTypes {
        NONE,//0
        USER_COMMAND,//1
        ACCEL,//2
        GYRO,//3
        MAG,//4
        QUAT,//5
        GPS,//6
        BUTTONS,//7
        PAL_1,//8
        PAL_2,//9
        PAL_4,//10
        PAL_8//11
    }

}
