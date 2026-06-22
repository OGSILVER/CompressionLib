package com.compressionlib.lossless.lz77;

import java.util.*;

class LZ77Decoder {
    byte[] decode(List<LZ77Token> tokens, int originalSize) {
        ByteArrayBuilder builder = new ByteArrayBuilder(originalSize);

        for (LZ77Token token : tokens) {
            if (token.isLiteral()) {
                builder.append(token.literal);
            } else {
                byte[] match = builder.getLastBytes(token.offset, token.length);
                for (byte b : match) {
                    builder.append(b);
                }
            }
        }

        return builder.toByteArray();
    }

    private static class ByteArrayBuilder {
        private List<Byte> bytes;

        ByteArrayBuilder(int capacity) {
            this.bytes = new ArrayList<>(capacity);
        }

        void append(byte b) {
            bytes.add(b);
        }

        byte[] getLastBytes(int offset, int length) {
            int startPos = bytes.size() - offset;
            byte[] result = new byte[length];

            for (int i = 0; i < length; i++) {
                int srcPos = startPos + (i % offset);
                result[i] = bytes.get(srcPos);
            }

            return result;
        }

        byte[] toByteArray() {
            byte[] result = new byte[bytes.size()];
            for (int i = 0; i < bytes.size(); i++) {
                result[i] = bytes.get(i);
            }
            return result;
        }
    }
}
