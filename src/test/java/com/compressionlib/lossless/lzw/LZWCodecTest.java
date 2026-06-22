package com.compressionlib.lossless.lzw;

import com.compressionlib.core.CompressionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LZW Codec")
class LZWCodecTest {

    private LZWCodec codec = new LZWCodec();

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
    @DisplayName("Repeated pattern round-trip")
    void testRepeatedPattern() throws CompressionException {
        String pattern = "ababababababababababababababababababab";
        byte[] data = pattern.getBytes();
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
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
    @DisplayName("Highly compressible data")
    void testHighlyCompressible() throws CompressionException {
        byte[] data = new byte[1000];
        for (int i = 0; i < 1000; i++) {
            data[i] = (byte) (i % 5);
        }
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
        assertTrue(compressed.length < data.length, "Repetitive data should compress");
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
    @DisplayName("Repeated sequences")
    void testRepeatedSequences() throws CompressionException {
        String text = "aaabbbcccaaabbbcccaaabbbccc";
        byte[] data = text.getBytes();
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
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
        String subpattern = "compressthisdata";
        for (int i = 0; i < 50; i++) {
            sb.append(subpattern);
        }
        byte[] data = sb.toString().getBytes();
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
        assertTrue(compressed.length < data.length, "Repetitive pattern should compress");
    }

    @Test
    @DisplayName("Mixed patterns and literals")
    void testMixedPatterns() throws CompressionException {
        byte[] data = "xxxyyyzzzxxxyyyzzzxxxyyyzzzaaaabbbbcccc".getBytes();
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }

    @Test
    @DisplayName("Dictionary growth test (large data)")
    void testDictionaryGrowth() throws CompressionException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("pattern").append(i).append("data");
        }
        byte[] data = sb.toString().getBytes();
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }

    @Test
    @DisplayName("Algorithm metadata")
    void testAlgorithmMetadata() {
        assertEquals("LZW", codec.getAlgorithmName());
        assertEquals(0x03, codec.getAlgorithmId());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 100, 256, 1000, 5000, 10000})
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
    @DisplayName("Repeating single byte")
    void testRepeatingByte() throws CompressionException {
        byte[] data = new byte[500];
        Arrays.fill(data, (byte) 'A');
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }

    @Test
    @DisplayName("Whitespace and special chars")
    void testSpecialChars() throws CompressionException {
        byte[] data = "\n\t\r   \0\0\0!!!@@@###".getBytes();
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }

    @Test
    @DisplayName("Ascending byte sequence")
    void testAscendingSequence() throws CompressionException {
        byte[] data = new byte[500];
        for (int i = 0; i < 500; i++) {
            data[i] = (byte) (i % 256);
        }
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }

    @Test
    @DisplayName("Compression ratio on text")
    void testCompressionRatio() throws CompressionException {
        String text = "the quick brown fox jumps over the lazy dog. " +
                     "the quick brown fox jumps over the lazy dog. " +
                     "the quick brown fox jumps over the lazy dog. ";
        byte[] data = text.getBytes();
        byte[] compressed = codec.compress(data);
        byte[] decompressed = codec.decompress(compressed);
        assertArrayEquals(data, decompressed);
        assertTrue(compressed.length < data.length, "Text should compress");
    }
}
