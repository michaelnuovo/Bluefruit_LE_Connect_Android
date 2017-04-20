package com.adafruit.bluefruit.le.connect.app.OurActivities;

import android.util.Log;

/**
 * Created by michael on 4/19/17.
 */

public class PacketUtils {

    /**
     * Returns a string as an ASCII byte array
     * @param str
     * @return
     */
    public static byte[] stringToBytesASCII(String str) {

        // Splits the string into a character array
        char[] charArray = str.toCharArray();

        // Initialize a byte array of length size(charArray)
        byte[] byteArray = new byte[charArray.length];

        // Populates the byte array with the ASCII byte form of each character in charArray
        for (int i = 0; i < byteArray.length; i++)
            byteArray[i] = (byte) charArray[i];

        return byteArray;
    }

    /**
     * Packages meta info into a byte array
     * @param byteArray
     * @param metaInfo
     * @return
     */
    public static byte[] getPacket(byte[] byteArray, String metaInfo){

        // Splits the meta info into a character array
        char[] charArray = metaInfo.toCharArray();

        // Initializes a new byte array called packet of size(charArray) + size(byteArray)
        byte[] packet = new byte[byteArray.length + charArray.length + 1];

        // Populates the packet with the charArray
        for(int i = 0; i < charArray.length; i++)
            packet[i] = (byte) charArray[i];

        for(int i = 0; i < packet.length;i++)
            Log.v("TAG","packet["+String.valueOf(i)+"] is " + String.valueOf(packet[i]));

        // Adds the length of byteArray to the packet array
        packet[charArray.length] = (byte) byteArray.length;

        for(int i = 0; i < packet.length;i++)
            Log.v("TAG","packet["+String.valueOf(i)+"] is " + String.valueOf(packet[i]));

//        Log.v("TAG","packet.length is "+String.valueOf(packet.length));
//        Log.v("TAG","byteArray.length is "+String.valueOf(byteArray.length));
//        Log.v("TAG","charArray.length is "+String.valueOf(charArray.length));

        // Populates the remainder of the packet with the byteArray
        for(int i = charArray.length + 1; i < packet.length; i++)
            packet[i] = byteArray[i - charArray.length - 1];

        for(int i = 0; i < packet.length;i++)
            Log.v("TAG","packet["+String.valueOf(i)+"] is " + String.valueOf(packet[i]));

        // Returns the packet
        return packet;
    }

    /**
     * Returns a formatted string of packet data which can be displayed in a user interface
     * @return
     */
    public static String getPacketStats(byte[] byteArray){

        // Convert each byte in the byte array into a character and fit it into a character array of
        // equal length
        char[] charArray = new char[byteArray.length];
        for(int i = 0; i < byteArray.length ; i++)
            charArray[i] = (char) byteArray[i];

        // Convert the character array into a string
        String text = new String(charArray);

        // The third character in the text string is incorrect because it was mapped from the third
        // element in the byteArray which is an actual number and not an ASCII index.
        // The third character therefore needs to be replaced with the string value of byteArray.length.
        text = text.substring(0,2) + "[" + String.valueOf(byteArray.length-3) + "]" +
                text.substring(3,text.length());

        // Create the empty result string
        String result = "";

        // Create a visual divider
        result += text + "\n" + "------------------" + "\n";

        // Add the ASCII value and symbol representations
        for(int i = 0; i < byteArray.length; i++){
            if(i != 2){
                result += String.valueOf(charArray[i]) + "  " + String.valueOf(byteArray[i]) + "\n";
            } else {
                result += "\n"+"["+String.valueOf(byteArray.length-3)+"]" + "\n\n";
            }
        }

        return result;
    }
}
