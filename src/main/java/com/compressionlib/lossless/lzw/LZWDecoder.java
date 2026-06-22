package com.compressionlib.lossless.lzw;

import java.util.*;

class LZWDecoder {
    private static final int INITIAL_CODE_SIZE = 9;
    private static final int MAX_CODE_SIZE = 16;
    private static final int MAX_DICT_SIZE = 1 << MAX_CODE_SIZE;
    private static final int CLEAR_CODE = 256;
    private static final int EOD_CODE = 257;

    byte[] decode(List<Integer> codes) {
        List<Byte> output = new ArrayList<>();

        if (codes.isEmpty()) {
            return new byte[0];
        }

        Map<Integer, String> dictionary = new HashMap<>();
        initializeDictionary(dictionary);

        int currentCodeSize = INITIAL_CODE_SIZE;
        int nextCode = 258;
        String w = "";

        for (int code : codes) {
            if (code == CLEAR_CODE) {
                initializeDictionary(dictionary);
                currentCodeSize = INITIAL_CODE_SIZE;
                nextCode = 258;
                w = "";
                continue;
            }

            if (code == EOD_CODE) {
                break;
            }

            String k;
            if (dictionary.containsKey(code)) {
                k = dictionary.get(code);
            } else if (code == nextCode) {
                k = w + w.charAt(0);
            } else {
                throw new RuntimeException("Invalid LZW code: " + code);
            }

            for (char c : k.toCharArray()) {
                output.add((byte) c);
            }

            if (!w.isEmpty() && nextCode < MAX_DICT_SIZE) {
                dictionary.put(nextCode, w + k.charAt(0));
                nextCode++;

                if (nextCode >= (1 << currentCodeSize) && currentCodeSize < MAX_CODE_SIZE) {
                    currentCodeSize++;
                }
            }

            w = k;
        }

        byte[] result = new byte[output.size()];
        for (int i = 0; i < output.size(); i++) {
            result[i] = output.get(i);
        }
        return result;
    }

    private void initializeDictionary(Map<Integer, String> dictionary) {
        dictionary.clear();
        for (int i = 0; i < 256; i++) {
            dictionary.put(i, String.valueOf((char) i));
        }
    }
}
