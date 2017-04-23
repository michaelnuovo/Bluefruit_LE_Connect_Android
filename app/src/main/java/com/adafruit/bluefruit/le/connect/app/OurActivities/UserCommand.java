package com.adafruit.bluefruit.le.connect.app.OurActivities;

import java.util.ArrayList;

/**
 * Created by michael on 4/22/17.
 */

public class UserCommand {

    static ArrayList<UserCommand> history = new ArrayList<>();

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

    public void commitToMemory(){
        history.add(this);
    }

    public String toString(){

        String one = new String(line1);
        String two = new String(line2);
        String three = new String(line3);
        String four = new String(line4);

        if(!line1.equals("")) one += "\n"; // if the line is not empty, add a line feed character
        if(!line2.equals("")) two += "\n"; // if the line is not empty, add a line feed character
        if(!line3.equals("")) three += "\n"; // if the line is not empty, add a line feed character
        if(!line4.equals("")) four += "\n"; // if the line is not empty, add a line feed character

        return one + two + three + four;
    }

    public String toStringShowLineFeeds(){
        String result = toString();
        return result.replace("\n","\\n");
    }

    public byte[] toPacket(){
        String command = toString().replace("\n","^").replace("\r","_");
        char[] commandChars = command.toCharArray();
        byte[] result = new byte[1 + 1 + 1 + commandChars.length];
        result[0] = Constants.DELIMITER;
        result[1] = (byte) Constants.PacketTypes.USER_COMMAND.ordinal();
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
}
