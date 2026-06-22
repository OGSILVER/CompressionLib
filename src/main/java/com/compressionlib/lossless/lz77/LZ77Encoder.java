package com.compressionlib.lossless.lz77;

import java.util.*;

class LZ77Encoder {
    private static final int WINDOW_SIZE = 32768;
    private static final int MIN_MATCH_LENGTH = 3;
    private static final int MAX_MATCH_LENGTH = 258;
    private static final int HASH_SIZE = 3;

    private Map<Integer, List<Integer>> hashTable;

    LZ77Encoder() {
        this.hashTable = new HashMap<>();
    }

    List<LZ77Token> encode(byte[] data) {
        List<LZ77Token> tokens = new ArrayList<>();

        if (data == null || data.length == 0) {
            return tokens;
        }

        int pos = 0;
        while (pos < data.length) {
            int matchLength = 0;
            int matchOffset = 0;

            if (pos + MIN_MATCH_LENGTH <= data.length) {
                int hash = hash3Bytes(data, pos);
                int[] match = findBestMatch(data, pos, hash);
                matchLength = match[0];
                matchOffset = match[1];
            }

            if (matchLength >= MIN_MATCH_LENGTH) {
                tokens.add(LZ77Token.createMatch(matchOffset, matchLength));
                addHashEntries(data, pos, matchLength);
                pos += matchLength;
            } else {
                tokens.add(LZ77Token.createLiteral(data[pos]));
                addHashEntry(data, pos);
                pos++;
            }
        }

        return tokens;
    }

    private int hash3Bytes(byte[] data, int pos) {
        if (pos + 3 > data.length) {
            return 0;
        }
        int h = 0;
        for (int i = 0; i < Math.min(3, data.length - pos); i++) {
            h = ((h << 5) + h) ^ (data[pos + i] & 0xFF);
        }
        return h & 0xFFFF;
    }

    private int[] findBestMatch(byte[] data, int pos, int hash) {
        int bestLength = 0;
        int bestOffset = 0;

        List<Integer> candidates = hashTable.getOrDefault(hash, new ArrayList<>());

        for (int candidatePos : candidates) {
            int offset = pos - candidatePos;
            if (offset <= 0 || offset > WINDOW_SIZE) {
                continue;
            }

            int length = matchLength(data, candidatePos, pos);
            if (length >= MIN_MATCH_LENGTH && length > bestLength) {
                bestLength = Math.min(length, MAX_MATCH_LENGTH);
                bestOffset = offset;
            }
        }

        return new int[]{bestLength, bestOffset};
    }

    private int matchLength(byte[] data, int pos1, int pos2) {
        int maxLen = Math.min(MAX_MATCH_LENGTH, data.length - pos2);
        int len = 0;

        while (len < maxLen && pos1 + len < pos2 && data[pos1 + len] == data[pos2 + len]) {
            len++;
        }

        return len;
    }

    private void addHashEntry(byte[] data, int pos) {
        if (pos + 3 <= data.length) {
            int hash = hash3Bytes(data, pos);
            hashTable.computeIfAbsent(hash, k -> new ArrayList<>()).add(pos);
        }
    }

    private void addHashEntries(byte[] data, int pos, int length) {
        for (int i = 0; i < length && pos + i + 3 <= data.length; i++) {
            addHashEntry(data, pos + i);
        }
    }
}
