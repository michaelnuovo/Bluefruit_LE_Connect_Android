package com.adafruit.bluefruit.le.BLEcom.app.OurActivities.PacketWrappers;

import android.graphics.Color;

import java.nio.ByteBuffer;

public class Palette8 extends PaletteBaseClass {

    public byte[] packet;

    public Palette8(int color1, int color2, int color3, int color4,
                    int color5, int color6, int color7, int color8){

        ByteBuffer buffer = ByteBuffer.allocate(3 + 3 * 8).order(java.nio.ByteOrder.LITTLE_ENDIAN);

        // Add delimiters
        buffer.put((byte) PacketUtils.DELIMTER_ONE);
        buffer.put((byte) PacketUtils.DELIMTER_TWO);

        // Add ID
        buffer.put((byte) PacketUtils.PacketTypes.PAL_8.ordinal());

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

        buffer.put((byte) Color.red(color5));
        buffer.put((byte) Color.green(color5));
        buffer.put((byte) Color.blue(color5));

        buffer.put((byte) Color.red(color6));
        buffer.put((byte) Color.green(color6));
        buffer.put((byte) Color.blue(color6));

        buffer.put((byte) Color.red(color7));
        buffer.put((byte) Color.green(color7));
        buffer.put((byte) Color.blue(color7));

        buffer.put((byte) Color.red(color8));
        buffer.put((byte) Color.green(color8));
        buffer.put((byte) Color.blue(color8));

        packet = buffer.array();
    }
}
