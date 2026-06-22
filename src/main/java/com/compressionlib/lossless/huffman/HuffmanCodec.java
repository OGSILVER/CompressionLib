package com.compressionlib.lossless.huffman;

import com.compressionlib.core.CompressionAlgorithm;
import com.compressionlib.core.CompressionException;
import com.compressionlib.io.BitInputStream;
import com.compressionlib.io.BitOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HuffmanCodec implements CompressionAlgorithm {
    private static final byte ALGORITHM_ID = 0x01;
    private HuffmanCodeTable codeTable;

    @Override
    public byte[] compress(byte[] data) throws CompressionException {
        try {
            if (data == null || data.length == 0) {
                return new byte[0];
            }

            codeTable = HuffmanCodeTable.buildFromData(data);

            if (codeTable.isEmpty()) {
                return new byte[0];
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (BitOutputStream bos = new BitOutputStream(baos)) {
                for (byte b : data) {
                    String code = codeTable.encode(b);
                    for (char c : code.toCharArray()) {
                        bos.writeBit(c == '1');
                    }
                }
            }

            return baos.toByteArray();
        } catch (IOException e) {
            throw new CompressionException("Huffman compression failed: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] decompress(byte[] data) throws CompressionException {
        try {
            if (data == null || data.length == 0) {
                return new byte[0];
            }

            if (codeTable == null || codeTable.isEmpty()) {
                throw new CompressionException("Code table not initialized");
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            try (BitInputStream bis = new BitInputStream(bais)) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                StringBuilder currentCode = new StringBuilder();
                int decodedCount = 0;
                int targetCount = codeTable.getOriginalByteCount();

                while (decodedCount < targetCount) {
                    try {
                        boolean bit = bis.readBit();
                        currentCode.append(bit ? '1' : '0');

                        if (codeTable.canDecode(currentCode.toString())) {
                            byte decodedByte = codeTable.decode(currentCode.toString());
                            baos.write(decodedByte);
                            decodedCount++;
                            currentCode = new StringBuilder();
                        }

                        if (currentCode.length() > codeTable.getMaxCodeLength()) {
                            throw new CompressionException("Invalid code sequence during decompression");
                        }
                    } catch (IOException e) {
                        if (decodedCount < targetCount) {
                            throw new CompressionException("Premature end of stream during decompression");
                        }
                        break;
                    }
                }

                return baos.toByteArray();
            }
        } catch (CompressionException e) {
            throw e;
        } catch (Exception e) {
            throw new CompressionException("Huffman decompression failed: " + e.getMessage(), e);
        }
    }

    public void setCodeTable(HuffmanCodeTable table) {
        this.codeTable = table;
    }

    public byte[] getCodeTableMetadata() throws CompressionException {
        try {
            if (codeTable == null) {
                return new byte[0];
            }
            return codeTable.serialize();
        } catch (IOException e) {
            throw new CompressionException("Failed to serialize code table: " + e.getMessage(), e);
        }
    }

    public void initializeFromMetadata(byte[] metadata) throws CompressionException {
        try {
            codeTable = HuffmanCodeTable.deserialize(metadata);
        } catch (IOException e) {
            throw new CompressionException("Failed to deserialize code table: " + e.getMessage(), e);
        }
    }

    @Override
    public String getAlgorithmName() {
        return "Huffman";
    }

    @Override
    public byte getAlgorithmId() {
        return ALGORITHM_ID;
    }
}
