package com.compressionlib.io;

import com.compressionlib.core.CompressionAlgorithm;
import com.compressionlib.core.CompressionException;
import com.compressionlib.lossless.huffman.HuffmanCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileFormat")
class FileFormatTest {

    @Test
    @DisplayName("Write and read header with algorithm ID and original size")
    void testHeaderRoundTrip(@TempDir Path tempDir) throws Exception {
        File testFile = tempDir.resolve("test.cmp").toFile();
        CompressionAlgorithm algo = new HuffmanCodec();
        byte[] original = "Hello, World!".getBytes();
        byte[] compressed = original; // Stub for now

        FileFormat.writeCompressedFile(testFile, algo, original, compressed);

        FileFormat.Header header = FileFormat.readCompressedFile(testFile);
        assertEquals(algo.getAlgorithmId(), header.algorithmId);
        assertEquals(original.length, header.originalSize);
        assertNotNull(header.metadata);
        assertEquals(0, header.metadata.length);
    }

    @Test
    @DisplayName("Header with metadata")
    void testHeaderWithMetadata(@TempDir Path tempDir) throws Exception {
        File testFile = tempDir.resolve("test_meta.cmp").toFile();
        CompressionAlgorithm algo = new HuffmanCodec();
        byte[] original = "Test data".getBytes();
        byte[] compressed = original;
        byte[] metadata = {0x01, 0x02, 0x03, 0x04};

        FileFormat.writeCompressedFile(testFile, algo, original, compressed, metadata);

        FileFormat.Header header = FileFormat.readCompressedFile(testFile);
        assertEquals(algo.getAlgorithmId(), header.algorithmId);
        assertEquals(original.length, header.originalSize);
        assertArrayEquals(metadata, header.metadata);
    }

    @Test
    @DisplayName("Invalid magic bytes throws exception")
    void testInvalidMagicBytes(@TempDir Path tempDir) throws Exception {
        File testFile = tempDir.resolve("invalid.cmp").toFile();

        // Write a file with wrong magic bytes manually
        try (BitOutputStream bos = new BitOutputStream(new java.io.FileOutputStream(testFile))) {
            bos.writeBits(0xDEADBEEF, 32); // Wrong magic
            bos.writeBits(1, 8); // Version
            bos.writeBits(1, 8); // Algorithm
            bos.writeBits(100, 32); // Size
            bos.writeBits(0, 16); // Metadata len
        }

        assertThrows(CompressionException.class, () -> FileFormat.readCompressedFile(testFile));
    }

    @Test
    @DisplayName("Large original size")
    void testLargeOriginalSize(@TempDir Path tempDir) throws Exception {
        File testFile = tempDir.resolve("large.cmp").toFile();
        CompressionAlgorithm algo = new HuffmanCodec();
        byte[] original = new byte[1024 * 1024]; // 1 MB
        java.util.Arrays.fill(original, (byte) 42);
        byte[] compressed = original;

        FileFormat.writeCompressedFile(testFile, algo, original, compressed);

        FileFormat.Header header = FileFormat.readCompressedFile(testFile);
        assertEquals(1024 * 1024, header.originalSize);
    }

    @Test
    @DisplayName("Empty data round-trip")
    void testEmptyData(@TempDir Path tempDir) throws Exception {
        File testFile = tempDir.resolve("empty.cmp").toFile();
        CompressionAlgorithm algo = new HuffmanCodec();
        byte[] original = new byte[0];
        byte[] compressed = new byte[0];

        FileFormat.writeCompressedFile(testFile, algo, original, compressed);

        FileFormat.Header header = FileFormat.readCompressedFile(testFile);
        assertEquals(0, header.originalSize);
    }
}
