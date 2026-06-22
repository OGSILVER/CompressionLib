package com.compressionlib.lossy.quantization;

import java.awt.image.BufferedImage;

public class ColorQuantizer {
    private int bitsPerChannel;

    public ColorQuantizer(int bitsPerChannel) {
        if (bitsPerChannel < 1 || bitsPerChannel > 8) {
            throw new IllegalArgumentException("Bits per channel must be 1-8");
        }
        this.bitsPerChannel = bitsPerChannel;
    }

    public BufferedImage quantize(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int levels = 1 << bitsPerChannel;
        int bucketSize = 256 / levels;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);

                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                r = quantizeChannel(r, bucketSize, levels);
                g = quantizeChannel(g, bucketSize, levels);
                b = quantizeChannel(b, bucketSize, levels);

                int quantizedRgb = (r << 16) | (g << 8) | b;
                result.setRGB(x, y, quantizedRgb);
            }
        }

        return result;
    }

    private int quantizeChannel(int value, int bucketSize, int levels) {
        int bucket = value / bucketSize;
        bucket = Math.min(bucket, levels - 1);
        return bucket * bucketSize + (bucketSize >> 1);
    }

    public static int calculateApproximateColorCount(BufferedImage image, int bitsPerChannel) {
        int levels = 1 << bitsPerChannel;
        return Math.min(levels * levels * levels, 1 << 24);
    }
}
