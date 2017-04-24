package com.adafruit.bluefruit.le.connect.app.OurActivities;

import android.bluetooth.BluetoothGattService;
import android.graphics.Color;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by michael on 4/19/17.
 */

public class PacketUtils {

    static public final int DELIMTER_ONE = 0xAA;
    static public final int DELIMTER_TWO = 0x55;

    static public final char DELIMETER = '#';

    protected BluetoothGattService mUartService;


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

    // e.g. test,2\n1234,5678,9012\n345.678,abcdefg,9876\n
    public static byte[] userCommandPacket(String command) {
        command = command.replace("\n","^").replace("\r","_");
        char[] commandChars = command.toCharArray();
        byte[] result = new byte[1 + 1 + 1 + commandChars.length];
        result[0] = DELIMETER;
        result[1] = (byte) PacketTypes.USER_COMMAND.ordinal();
        result[2] = (byte) commandChars.length;
        for(int i = 0; i < commandChars.length; i++)
            if(commandChars[i] == '^')
                result[i+3] = 10;
            else if(commandChars[i] == '_')
                result[i+3] = 13;
            else
                result[i+3] = (byte) commandChars[i];
        return result;
    }

    public static byte[] palettePacket(int color){

        ByteBuffer buffer = ByteBuffer.allocate(3 + 3 * 1).order(java.nio.ByteOrder.LITTLE_ENDIAN);

        int delimeterOne = PacketUtils.DELIMTER_ONE;
        int delimeterTwo = PacketUtils.DELIMTER_TWO;

        buffer.put((byte) delimeterOne);
        buffer.put((byte) delimeterTwo);
        buffer.put((byte) PacketTypes.PAL_1.ordinal());

        buffer.put((byte) Color.red(color));
        buffer.put((byte) Color.green(color));
        buffer.put((byte) Color.blue(color));

        return buffer.array();
    }

    public static byte[] palettePacket(int color1, int color2){
        byte[] result = new byte[3 + 3 * 2];
        result[0] = DELIMETER;
        result[1] = (byte) PacketTypes.PAL_2.ordinal();
        result[2] = (byte) Color.red(color1);
        result[3] = (byte) Color.blue(color1);
        result[4] = (byte) Color.green(color1);
        result[5] = (byte) Color.red(color2);
        result[6] = (byte) Color.blue(color2);
        result[7] = (byte) Color.green(color2);
        return result;
    }

    public static byte[] palettePacket(int color1, int color2, int color3, int color4){
        byte[] result = new byte[3 + 3 * 4];
        result[0] = DELIMETER;
        result[1] = (byte) PacketTypes.PAL_4.ordinal();
        result[2] = (byte) Color.red(color1);
        result[3] = (byte) Color.blue(color1);
        result[4] = (byte) Color.green(color1);
        result[5] = (byte) Color.red(color2);
        result[6] = (byte) Color.blue(color2);
        result[7] = (byte) Color.green(color2);
        result[8] = (byte) Color.red(color3);
        result[9] = (byte) Color.blue(color3);
        result[10] = (byte) Color.green(color3);
        result[11] = (byte) Color.red(color4);
        result[12] = (byte) Color.blue(color4);
        result[13] = (byte) Color.green(color4);
        return result;
    }

    public static byte[] palettePacket(int color1, int color2, int color3, int color4,
                                        int color5, int color6, int color7, int color8){
        byte[] result = new byte[3 + 3 * 8];
        result[0] = DELIMETER;
        result[1] = (byte) PacketTypes.PAL_8.ordinal();
        result[2] = (byte) Color.red(color1);
        result[3] = (byte) Color.blue(color1);
        result[4] = (byte) Color.green(color1);
        result[5] = (byte) Color.red(color2);
        result[6] = (byte) Color.blue(color2);
        result[7] = (byte) Color.green(color2);
        result[8] = (byte) Color.red(color3);
        result[9] = (byte) Color.blue(color3);
        result[10] = (byte) Color.green(color3);
        result[11] = (byte) Color.red(color4);
        result[12] = (byte) Color.blue(color4);
        result[13] = (byte) Color.green(color4);
        result[11] = (byte) Color.red(color5);
        result[12] = (byte) Color.blue(color5);
        result[13] = (byte) Color.green(color5);
        result[14] = (byte) Color.red(color6);
        result[15] = (byte) Color.blue(color6);
        result[16] = (byte) Color.green(color6);
        result[17] = (byte) Color.red(color7);
        result[18] = (byte) Color.blue(color7);
        result[19] = (byte) Color.green(color7);
        result[20] = (byte) Color.red(color8);
        result[21] = (byte) Color.blue(color8);
        result[22] = (byte) Color.green(color8);
        return result;
    }

    public static void logByteArray(byte[] byteArray){
        Log.v("TAG","---- Byte Array -----");
        for(Byte b : byteArray) Log.v("TAG","byte : "+String.valueOf(b));
        Log.v("TAG","---- End ------------");
    }


    public static String getPacketStats(byte[] packet){

        return  packetToTextString(packet);
    }

    public static String packetToTextString(byte[] byteArray){

        String result = "";

        Log.v("TAG","byteArray[2] is " + String.valueOf(byteArray[2]));

        for(int i = 0; i < byteArray.length ; i++)
            if(i == 1)
                result += String.valueOf(byteArray[1]);
            else if(i == 2)
                result += String.valueOf(byteArray[2]);
            else {
                if(byteArray[i] == 10)
                    result += "\\n";
                else if (byteArray[i] == 13)
                    result += "\\r";
                else
                    result += String.valueOf((char) byteArray[i]);
            }

        Log.v("TAG","result is "+result);

        return result;
    }


}
