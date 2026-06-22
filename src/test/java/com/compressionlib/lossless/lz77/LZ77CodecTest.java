package com.compressionlib.lossless.lz77;

import com.compressionlib.core.CompressionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LZ77 Codec")
class LZ77CodecTest {

    private LZ77Codec codec = new LZ77Codec();

    @Test
    @DisplayName("Empty data round-trip")
    void testEmptyData() throws CompressionException {
        byte[] data = new byte[0];
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }

    @Test
    @DisplayName("Single byte round-trip")
    void testSingleByte() throws CompressionException {
        byte[] data = {(byte) 42};
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }

    @Test
    @DisplayName("Repeated sequence round-trip")
    void testRepeatedSequence() throws CompressionException {
        String pattern = "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc";
        byte[] data = pattern.getBytes();
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
        assertTrue(compressed.length < data.length, "Repeated pattern should compress");
    }

    @Test
    @DisplayName("Simple text round-trip")
    void testSimpleText() throws CompressionException {
        byte[] data = "the quick brown fox jumps over the lazy dog".getBytes();
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }

    @Test
    @DisplayName("All byte values round-trip")
    void testAllByteValues() throws CompressionException {
        byte[] data = new byte[256];
        for (int i = 0; i < 256; i++) {
            data[i] = (byte) i;
        }
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }

    @Test
    @DisplayName("Repeated single byte")
    void testRepeatedSingleByte() throws CompressionException {
        byte[] data = new byte[500];
        Arrays.fill(data, (byte) 42);
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
        assertTrue(compressed.length < data.length, "Repeated bytes should compress significantly");
    }

    @Test
    @DisplayName("Random data round-trip")
    void testRandomData() throws CompressionException {
        Random random = new Random(42);
        byte[] data = new byte[1000];
        random.nextBytes(data);

        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }

    @Test
    @DisplayName("Highly compressible data")
    void testHighlyCompressible() throws CompressionException {
        byte[] data = new byte[1000];
        for (int i = 0; i < 1000; i++) {
            data[i] = (byte) (i % 5);
        }
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
        assertTrue(compressed.length < data.length);
    }

    @Test
    @DisplayName("Binary data round-trip")
    void testBinaryData() throws CompressionException {
        byte[] data = {0x00, (byte) 0xFF, 0x00, (byte) 0xFF, 0x00, (byte) 0xFF};
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }

    @Test
    @DisplayName("Long repeated substring")
    void testLongRepeatedSubstring() throws CompressionException {
        StringBuilder sb = new StringBuilder();
        String subpattern = "compressthis";
        for (int i = 0; i < 100; i++) {
            sb.append(subpattern);
        }
        byte[] data = sb.toString().getBytes();
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }

    @Test
    @DisplayName("Mixed literal and match sequences")
    void testMixedLiteralsAndMatches() throws CompressionException {
        byte[] data = "aaabbbcccdddabcabcabc".getBytes();
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }

    @Test
    @DisplayName("Algorithm metadata")
    void testAlgorithmMetadata() {
        assertEquals("LZ77", codec.getAlgorithmName());
        assertEquals(0x02, codec.getAlgorithmId());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 100, 256, 1000, 5000})
    @DisplayName("Various data sizes")
    void testVariousSizes(int size) throws CompressionException {
        byte[] data = new byte[size];
        Random random = new Random(size);
        random.nextBytes(data);

        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed, "Failed for size: " + size);
    }

    @Test
    @DisplayName("Null input")
    void testNullInput() throws CompressionException {
        byte[] result = codec.compress(null);
        assertEquals(0, result.length);
    }

    @Test
    @DisplayName("Streaming patterns")
    void testStreamingPatterns() throws CompressionException {
        byte[] data = "abcd abcd abcd abcd abcd".getBytes();
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }

    @Test
    @DisplayName("Patterns at boundary")
    void testBoundaryPatterns() throws CompressionException {
        byte[] data = "xyzxyzxyzxyzxyzxyzxyz".getBytes();
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }
}
