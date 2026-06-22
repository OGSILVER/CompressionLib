package com.compressionlib.lossless.huffman;

import com.compressionlib.core.CompressionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Huffman Codec")
class HuffmanCodecTest {

    private HuffmanCodec codec = new HuffmanCodec();

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
    @DisplayName("Repeated bytes round-trip")
    void testRepeatedBytes() throws CompressionException {
        byte[] data = new byte[100];
        Arrays.fill(data, (byte) 65);
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }

    @Test
    @DisplayName("Simple text round-trip")
    void testSimpleText() throws CompressionException {
        byte[] data = "hello world hello world".getBytes();
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
    @DisplayName("Highly compressible data (repeated pattern)")
    void testHighlyCompressibleData() throws CompressionException {
        byte[] data = new byte[500];
        for (int i = 0; i < 500; i++) {
            data[i] = (byte) (i % 3);
        }
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
        assertTrue(compressed.length < data.length, "Highly repetitive data should compress");
    }

    @Test
    @DisplayName("Compression ratio reasonable for text")
    void testCompressionRatio() throws CompressionException {
        String text = "the quick brown fox jumps over the lazy dog. " +
                     "the quick brown fox jumps over the lazy dog. " +
                     "the quick brown fox jumps over the lazy dog. ";
        byte[] data = text.getBytes();
        byte[] compressed = codec.compress(data);

        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
        assertTrue(compressed.length < data.length, "Text should compress reasonably");
    }

    @Test
    @DisplayName("Two distinct bytes")
    void testTwoDistinctBytes() throws CompressionException {
        byte[] data = new byte[200];
        for (int i = 0; i < 200; i++) {
            data[i] = (byte) (i % 2 == 0 ? 0xFF : 0x00);
        }
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }

    @Test
    @DisplayName("Large data (10KB)")
    void testLargeData() throws CompressionException {
        byte[] data = new byte[10000];
        Random random = new Random(123);
        random.nextBytes(data);

        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }

    @Test
    @DisplayName("Null input")
    void testNullInput() throws CompressionException {
        byte[] result = codec.compress(null);
        assertEquals(0, result.length);
    }

    @Test
    @DisplayName("Algorithm metadata")
    void testAlgorithmMetadata() {
        assertEquals("Huffman", codec.getAlgorithmName());
        assertEquals(0x01, codec.getAlgorithmId());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 100, 256, 1000})
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
    @DisplayName("Whitespace and special characters")
    void testSpecialCharacters() throws CompressionException {
        byte[] data = "\n\t\r   \0\0\0!!!".getBytes();
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }
}
