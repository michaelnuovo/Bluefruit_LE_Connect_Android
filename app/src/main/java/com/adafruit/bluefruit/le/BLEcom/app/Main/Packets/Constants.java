package com.adafruit.bluefruit.le.BLEcom.app.Main.Packets;

/**
 * Created by michael on 4/20/17.
 */

public class Constants {

    public static final char DELIMITER = '#';

    static public final int DELIMTER_ONE = 0xAA;
    static public final int DELIMTER_TWO = 0x55;

    static public final int PATTERN_MAX = 14;
    static public final int SECONDS_MAX = 99;
    static public final int GAMMA_MAX = 8;

    static public boolean turnLEDSOffAfterSendingPallet = false;

    public enum PacketTypes {
        NO_COMMAND,//0
        USER_COMMAND,//1
        PACK_ACCEL,//2
        PACK_GYRO,//3
        PACK_MAG,//4
        PACK_QUAT,//5
        PACK_GPS,//6
        PACK_BUTTONS,//7
        PACK_PAL_1,//8
        PACK_PAL_2,//9
        PACK_PAL_4,//10
        PACK_PAL_8,//11
        PACK_PAT,
        COMMAND_END
    }
}
