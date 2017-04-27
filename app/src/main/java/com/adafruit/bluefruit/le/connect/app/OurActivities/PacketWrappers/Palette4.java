package com.adafruit.bluefruit.le.connect.app.OurActivities.PacketWrappers;

import android.graphics.Color;

import java.nio.ByteBuffer;

public class Palette4 extends PaletteBaseClass {

    public byte[] packet;

    public Palette4(int color1, int color2, int color3, int color4){

        ByteBuffer buffer = ByteBuffer.allocate(3 + 3 * 4).order(java.nio.ByteOrder.LITTLE_ENDIAN);

        // Add delimiters
        buffer.put((byte) PacketUtils.DELIMTER_ONE);
        buffer.put((byte) PacketUtils.DELIMTER_TWO);

        // Add ID
        buffer.put((byte) PacketUtils.PacketTypes.PAL_4.ordinal());

        // Add RGBs
        buffer.put((byte) Color.red(color1));
        buffer.put((byte) Color.green(color1));
        buffer.put((byte) Color.blue(color1));

        buffer.put((byte) Color.red(color2));
        buffer.put((byte) Color.green(color2));
        buffer.put((byte) Color.blue(color2));

        buffer.put((byte) Color.red(color3));
        buffer.put((byte) Color.green(color3));
        buffer.put((byte) Color.blue(color3));

        buffer.put((byte) Color.red(color4));
        buffer.put((byte) Color.green(color4));
        buffer.put((byte) Color.blue(color4));


        packet = buffer.array();
    }
}
