package com;

import com.CompressionUtils;
import java.awt.Graphics;
import com.QuadTreeNode;
import java.awt.Rectangle;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class Compression {

    public static QuadTreeNode buildQuadTree(BufferedImage img, Rectangle region, double threshold, int minBlockSize) {
        if (region.width <= minBlockSize || region.height <= minBlockSize) {
            return new QuadTreeNode(region, CompressionUtils.computeAverageColor(img, region), true);
        }

        Color avgColor = CompressionUtils.computeAverageColor(img, region);
        double error = CompressionUtils.computeVariance(img, region, avgColor); // Bisa diganti dengan metode lain

        if (error <= threshold) {
            return new QuadTreeNode(region, avgColor, true);
        }

        int halfWidth = region.width / 2;
        int halfHeight = region.height / 2;

        if (halfWidth < minBlockSize && halfHeight < minBlockSize) {
            return new QuadTreeNode(region, avgColor, true);
        }

        Rectangle[] subRegions = {
            new Rectangle(region.x, region.y, halfWidth, halfHeight),
            new Rectangle(region.x + halfWidth, region.y, region.width - halfWidth, halfHeight),
            new Rectangle(region.x, region.y + halfHeight, halfWidth, region.height - halfHeight),
            new Rectangle(region.x + halfWidth, region.y + halfHeight, region.width - halfWidth, region.height - halfHeight)
        };

        boolean shouldDivide = true;
        for (Rectangle subRegion : subRegions) {
            double subError = CompressionUtils.computeVariance(img, subRegion, CompressionUtils.computeAverageColor(img, subRegion));
            if (subError <= threshold) {
                shouldDivide = false;
                break;
            }
        }

        if (!shouldDivide) {
            return new QuadTreeNode(region, avgColor, true);
        }

        QuadTreeNode node = new QuadTreeNode(region, avgColor, false);
        for (int i = 0; i < 4; i++) {
            node.children[i] = buildQuadTree(img, subRegions[i], threshold, minBlockSize);
        }

        return node;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // User Inputs
        System.out.print("Masukkan alamat absolut gambar yang akan dikompresi: ");
        String inputPath = scanner.nextLine();

        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            System.out.println("File gambar tidak ditemukan.");
            return;
        }

        System.out.print("Masukkan ambang batas error: ");
        double threshold = scanner.nextDouble();

        System.out.print("Masukkan ukuran blok minimum: ");
        int minBlockSize = scanner.nextInt();

        String outputPath = System.getProperty("user.dir") + "\\..\\test\\output.png";

        scanner.close();

        try {
            // Load image
            BufferedImage img = ImageIO.read(new File(inputPath));
            long originalSize = inputFile.length();

            long startTime = System.nanoTime();

            QuadTreeNode root = buildQuadTree(img, new Rectangle(0, 0, img.getWidth(), img.getHeight()), threshold, minBlockSize);

            BufferedImage compressedImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics g = compressedImg.getGraphics();
            CompressionUtils.drawCompressedImage(g, root);
            g.dispose();
            ImageIO.write(compressedImg, "png", new File(outputPath));

            long endTime = System.nanoTime();
            long executionTime = (endTime - startTime) / 1_000_000;

            long compressedSize = new File(outputPath).length();
            double compressionRatio = (1.0 - ((double) compressedSize / originalSize)) * 100;
            int depth = CompressionUtils.computeDepth(root);
            int nodeCount = CompressionUtils.countNodes(root);

            System.out.println("\n=== HASIL KOMPRESI ===");
            System.out.println("Waktu eksekusi: " + executionTime + " ms");
            System.out.println("Ukuran gambar sebelum: " + originalSize / 1024 + " KB");
            System.out.println("Ukuran gambar setelah: " + compressedSize / 1024 + " KB");
            System.out.printf("Persentase kompresi: %.2f%%\n", compressionRatio);
            System.out.println("Kedalaman pohon: " + depth);
            System.out.println("Banyak simpul pada pohon: " + nodeCount);
            System.out.println("Gambar hasil kompresi tersimpan di: " + outputPath);

        } catch (Exception e) {
            System.out.println("Terjadi kesalahan: " + e.getMessage());
        }
    }
}
