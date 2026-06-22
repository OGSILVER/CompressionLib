package com.compressionlib.io;

import com.compressionlib.core.CompressionAlgorithm;
import com.compressionlib.core.CompressionException;

import java.io.*;

public class FileFormat {
    private static final int MAGIC = 0x434F4D50; // "COMP" in ASCII
    private static final byte VERSION = 0x01;
    private static final int HEADER_BASE_SIZE = 12; // Magic(4) + Version(1) + AlgoId(1) + OrigSize(4) + MetadataLen(2)

    public static class Header {
        public byte algorithmId;
        public int originalSize;
        public byte[] metadata;

        public Header(byte algorithmId, int originalSize, byte[] metadata) {
            this.algorithmId = algorithmId;
            this.originalSize = originalSize;
            this.metadata = metadata != null ? metadata : new byte[0];
        }
    }

    public static void writeCompressedFile(File file, CompressionAlgorithm algorithm, byte[] original, byte[] compressed) throws IOException, CompressionException {
        writeCompressedFile(file, algorithm, original, compressed, null);
    }

    public static void writeCompressedFile(File file, CompressionAlgorithm algorithm, byte[] original, byte[] compressed, byte[] metadata) throws IOException, CompressionException {
        try (FileOutputStream fos = new FileOutputStream(file);
             BitOutputStream bos = new BitOutputStream(fos)) {
            writeHeader(bos, algorithm, original, metadata);
            bos.writeBytes(compressed);
            bos.flush();
        }
    }

    private static void writeHeader(BitOutputStream bos, CompressionAlgorithm algorithm, byte[] original, byte[] metadata) throws IOException {
        // Write magic bytes
        bos.writeBits(MAGIC, 32);

        // Write version
        bos.writeBits(VERSION, 8);

        // Write algorithm ID
        bos.writeBits(algorithm.getAlgorithmId(), 8);

        // Write original size (4 bytes)
        bos.writeBits(original.length, 32);

        // Write metadata length (2 bytes)
        short metadataLen = (short) (metadata != null ? metadata.length : 0);
        bos.writeBits(metadataLen & 0xFFFF, 16);

        // Write metadata if present
        if (metadata != null && metadata.length > 0) {
            bos.writeBytes(metadata);
        }
    }

    public static Header readCompressedFile(File file) throws IOException, CompressionException {
        try (FileInputStream fis = new FileInputStream(file);
             BitInputStream bis = new BitInputStream(fis)) {
            return readHeader(bis);
        }
    }

    private static Header readHeader(BitInputStream bis) throws IOException, CompressionException {
        // Read and validate magic bytes
        long magic = bis.readBits(32);
        if (magic != MAGIC) {
            throw new CompressionException("Invalid file format: incorrect magic bytes");
        }

        // Read and validate version
        byte version = (byte) bis.readBits(8);
        if (version != VERSION) {
            throw new CompressionException("Unsupported file version: " + version);
        }

        // Read algorithm ID
        byte algorithmId = (byte) bis.readBits(8);

        // Read original size
        int originalSize = (int) bis.readBits(32);
        if (originalSize < 0) {
            throw new CompressionException("Invalid original size in header");
        }

        // Read metadata length
        int metadataLen = (int) bis.readBits(16);
        if (metadataLen < 0 || metadataLen > 65535) {
            throw new CompressionException("Invalid metadata length");
        }

        // Read metadata
        byte[] metadata = null;
        if (metadataLen > 0) {
            metadata = new byte[metadataLen];
            int bytesRead = bis.readBytes(metadata);
            if (bytesRead != metadataLen) {
                throw new CompressionException("Failed to read complete metadata");
            }
        }

        return new Header(algorithmId, originalSize, metadata);
    }

    public static byte[] readCompressedData(File file) throws IOException, CompressionException {
        try (FileInputStream fis = new FileInputStream(file);
             BitInputStream bis = new BitInputStream(fis)) {
            readHeader(bis);
            bis.alignToByte();

            // Read remaining compressed data
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = bis.readBytes(buffer)) > 0) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }
}
