package com.compressionlib.lossless.lzw;

import com.compressionlib.core.CompressionAlgorithm;
import com.compressionlib.core.CompressionException;
import com.compressionlib.io.BitInputStream;
import com.compressionlib.io.BitOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class LZWCodec implements CompressionAlgorithm {
    private static final byte ALGORITHM_ID = 0x03;

    @Override
    public byte[] compress(byte[] data) throws CompressionException {
        try {
            if (data == null || data.length == 0) {
                return new byte[0];
            }

            LZWEncoder encoder = new LZWEncoder();
            List<Integer> codes = encoder.encode(data);
            List<Integer> codeSizes = encoder.getCodeSizes();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (BitOutputStream bos = new BitOutputStream(baos)) {
                bos.writeBits(codes.size(), 32);

                for (int i = 0; i < codes.size(); i++) {
                    int codeSize = codeSizes.get(i);
                    bos.writeBits(codes.get(i), codeSize);
                }
            }

            return baos.toByteArray();
        } catch (IOException e) {
            throw new CompressionException("LZW compression failed: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] decompress(byte[] data) throws CompressionException {
        try {
            if (data == null || data.length == 0) {
                return new byte[0];
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            try (BitInputStream bis = new BitInputStream(bais)) {
                int codeCount = (int) bis.readBits(32);
                List<Integer> codes = new java.util.ArrayList<>();
                List<Integer> codeSizes = new java.util.ArrayList<>();

                int nextCode = 258;
                int currentCodeSize = 9;

                for (int i = 0; i < codeCount; i++) {
                    int code = (int) bis.readBits(currentCodeSize);
                    codes.add(code);
                    codeSizes.add(currentCodeSize);

                    if (code != 256 && code != 257) {
                        if (nextCode < (1 << 16)) {
                            nextCode++;
                        }

                        for (int bitWidth = 9; bitWidth <= 16; bitWidth++) {
                            if (nextCode >= (1 << (bitWidth - 1))) {
                                currentCodeSize = bitWidth;
                            }
                        }
                    } else if (code == 256) {
                        nextCode = 258;
                        currentCodeSize = 9;
                    }
                }

                LZWDecoder decoder = new LZWDecoder();
                return decoder.decode(codes);
            }
        } catch (IOException e) {
            throw new CompressionException("LZW decompression failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getAlgorithmName() {
        return "LZW";
    }

    @Override
    public byte getAlgorithmId() {
        return ALGORITHM_ID;
    }
}

