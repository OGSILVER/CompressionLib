package com.compressionlib.cli;

import com.compressionlib.core.Compressor;
import com.compressionlib.core.CompressionException;
import java.io.File;

public class CompressionCLI {
    private static Compressor compressor = new Compressor();

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }

        try {
            processArguments(args);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void processArguments(String[] args) throws Exception {
        String command = args[0];

        switch (command) {
            case "-c":
            case "--compress":
                handleCompress(args);
                break;
            case "-d":
            case "--decompress":
                handleDecompress(args);
                break;
            case "--quantize":
                handleQuantize(args);
                break;
            case "--downsample":
                handleDownsample(args);
                break;
            case "--benchmark":
                handleBenchmark(args);
                break;
            default:
                System.err.println("Unknown command: " + command);
                printUsage();
                System.exit(1);
        }
    }

    private static void handleCompress(String[] args) throws CompressionException {
        if (args.length < 5) {
            System.err.println("Usage: --compress --algo <algorithm> <input> <output>");
            System.exit(1);
        }

        if (!args[1].equals("--algo")) {
            System.err.println("Expected --algo flag");
            System.exit(1);
        }

        String algorithm = args[2];
        String inputPath = args[3];
        String outputPath = args[4];

        File inputFile = new File(inputPath);
        File outputFile = new File(outputPath);

        if (!inputFile.exists()) {
            System.err.println("Input file not found: " + inputPath);
            System.exit(1);
        }

        long startTime = System.currentTimeMillis();
        compressor.compressFile(algorithm, inputFile, outputFile);
        long elapsed = System.currentTimeMillis() - startTime;

        long originalSize = inputFile.length();
        long compressedSize = outputFile.length();
        double ratio = (double) compressedSize / originalSize * 100;

        System.out.println("Compression successful!");
        System.out.println("Algorithm: " + algorithm);
        System.out.println("Original size: " + formatBytes(originalSize));
        System.out.println("Compressed size: " + formatBytes(compressedSize));
        System.out.println("Ratio: " + String.format("%.2f%%", ratio));
        System.out.println("Time: " + elapsed + "ms");
    }

    private static void handleDecompress(String[] args) throws CompressionException {
        if (args.length < 3) {
            System.err.println("Usage: --decompress <input> <output>");
            System.exit(1);
        }

        String inputPath = args[1];
        String outputPath = args[2];

        File inputFile = new File(inputPath);
        File outputFile = new File(outputPath);

        if (!inputFile.exists()) {
            System.err.println("Input file not found: " + inputPath);
            System.exit(1);
        }

        long startTime = System.currentTimeMillis();
        compressor.decompressFile(inputFile, outputFile);
        long elapsed = System.currentTimeMillis() - startTime;

        long compressedSize = inputFile.length();
        long originalSize = outputFile.length();

        System.out.println("Decompression successful!");
        System.out.println("Compressed size: " + formatBytes(compressedSize));
        System.out.println("Original size: " + formatBytes(originalSize));
        System.out.println("Time: " + elapsed + "ms");
    }

    private static void handleQuantize(String[] args) throws Exception {
        if (args.length < 5 || !args[1].equals("--colors")) {
            System.err.println("Usage: --quantize --colors <bits> <input.png> <output.png>");
            System.exit(1);
            return;
        }

        int bitsPerChannel = 0;
        try {
            bitsPerChannel = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid bits per channel: " + args[2]);
            System.exit(1);
            return;
        }

        String inputPath = args[3];
        String outputPath = args[4];

        File inputFile = new File(inputPath);
        File outputFile = new File(outputPath);

        if (!inputFile.exists()) {
            System.err.println("Input file not found: " + inputPath);
            System.exit(1);
            return;
        }

        try {
            com.compressionlib.image.ImageCodec.quantizeImage(inputFile, outputFile, bitsPerChannel);
            System.out.println("Color quantization successful!");
            System.out.println("Bits per channel: " + bitsPerChannel);
            System.out.println("Approximate color count: " + (1 << (bitsPerChannel * 3)));
            System.out.println("Output: " + outputPath);
        } catch (Exception e) {
            System.err.println("Quantization failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void handleDownsample(String[] args) throws Exception {
        if (args.length < 5 || !args[1].equals("--factor")) {
            System.err.println("Usage: --downsample --factor <factor> <input.png> <output.png>");
            System.exit(1);
            return;
        }

        int factor = 0;
        try {
            factor = Integer.parseInt(args[2]);
            if (factor < 1) {
                throw new NumberFormatException("Factor must be >= 1");
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid factor: " + args[2]);
            System.exit(1);
            return;
        }

        String inputPath = args[3];
        String outputPath = args[4];

        File inputFile = new File(inputPath);
        File outputFile = new File(outputPath);

        if (!inputFile.exists()) {
            System.err.println("Input file not found: " + inputPath);
            System.exit(1);
            return;
        }

        try {
            com.compressionlib.image.ImageCodec.downsampleImage(inputFile, outputFile, factor);
            System.out.println("Downsampling successful!");
            System.out.println("Downsample factor: " + factor);
            System.out.println("Output: " + outputPath);
        } catch (Exception e) {
            System.err.println("Downsampling failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void handleBenchmark(String[] args) throws Exception {
        com.compressionlib.benchmark.CompressionBenchmark.main(new String[0]);
    }

    private static String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    private static void printUsage() {
        System.out.println("Compression Library CLI");
        System.out.println();
        System.out.println("Lossless Compression:");
        System.out.println("  -c, --compress --algo <algorithm> <input> <output>");
        System.out.println("                 Compress input file using specified algorithm");
        System.out.println("  -d, --decompress <input> <output>");
        System.out.println("                 Decompress input file");
        System.out.println();
        System.out.println("Algorithms: huffman, lz77, lzw");
        System.out.println();
        System.out.println("Image Processing:");
        System.out.println("  --quantize --colors <num> <input.png> <output.png>");
        System.out.println("                 Reduce colors using quantization");
        System.out.println("  --downsample --factor <num> <input.png> <output.png>");
        System.out.println("                 Downsample image resolution");
        System.out.println();
        System.out.println("Utilities:");
        System.out.println("  --benchmark     Run compression benchmark on sample data");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar compresslib.jar -c --algo huffman input.txt output.cmp");
        System.out.println("  java -jar compresslib.jar -d output.cmp restored.txt");
        System.out.println("  java -jar compresslib.jar --benchmark");
    }
}

