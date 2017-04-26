package com.adafruit.bluefruit.le.connect.app.OurActivities.PacketWrappers;

public class UserCommand {

    public String line1;
    public String line2;
    public String line3;
    public String line4;

    public UserCommand(String line1, String line2, String line3, String line4){
        this.line1 = line1;
        this.line2 = line2;
        this.line3 = line3;
        this.line4 = line4;
    }

    public String toString(){

        // Convert null values to empty string
        if(line1 == null) line1 = "";
        if(line2 == null) line2 = "";
        if(line3 == null) line3 = "";
        if(line4 == null) line4 = "";

        // Adds line feed characters at end of non empty lines
        if(!line1.equals("")) line1 += "\n";
        if(!line2.equals("")) line2 += "\n";
        if(!line3.equals("")) line3 += "\n";
        if(!line4.equals("")) line4 += "\n";

        // Return the lines in concatenation
        return line1 + line2 + line3 + line4;
    }

    public String toStringShowLineFeeds(){
        String result = toString();
        return result.replace("\n","\\n");
    }

    // TODO refactor this method using ByteBuffer
    public byte[] toPacket(){
        String command = toString().replace("\n","^").replace("\r","_");
        char[] commandChars = command.toCharArray();
        byte[] result = new byte[2 + 1 + 1 + commandChars.length];
        result[0] = (byte) Constants.DELIMTER_ONE;
        result[1] = (byte) Constants.DELIMTER_TWO;
        result[2] = (byte) Constants.PacketTypes.USER_COMMAND.ordinal();
        result[3] = (byte) commandChars.length;
        for(int i = 0; i < commandChars.length; i++)
            if(commandChars[i] == '^')
                result[i+4] = 10;
            else if(commandChars[i] == '_')
                result[i+4] = 13;
            else
                result[i+4] = (byte) commandChars[i];
        return result;
    }

    public static UserCommand testCommand1(){
        //private static String myString = "test,2\n1234,5678,9012\n345.678,abcdefg,9876\n";
        return new UserCommand("test,2","1234,5678,9012","345.678,abcdefg,9876",null);
    }
}
