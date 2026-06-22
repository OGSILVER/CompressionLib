package com.compressionlib.lossless.huffman;

class HuffmanNode implements Comparable<HuffmanNode> {
    byte value;
    int frequency;
    HuffmanNode left;
    HuffmanNode right;
    boolean isLeaf;

    HuffmanNode(byte value, int frequency) {
        this.value = value;
        this.frequency = frequency;
        this.isLeaf = true;
    }

    HuffmanNode(HuffmanNode left, HuffmanNode right) {
        this.frequency = left.frequency + right.frequency;
        this.left = left;
        this.right = right;
        this.isLeaf = false;
    }

    @Override
    public int compareTo(HuffmanNode other) {
        return Integer.compare(this.frequency, other.frequency);
    }
}
