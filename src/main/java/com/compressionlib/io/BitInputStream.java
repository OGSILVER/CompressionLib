package com.compressionlib.io;

import java.io.IOException;
import java.io.InputStream;

public class BitInputStream implements AutoCloseable {
    private InputStream in;
    private int currentByte;
    private int bitPosition;
    private boolean hasCurrentByte;

    public BitInputStream(InputStream in) {
        this.in = in;
        this.bitPosition = 8;
        this.hasCurrentByte = false;
    }

    public boolean readBit() throws IOException {
        if (bitPosition == 8) {
            currentByte = in.read();
            if (currentByte == -1) {
                throw new IOException("End of stream reached");
            }
            bitPosition = 0;
            hasCurrentByte = true;
        }

        boolean bit = ((currentByte >> (7 - bitPosition)) & 1) == 1;
        bitPosition++;
        return bit;
    }

    public long readBits(int count) throws IOException {
        if (count < 0 || count > 63) {
            throw new IllegalArgumentException("Count must be between 0 and 63");
        }

        if (count == 0) {
            return 0;
        }

        long result = 0;
        for (int i = 0; i < count; i++) {
            result = (result << 1) | (readBit() ? 1 : 0);
        }
        return result;
    }

    public byte readByte() throws IOException {
        return (byte) readBits(8);
    }

    public int readBytes(byte[] dest) throws IOException {
        return readBytes(dest, 0, dest.length);
    }

    public int readBytes(byte[] dest, int offset, int length) throws IOException {
        alignToByte();

        int bytesRead = 0;
        while (bytesRead < length) {
            int b = in.read();
            if (b == -1) {
                break;
            }
            dest[offset + bytesRead] = (byte) b;
            bytesRead++;
        }
        return bytesRead;
    }

    public void skip(int bits) throws IOException {
        for (int i = 0; i < bits; i++) {
            readBit();
        }
    }

    public void alignToByte() throws IOException {
        if (bitPosition != 8 && bitPosition != 0) {
            bitPosition = 8;
        }
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
