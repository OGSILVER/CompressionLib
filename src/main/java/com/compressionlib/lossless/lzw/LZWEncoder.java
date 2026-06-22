package com.compressionlib.lossless.lzw;

import java.util.*;

class LZWEncoder {
    private static final int INITIAL_CODE_SIZE = 9;
    private static final int MAX_CODE_SIZE = 16;
    private static final int MAX_DICT_SIZE = 1 << MAX_CODE_SIZE;
    private static final int CLEAR_CODE = 256;
    private static final int EOD_CODE = 257;

    private Map<String, Integer> dictionary;
    private int nextCode;
    private List<Integer> codes;
    private List<Integer> codeSizes;

    LZWEncoder() {
        this.codes = new ArrayList<>();
        this.codeSizes = new ArrayList<>();
        initializeDictionary();
    }

    private void initializeDictionary() {
        dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(String.valueOf((char) i), i);
        }
        nextCode = 258;
    }

    List<Integer> encode(byte[] data) {
        codes.clear();
        codeSizes.clear();
        initializeDictionary();

        if (data == null || data.length == 0) {
            return codes;
        }

        codes.add(CLEAR_CODE);
        codeSizes.add(INITIAL_CODE_SIZE);

        String w = "";

        for (byte b : data) {
            String c = String.valueOf((char) (b & 0xFF));
            String wc = w + c;

            if (dictionary.containsKey(wc)) {
                w = wc;
            } else {
                int code = dictionary.get(w);
                codes.add(code);

                int codeSize = INITIAL_CODE_SIZE;
                for (int i = INITIAL_CODE_SIZE; i <= MAX_CODE_SIZE; i++) {
                    if (nextCode >= (1 << (i - 1))) {
                        codeSize = i;
                    }
                }
                codeSizes.add(codeSize);

                if (nextCode < MAX_DICT_SIZE) {
                    dictionary.put(wc, nextCode);
                    nextCode++;
                }

                w = c;
            }
        }

        if (!w.isEmpty()) {
            int code = dictionary.get(w);
            codes.add(code);

            int codeSize = INITIAL_CODE_SIZE;
            for (int i = INITIAL_CODE_SIZE; i <= MAX_CODE_SIZE; i++) {
                if (nextCode >= (1 << (i - 1))) {
                    codeSize = i;
                }
            }
            codeSizes.add(codeSize);
        }

        codes.add(EOD_CODE);
        int codeSize = INITIAL_CODE_SIZE;
        for (int i = INITIAL_CODE_SIZE; i <= MAX_CODE_SIZE; i++) {
            if (nextCode >= (1 << (i - 1))) {
                codeSize = i;
            }
        }
        codeSizes.add(codeSize);

        return codes;
    }

    List<Integer> getCodeSizes() {
        return codeSizes;
    }
}

