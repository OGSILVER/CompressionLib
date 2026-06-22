package com.compressionlib.io;

import java.io.IOException;
import java.io.OutputStream;

public class BitOutputStream implements AutoCloseable {
    private OutputStream out;
    private int currentByte;
    private int bitPosition;

    public BitOutputStream(OutputStream out) {
        this.out = out;
        this.currentByte = 0;
        this.bitPosition = 0;
    }

    public void writeBit(boolean bit) throws IOException {
        if (bit) {
            currentByte |= (1 << (7 - bitPosition));
        }
        bitPosition++;

        if (bitPosition == 8) {
            out.write(currentByte);
            currentByte = 0;
            bitPosition = 0;
        }
    }

    public void writeBits(long value, int count) throws IOException {
        if (count < 0 || count > 63) {
            throw new IllegalArgumentException("Count must be between 0 and 63");
        }

        for (int i = count - 1; i >= 0; i--) {
            writeBit(((value >> i) & 1) == 1);
        }
    }

    public void writeByte(byte b) throws IOException {
        writeBits(b & 0xFF, 8);
    }

    public void writeBytes(byte[] src) throws IOException {
        writeBytes(src, 0, src.length);
    }

    public void writeBytes(byte[] src, int offset, int length) throws IOException {
        alignToByte();

        for (int i = 0; i < length; i++) {
            out.write(src[offset + i]);
        }
    }

    public void alignToByte() throws IOException {
        if (bitPosition > 0) {
            out.write(currentByte);
            currentByte = 0;
            bitPosition = 0;
        }
    }

    public void flush() throws IOException {
        if (bitPosition > 0) {
            out.write(currentByte);
            currentByte = 0;
            bitPosition = 0;
        }
        out.flush();
    }

    @Override
    public void close() throws IOException {
        flush();
        out.close();
    }
}
