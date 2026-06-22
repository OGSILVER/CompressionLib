package com.compressionlib.lossless.lz77;

import com.compressionlib.core.CompressionAlgorithm;
import com.compressionlib.core.CompressionException;
import com.compressionlib.io.BitInputStream;
import com.compressionlib.io.BitOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class LZ77Codec implements CompressionAlgorithm {
    private static final byte ALGORITHM_ID = 0x02;

    @Override
    public byte[] compress(byte[] data) throws CompressionException {
        try {
            if (data == null || data.length == 0) {
                return new byte[0];
            }

            LZ77Encoder encoder = new LZ77Encoder();
            List<LZ77Token> tokens = encoder.encode(data);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (BitOutputStream bos = new BitOutputStream(baos)) {
                bos.writeBits(tokens.size(), 32);

                for (LZ77Token token : tokens) {
                    if (token.isLiteral()) {
                        bos.writeBit(false);
                        bos.writeByte(token.literal);
                    } else {
                        bos.writeBit(true);
                        bos.writeBits(token.offset, 15);
                        bos.writeBits(token.length - 3, 8);
                    }
                }
            }

            return baos.toByteArray();
        } catch (IOException e) {
            throw new CompressionException("LZ77 compression failed: " + e.getMessage(), e);
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
                int tokenCount = (int) bis.readBits(32);
                List<LZ77Token> tokens = new java.util.ArrayList<>();

                for (int i = 0; i < tokenCount; i++) {
                    boolean isMatch = bis.readBit();
                    if (isMatch) {
                        int offset = (int) bis.readBits(15);
                        int length = (int) bis.readBits(8) + 3;
                        tokens.add(LZ77Token.createMatch(offset, length));
                    } else {
                        byte literal = bis.readByte();
                        tokens.add(LZ77Token.createLiteral(literal));
                    }
                }

                LZ77Decoder decoder = new LZ77Decoder();
                return decoder.decode(tokens, data.length);
            }
        } catch (IOException e) {
            throw new CompressionException("LZ77 decompression failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getAlgorithmName() {
        return "LZ77";
    }

    @Override
    public byte getAlgorithmId() {
        return ALGORITHM_ID;
    }
}
