package com.compressionlib.benchmark;

import com.compressionlib.core.CompressionAlgorithm;
import com.compressionlib.core.CompressionException;
import com.compressionlib.lossless.huffman.HuffmanCodec;
import com.compressionlib.lossless.lz77.LZ77Codec;
import com.compressionlib.lossless.lzw.LZWCodec;

import java.util.*;

public class CompressionBenchmark {
    private static class Result {
        String algorithmName;
        long originalSize;
        long compressedSize;
        long compressionTime;
        long decompressionTime;

        double getCompressionRatio() {
            return originalSize > 0 ? (double) compressedSize / originalSize : 0;
        }

        double getCompressionMbps() {
            return compressionTime > 0 ? (originalSize / 1024.0 / 1024.0) / (compressionTime / 1000.0) : 0;
        }

        double getDecompressionMbps() {
            return decompressionTime > 0 ? (originalSize / 1024.0 / 1024.0) / (decompressionTime / 1000.0) : 0;
        }
    }

    private List<CompressionAlgorithm> algorithms;

    public CompressionBenchmark() {
        this.algorithms = new ArrayList<>();
        algorithms.add(new HuffmanCodec());
        algorithms.add(new LZ77Codec());
        algorithms.add(new LZWCodec());
    }

    public void benchmark(byte[] data, String label) throws CompressionException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Benchmark: " + label);
        System.out.println("Input size: " + formatBytes(data.length));
        System.out.println("=".repeat(80));

        List<Result> results = new ArrayList<>();

        for (CompressionAlgorithm algo : algorithms) {
            Result result = benchmarkAlgorithm(algo, data);
            results.add(result);
        }

        printResults(results);
    }

    private Result benchmarkAlgorithm(CompressionAlgorithm algo, byte[] data) throws CompressionException {
        Result result = new Result();
        result.algorithmName = algo.getAlgorithmName();
        result.originalSize = data.length;

        long startTime = System.nanoTime();
        byte[] compressed = algo.compress(data);
        long endTime = System.nanoTime();
        result.compressionTime = (endTime - startTime) / 1_000_000;
        result.compressedSize = compressed.length;

        startTime = System.nanoTime();
        byte[] decompressed = algo.decompress(compressed);
        endTime = System.nanoTime();
        result.decompressionTime = (endTime - startTime) / 1_000_000;

        if (!Arrays.equals(data, decompressed)) {
            throw new CompressionException("Decompressed data doesn't match original!");
        }

        return result;
    }

    private void printResults(List<Result> results) {
        System.out.printf("%-12s %-12s %-12s %-12s %-12s %-12s%n",
            "Algorithm", "Compressed", "Ratio", "Comp Time", "Comp Speed", "Decomp Speed");
        System.out.println("-".repeat(80));

        for (Result r : results) {
            System.out.printf("%-12s %-12s %-12.2f%% %-12dms %-12.1f MB/s %-12.1f MB/s%n",
                r.algorithmName,
                formatBytes(r.compressedSize),
                r.getCompressionRatio() * 100,
                r.compressionTime,
                r.getCompressionMbps(),
                r.getDecompressionMbps());
        }
    }

    private String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    public static void main(String[] args) throws CompressionException {
        CompressionBenchmark benchmark = new CompressionBenchmark();

        benchmark.benchmark(generateRepeatingPattern(100_000), "Highly Repetitive (100KB)");
        benchmark.benchmark(generateRandomData(100_000), "Random Data (100KB)");
        benchmark.benchmark(generateTextLike(100_000), "Text-like Data (100KB)");
    }

    private static byte[] generateRepeatingPattern(int size) {
        byte[] data = new byte[size];
        String pattern = "the quick brown fox jumps over the lazy dog. ";
        byte[] patternBytes = pattern.getBytes();

        for (int i = 0; i < size; i++) {
            data[i] = patternBytes[i % patternBytes.length];
        }
        return data;
    }

    private static byte[] generateRandomData(int size) {
        byte[] data = new byte[size];
        Random random = new Random(42);
        random.nextBytes(data);
        return data;
    }

    private static byte[] generateTextLike(int size) {
        byte[] data = new byte[size];
        Random random = new Random(123);
        StringBuilder sb = new StringBuilder();

        String[] words = {"compression", "algorithm", "lossless", "data", "stream", "encode", "decode", "huffman", "lz77", "lzw"};

        while (sb.length() < size) {
            sb.append(words[random.nextInt(words.length)]);
            if (random.nextDouble() < 0.3) {
                sb.append(" ");
            }
            if (random.nextDouble() < 0.1) {
                sb.append("\n");
            }
        }

        byte[] bytes = sb.toString().getBytes();
        System.arraycopy(bytes, 0, data, 0, Math.min(bytes.length, size));
        return data;
    }
}
