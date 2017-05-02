package com.adafruit.bluefruit.le.TalkingToEachOther.app.OurActivities.PacketWrappers;

import android.graphics.Color;

import java.nio.ByteBuffer;


public class Palette2 extends PaletteBaseClass {

    public byte[] packet;

    public Palette2(int color1, int color2){

        ByteBuffer buffer = ByteBuffer.allocate(3 + 3 * 2).order(java.nio.ByteOrder.LITTLE_ENDIAN);

        // Add delimiters
        buffer.put((byte) Constants.DELIMTER_ONE);
        buffer.put((byte) Constants.DELIMTER_TWO);

        // Add ID
        buffer.put((byte) PacketUtils.PacketTypes.PAL_2.ordinal());

        // Add RGBs
        buffer.put((byte) Color.red(color1));
        buffer.put((byte) Color.green(color1));
        buffer.put((byte) Color.blue(color1));

        buffer.put((byte) Color.red(color2));
        buffer.put((byte) Color.green(color2));
        buffer.put((byte) Color.blue(color2));

        packet = buffer.array();
    }
}
