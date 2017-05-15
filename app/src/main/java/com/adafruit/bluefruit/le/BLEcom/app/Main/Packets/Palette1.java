package com.adafruit.bluefruit.le.BLEcom.app.Main.Packets;

import android.graphics.Color;

import java.nio.ByteBuffer;


public class Palette1 extends PaletteBaseClass {

    public byte[] packet;

    public Palette1(int color){

        ByteBuffer buffer = ByteBuffer.allocate(3 + 3 * 1).order(java.nio.ByteOrder.LITTLE_ENDIAN);

        // Add delimiters
        buffer.put((byte) Constants.DELIMTER_ONE);
        buffer.put((byte) Constants.DELIMTER_TWO);

        // Add ID
        buffer.put((byte) Constants.PacketTypes.PAL_1.ordinal());

        // Add RGBs
        buffer.put((byte) Color.red(color));
        buffer.put((byte) Color.green(color));
        buffer.put((byte) Color.blue(color));

        packet = buffer.array();
    }
}
