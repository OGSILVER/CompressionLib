package com.compressionlib.lossless.huffman;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import com.compressionlib.io.BitInputStream;
import com.compressionlib.io.BitOutputStream;

class HuffmanCodeTable {
    private Map<Byte, String> codeMap;
    private Map<String, Byte> reverseCodeMap;
    private int maxCodeLength;
    private int originalByteCount;

    private HuffmanCodeTable() {
        this.codeMap = new HashMap<>();
        this.reverseCodeMap = new HashMap<>();
        this.maxCodeLength = 0;
        this.originalByteCount = 0;
    }

    static HuffmanCodeTable buildFromData(byte[] data) {
        HuffmanCodeTable table = new HuffmanCodeTable();
        table.originalByteCount = data == null ? 0 : data.length;

        if (data == null || data.length == 0) {
            return table;
        }

        Map<Byte, Integer> frequencies = new HashMap<>();
        for (byte b : data) {
            frequencies.put(b, frequencies.getOrDefault(b, 0) + 1);
        }

        if (frequencies.size() == 1) {
            byte onlyByte = frequencies.keySet().iterator().next();
            table.codeMap.put(onlyByte, "0");
            table.reverseCodeMap.put("0", onlyByte);
            table.maxCodeLength = 1;
            return table;
        }

        PriorityQueue<HuffmanNode> heap = new PriorityQueue<>();
        for (Map.Entry<Byte, Integer> entry : frequencies.entrySet()) {
            heap.offer(new HuffmanNode(entry.getKey(), entry.getValue()));
        }

        while (heap.size() > 1) {
            HuffmanNode left = heap.poll();
            HuffmanNode right = heap.poll();
            heap.offer(new HuffmanNode(left, right));
        }

        HuffmanNode root = heap.poll();
        generateCodes(root, "", table);

        return table;
    }

    private static void generateCodes(HuffmanNode node, String code, HuffmanCodeTable table) {
        if (node.isLeaf) {
            table.codeMap.put(node.value, code.isEmpty() ? "0" : code);
            table.reverseCodeMap.put(code.isEmpty() ? "0" : code, node.value);
            table.maxCodeLength = Math.max(table.maxCodeLength, code.length());
        } else {
            if (node.left != null) {
                generateCodes(node.left, code + "0", table);
            }
            if (node.right != null) {
                generateCodes(node.right, code + "1", table);
            }
        }
    }

    String encode(byte b) {
        return codeMap.getOrDefault(b, "0");
    }

    boolean canDecode(String code) {
        return reverseCodeMap.containsKey(code);
    }

    byte decode(String code) {
        return reverseCodeMap.get(code);
    }

    byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BitOutputStream bos = new BitOutputStream(baos)) {
            bos.writeBits(originalByteCount, 32);
            bos.writeBits(codeMap.size(), 16);

            for (Map.Entry<Byte, String> entry : codeMap.entrySet()) {
                bos.writeByte(entry.getKey());
                String code = entry.getValue();
                bos.writeBits(code.length(), 8);
                for (char c : code.toCharArray()) {
                    bos.writeBit(c == '1');
                }
            }
        }
        return baos.toByteArray();
    }

    static HuffmanCodeTable deserialize(byte[] data) throws IOException {
        HuffmanCodeTable table = new HuffmanCodeTable();

        if (data == null || data.length == 0) {
            return table;
        }

        try (BitInputStream bis = new BitInputStream(new ByteArrayInputStream(data))) {
            table.originalByteCount = (int) bis.readBits(32);
            int tableSize = (int) bis.readBits(16);

            for (int i = 0; i < tableSize; i++) {
                byte value = bis.readByte();
                int codeLength = (int) bis.readBits(8);

                StringBuilder code = new StringBuilder();
                for (int j = 0; j < codeLength; j++) {
                    code.append(bis.readBit() ? '1' : '0');
                }

                String codeStr = code.toString();
                table.codeMap.put(value, codeStr);
                table.reverseCodeMap.put(codeStr, value);
                table.maxCodeLength = Math.max(table.maxCodeLength, codeLength);
            }
        }

        return table;
    }

    boolean isEmpty() {
        return codeMap.isEmpty();
    }

    Map<Byte, String> getCodeMap() {
        return new HashMap<>(codeMap);
    }

    int getMaxCodeLength() {
        return maxCodeLength;
    }

    int getOriginalByteCount() {
        return originalByteCount;
    }
}
