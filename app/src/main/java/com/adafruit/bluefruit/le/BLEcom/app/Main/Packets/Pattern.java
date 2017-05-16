package com.adafruit.bluefruit.le.BLEcom.app.Main.Packets;

import android.graphics.Color;

import java.nio.ByteBuffer;

/**
 * Created by michael on 5/15/17.
 */

public class Pattern {


    public byte[] packet;

    public Pattern(int patId, int patSec, int gamma){

        int headerSize = 3; // Two delimeters + the packet ID
        int payLoadSize = 3; // one byte for the pattern ID + Two bytes for seconds and gamma
        int packetSize = headerSize + payLoadSize;
        ByteBuffer buffer = ByteBuffer.allocate(packetSize).order(java.nio.ByteOrder.LITTLE_ENDIAN);

        // Add delimiters
        buffer.put((byte) Constants.DELIMTER_ONE);
        buffer.put((byte) Constants.DELIMTER_TWO);

        // Add packet ID
        buffer.put((byte) Constants.PacketTypes.PACK_PAT.ordinal());

        // Add pattern ID
        buffer.put((byte) patId);

        // Add seconds and gamma p
        buffer.put((byte) patSec);
        buffer.put((byte) gamma);

        packet = buffer.array();
    }
}
