package com.compressionlib.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BitInputStream and BitOutputStream")
class BitInputOutputStreamTest {

    @Test
    @DisplayName("Single bit read/write round-trip")
    void testSingleBitRoundTrip() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BitOutputStream bos = new BitOutputStream(baos)) {
            bos.writeBit(true);
            bos.writeBit(false);
            bos.writeBit(true);
            bos.writeBit(true);
            bos.writeBit(false);
            bos.writeBit(false);
            bos.writeBit(true);
            bos.writeBit(false);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (BitInputStream bis = new BitInputStream(bais)) {
            assertTrue(bis.readBit());
            assertFalse(bis.readBit());
            assertTrue(bis.readBit());
            assertTrue(bis.readBit());
            assertFalse(bis.readBit());
            assertFalse(bis.readBit());
            assertTrue(bis.readBit());
            assertFalse(bis.readBit());
        }
    }

    @Test
    @DisplayName("Multi-bit read/write (8 bits)")
    void testMultiBitEightBits() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BitOutputStream bos = new BitOutputStream(baos)) {
            bos.writeBits(0b10110101, 8);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (BitInputStream bis = new BitInputStream(bais)) {
            long result = bis.readBits(8);
            assertEquals(0b10110101, result);
        }
    }

    @Test
    @DisplayName("Non-byte-aligned reads (3 bits)")
    void testThreeBits() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BitOutputStream bos = new BitOutputStream(baos)) {
            bos.writeBits(0b101, 3);
            bos.writeBits(0b011, 3);
            bos.writeBits(0b10, 2);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (BitInputStream bis = new BitInputStream(bais)) {
            assertEquals(0b101, bis.readBits(3));
            assertEquals(0b011, bis.readBits(3));
            assertEquals(0b10, bis.readBits(2));
        }
    }

    @Test
    @DisplayName("Non-byte-aligned reads (7 bits)")
    void testSevenBits() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BitOutputStream bos = new BitOutputStream(baos)) {
            bos.writeBits(0x7F, 7);
            bos.writeBits(0x55, 7);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (BitInputStream bis = new BitInputStream(bais)) {
            assertEquals(0x7F, bis.readBits(7));
            assertEquals(0x55, bis.readBits(7));
        }
    }

    @Test
    @DisplayName("Mixed bit and byte operations")
    void testMixedOperations() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BitOutputStream bos = new BitOutputStream(baos)) {
            bos.writeBits(0b1010, 4);
            bos.writeByte((byte) 0xFF);
            bos.writeBits(0b11, 2);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (BitInputStream bis = new BitInputStream(bais)) {
            assertEquals(0b1010, bis.readBits(4));
            assertEquals((byte) 0xFF, bis.readByte());
            assertEquals(0b11, bis.readBits(2));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 7, 13, 23, 31, 63})
    @DisplayName("Variable bit length reads/writes")
    void testVariableBitLengths(int bitCount) throws Exception {
        long testValue = (1L << bitCount) - 1; // All ones for bit count

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BitOutputStream bos = new BitOutputStream(baos)) {
            bos.writeBits(testValue, bitCount);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (BitInputStream bis = new BitInputStream(bais)) {
            long result = bis.readBits(bitCount);
            assertEquals(testValue, result);
        }
    }

    @Test
    @DisplayName("Multiple bytes round-trip")
    void testMultipleBytes() throws Exception {
        byte[] testData = {(byte) 0xFF, (byte) 0x00, (byte) 0xAA, (byte) 0x55, (byte) 0x12, (byte) 0x34};

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BitOutputStream bos = new BitOutputStream(baos)) {
            bos.writeBytes(testData);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (BitInputStream bis = new BitInputStream(bais)) {
            byte[] result = new byte[testData.length];
            int read = bis.readBytes(result);
            assertEquals(testData.length, read);
            assertArrayEquals(testData, result);
        }
    }

    @Test
    @DisplayName("Byte-aligned and non-aligned byte writes")
    void testByteAlignmentBehavior() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BitOutputStream bos = new BitOutputStream(baos)) {
            bos.writeBits(0b101, 3); // Not byte-aligned
            bos.writeByte((byte) 0xFF);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (BitInputStream bis = new BitInputStream(bais)) {
            assertEquals(0b101, bis.readBits(3));
            assertEquals((byte) 0xFF, bis.readByte());
        }
    }

    @Test
    @DisplayName("Flush behavior with partial byte")
    void testFlushWithPartialByte() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BitOutputStream bos = new BitOutputStream(baos)) {
            bos.writeBit(true);
            bos.writeBit(false);
            bos.writeBit(true);
            bos.flush();
        }

        byte[] data = baos.toByteArray();
        assertEquals(1, data.length);
        assertEquals((byte) 0b10100000, data[0]);
    }

    @Test
    @DisplayName("Empty read after partial writes")
    void testReadPartialBits() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BitOutputStream bos = new BitOutputStream(baos)) {
            bos.writeBits(0b1010, 4);
            bos.writeBits(0b11, 2);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (BitInputStream bis = new BitInputStream(bais)) {
            assertEquals(0b1010, bis.readBits(4));
            assertEquals(0b11, bis.readBits(2));
        }
    }

    @Test
    @DisplayName("Zero-length writes")
    void testZeroLengthReadsWrites() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BitOutputStream bos = new BitOutputStream(baos)) {
            bos.writeBits(0, 0);
            bos.writeByte((byte) 42);
            bos.writeBits(0, 0);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (BitInputStream bis = new BitInputStream(bais)) {
            assertEquals(0, bis.readBits(0));
            assertEquals((byte) 42, bis.readByte());
            assertEquals(0, bis.readBits(0));
        }
    }

    @Test
    @DisplayName("Large value spanning multiple bytes")
    void testLargeMultiBytValue() throws Exception {
        long largeValue = 0x123456789ABCDEFL; // Mask to 56 bits
        int bitCount = 56;
        largeValue = largeValue & ((1L << bitCount) - 1); // Ensure value fits in bitCount

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BitOutputStream bos = new BitOutputStream(baos)) {
            bos.writeBits(largeValue, bitCount);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (BitInputStream bis = new BitInputStream(bais)) {
            long result = bis.readBits(bitCount);
            assertEquals(largeValue, result);
        }
    }
}
