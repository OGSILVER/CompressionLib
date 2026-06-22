package com.compressionlib.lossless.lz77;

class LZ77Token {
    static final byte LITERAL_TYPE = 0;
    static final byte MATCH_TYPE = 1;

    byte type;
    byte literal;
    int offset;
    int length;

    private LZ77Token(byte type) {
        this.type = type;
    }

    static LZ77Token createLiteral(byte value) {
        LZ77Token token = new LZ77Token(LITERAL_TYPE);
        token.literal = value;
        return token;
    }

    static LZ77Token createMatch(int offset, int length) {
        LZ77Token token = new LZ77Token(MATCH_TYPE);
        token.offset = offset;
        token.length = length;
        return token;
    }

    boolean isLiteral() {
        return type == LITERAL_TYPE;
    }

    boolean isMatch() {
        return type == MATCH_TYPE;
    }

    @Override
    public String toString() {
        if (isLiteral()) {
            return String.format("Literal(%d)", literal & 0xFF);
        } else {
            return String.format("Match(offset=%d, length=%d)", offset, length);
        }
    }
}
