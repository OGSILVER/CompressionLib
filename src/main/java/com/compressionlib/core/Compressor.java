package com.compressionlib.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.compressionlib.io.FileFormat;
import com.compressionlib.lossless.huffman.HuffmanCodec;
import com.compressionlib.lossless.lz77.LZ77Codec;
import com.compressionlib.lossless.lzw.LZWCodec;

public class Compressor {
    private Map<Byte, CompressionAlgorithm> algorithms;
    private Map<String, CompressionAlgorithm> algorithmsByName;

    public Compressor() {
        this.algorithms = new HashMap<>();
        this.algorithmsByName = new HashMap<>();

        registerAlgorithm(new HuffmanCodec());
        registerAlgorithm(new LZ77Codec());
        registerAlgorithm(new LZWCodec());
    }

    public void registerAlgorithm(CompressionAlgorithm algorithm) {
        algorithms.put(algorithm.getAlgorithmId(), algorithm);
        algorithmsByName.put(algorithm.getAlgorithmName().toLowerCase(), algorithm);
    }

    public void compressFile(String algorithmName, File input, File output) throws CompressionException {
        try {
            CompressionAlgorithm algo = algorithmsByName.get(algorithmName.toLowerCase());
            if (algo == null) {
                throw new CompressionException("Unknown algorithm: " + algorithmName);
            }

            byte[] inputData = java.nio.file.Files.readAllBytes(input.toPath());
            byte[] compressedData = algo.compress(inputData);

            byte[] metadata = null;
            if (algo instanceof com.compressionlib.lossless.huffman.HuffmanCodec) {
                com.compressionlib.lossless.huffman.HuffmanCodec huffman = (com.compressionlib.lossless.huffman.HuffmanCodec) algo;
                metadata = huffman.getCodeTableMetadata();
            }

            FileFormat.writeCompressedFile(output, algo, inputData, compressedData, metadata);
        } catch (CompressionException e) {
            throw e;
        } catch (Exception e) {
            throw new CompressionException("Failed to compress file: " + e.getMessage(), e);
        }
    }

    public void decompressFile(File input, File output) throws CompressionException {
        try {
            FileFormat.Header header = FileFormat.readCompressedFile(input);

            CompressionAlgorithm algo = algorithms.get(header.algorithmId);
            if (algo == null) {
                throw new CompressionException("Unknown algorithm ID: " + header.algorithmId);
            }

            if (algo instanceof com.compressionlib.lossless.huffman.HuffmanCodec && header.metadata != null) {
                com.compressionlib.lossless.huffman.HuffmanCodec huffman = (com.compressionlib.lossless.huffman.HuffmanCodec) algo;
                huffman.initializeFromMetadata(header.metadata);
            }

            byte[] compressedData = FileFormat.readCompressedData(input);
            byte[] decompressedData = algo.decompress(compressedData);

            if (decompressedData.length != header.originalSize) {
                throw new CompressionException(
                    String.format("Decompressed size mismatch: expected %d, got %d",
                        header.originalSize, decompressedData.length));
            }

            java.nio.file.Files.write(output.toPath(), decompressedData);
        } catch (CompressionException e) {
            throw e;
        } catch (Exception e) {
            throw new CompressionException("Failed to decompress file: " + e.getMessage(), e);
        }
    }

    public String[] getAvailableAlgorithms() {
        return algorithmsByName.keySet().toArray(new String[0]);
    }
}


