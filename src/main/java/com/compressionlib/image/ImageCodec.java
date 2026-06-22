package com.compressionlib.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import com.compressionlib.lossy.quantization.ColorQuantizer;
import com.compressionlib.lossy.downsampling.ImageDownsampler;

public class ImageCodec {

    public static BufferedImage readImage(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new IOException("Failed to read image: " + file.getPath());
        }
        return image;
    }

    public static void writeImage(BufferedImage image, File file) throws IOException {
        String filename = file.getName().toLowerCase();
        String format = "png";

        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            format = "jpg";
        } else if (filename.endsWith(".gif")) {
            format = "gif";
        } else if (filename.endsWith(".bmp")) {
            format = "bmp";
        }

        boolean success = ImageIO.write(image, format, file);
        if (!success) {
            throw new IOException("Failed to write image: " + file.getPath());
        }
    }

    public static void quantizeImage(File inputFile, File outputFile, int bitsPerChannel) throws IOException {
        BufferedImage original = readImage(inputFile);
        ColorQuantizer quantizer = new ColorQuantizer(bitsPerChannel);
        BufferedImage quantized = quantizer.quantize(original);
        writeImage(quantized, outputFile);
    }

    public static void downsampleImage(File inputFile, File outputFile, int factor) throws IOException {
        BufferedImage original = readImage(inputFile);
        ImageDownsampler downsampler = new ImageDownsampler(factor);
        BufferedImage downsampled = downsampler.downsample(original);
        writeImage(downsampled, outputFile);
    }

    public static void downsampleAndUpsample(File inputFile, File outputFile, int factor) throws IOException {
        BufferedImage original = readImage(inputFile);
        ImageDownsampler downsampler = new ImageDownsampler(factor);

        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        BufferedImage downsampled = downsampler.downsample(original);
        BufferedImage upsampled = downsampler.upsample(downsampled, originalWidth, originalHeight);

        writeImage(upsampled, outputFile);
    }
}
