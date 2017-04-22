package com.adafruit.bluefruit.le.connect.app.OurActivities;

import android.bluetooth.BluetoothGattService;
import android.util.Log;

/**
 * Created by michael on 4/19/17.
 */

public class PacketUtils {

    // Constants
    static private String LINE_FEED = "\n";
    static private String CARRIAGE_RETURN = "\r";

    static private char LINE_FEED_DUMMY_CHAR = '_';
    static private char CARRIAGE_RETURN_DUMMY_CHAR = '^';

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

    /**
     * Returns a string as an ASCII byte array
     * @param str
     * @return
     */
    public static byte[] stringToBytesASCII(String str) {

        // \n becomes _
        // \r becomes ^
        str = replaceEscapeChars(str);

        // Splits the string into a character array
        char[] charArray = str.toCharArray();

        // Initialize a byte array of length size(charArray)
        byte[] byteArray = new byte[charArray.length];

        // Populates the byte array with the ASCII byte form of each character in charArray
        for (int i = 0; i < byteArray.length; i++)
            byteArray[i] = charToByte(charArray[i]);

        return byteArray;
    }

    private static String replaceEscapeChars(String str){
        str = str.replace(LINE_FEED, String.valueOf(LINE_FEED_DUMMY_CHAR));
        str = str.replace(CARRIAGE_RETURN, String.valueOf(CARRIAGE_RETURN_DUMMY_CHAR));
        return str;
    }

    private static byte charToByte(char c){
        if(c == LINE_FEED_DUMMY_CHAR) // We have a line feed
            return 10;
        if(c == CARRIAGE_RETURN_DUMMY_CHAR) // We have a carriage return
            return 13;
        return (byte) c;
    }

    public static byte[] byteArrayToPacket(byte[] byteArray, PacketTypes packetType){

        byte[] packet = new byte[3 + byteArray.length + 1];
        packet[0] = (byte) '#'; // Insert packet header
        packet[1] = (byte) packetType.ordinal(); // Insert type
        packet[2] = (byte) (byteArray.length); // Insert size of packet

        for(int i = 0; i < byteArray.length; i++) // Port over the bytes into the packet
            packet[i + 3] = byteArray[i];

        //byte[] result = addTerminalLineFeed(packet, packetType);

        return packet;
    }

    // If this packet represent a command, then we need to add a line feed at the end
    public static byte[] addTerminalLineFeed(byte[] byteArray){

        byteArray[2] += 1;
        byte[] result = new byte[byteArray.length + 1];
        result[result.length-1] = (byte) 10;

        return result;
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

    public static void logByteArray(byte[] byteArray){
        Log.v("TAG","---- Byte Array -----");
        for(Byte b : byteArray) Log.v("TAG","byte : "+String.valueOf(b));
        Log.v("TAG","---- End ------------");
    }
}
