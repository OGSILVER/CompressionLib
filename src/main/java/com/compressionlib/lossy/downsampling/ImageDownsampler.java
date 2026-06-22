package com.compressionlib.lossy.downsampling;

import java.awt.Image;
import java.awt.image.BufferedImage;

public class ImageDownsampler {
    private int factor;

    public ImageDownsampler(int factor) {
        if (factor < 1) {
            throw new IllegalArgumentException("Factor must be >= 1");
        }
        this.factor = factor;
    }

    public BufferedImage downsample(BufferedImage image) {
        if (factor == 1) {
            return image;
        }

        int newWidth = Math.max(1, image.getWidth() / factor);
        int newHeight = Math.max(1, image.getHeight() / factor);

        BufferedImage downsampled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                int sourceX = x * factor;
                int sourceY = y * factor;

                int avgR = 0;
                int avgG = 0;
                int avgB = 0;
                int count = 0;

                for (int dy = 0; dy < factor && sourceY + dy < image.getHeight(); dy++) {
                    for (int dx = 0; dx < factor && sourceX + dx < image.getWidth(); dx++) {
                        int rgb = image.getRGB(sourceX + dx, sourceY + dy);

                        avgR += (rgb >> 16) & 0xFF;
                        avgG += (rgb >> 8) & 0xFF;
                        avgB += rgb & 0xFF;
                        count++;
                    }
                }

                avgR /= count;
                avgG /= count;
                avgB /= count;

                int resultRgb = (avgR << 16) | (avgG << 8) | avgB;
                downsampled.setRGB(x, y, resultRgb);
            }
        }

        return downsampled;
    }

    public BufferedImage upsample(BufferedImage image, int originalWidth, int originalHeight) {
        BufferedImage upsampled = new BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < originalHeight; y++) {
            for (int x = 0; x < originalWidth; x++) {
                int sourceX = x / factor;
                int sourceY = y / factor;

                sourceX = Math.min(sourceX, image.getWidth() - 1);
                sourceY = Math.min(sourceY, image.getHeight() - 1);

                int rgb = image.getRGB(sourceX, sourceY);
                upsampled.setRGB(x, y, rgb);
            }
        }

        return upsampled;
    }
}
