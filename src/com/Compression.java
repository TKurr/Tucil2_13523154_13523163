package com;

import java.awt.Graphics;
import java.awt.Rectangle;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class Compression {
    public static boolean isSSIM =  false;
    
    public static double selectMethod(BufferedImage img, Rectangle region, Color avgColor, String errorMethod) {
        switch (errorMethod) {
            case "1": 
                return CompressionUtils.Variance(img, region, avgColor);
            case "2": 
                return CompressionUtils.MAD(img, region, avgColor);
            case "3": 
                return CompressionUtils.MaxPixelDifference(img, region, avgColor);
            case "4": 
                return CompressionUtils.Entropy(img, region);
            case "5": 
                isSSIM = true;
                return CompressionUtils.SSIM(img, region);
            default:
                throw new IllegalArgumentException("Metode error tidak valid.");
        }
    }

    public static QuadTreeNode buildQuadTree(BufferedImage img, Rectangle region, double threshold, int minBlockSize, String errorMethod) {
        if (region.width * region.height <= minBlockSize) {
            return new QuadTreeNode(region, CompressionUtils.AverageColor(img, region), true);
        }

        Color avgColor = CompressionUtils.AverageColor(img, region);
        double error = selectMethod(img, region, avgColor, errorMethod); 
        if (isSSIM){
            if (error > threshold) {
                return new QuadTreeNode(region, avgColor, false);
            }
        } else {
            if (error <= threshold) {
                return new QuadTreeNode(region, avgColor, true);
            }
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
            double subError = selectMethod(img, subRegion, CompressionUtils.AverageColor(img, subRegion), errorMethod);
            if (isSSIM){
                if (subError > threshold) {
                    shouldDivide = false;
                    break;
                }
            } 
            else {
                if (subError <= threshold) {
                    shouldDivide = false;
                    break;
                }
            }
        }

        if (!shouldDivide) {
            return new QuadTreeNode(region, avgColor, true);
        }

        QuadTreeNode node = new QuadTreeNode(region, avgColor, false);
        for (int i = 0; i < 4; i++) {
            node.children[i] = buildQuadTree(img, subRegions[i], threshold, minBlockSize, errorMethod);
        }

        return node;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Masukkan alamat absolut gambar yang akan dikompresi: ");
        String inputPath = scanner.nextLine();

        String errorMethod = "";
        while (true) {
            System.out.println("\nPilih metode pengukuran error:");
            System.out.println("1. Variance");
            System.out.println("2. Mean Absolute Deviation (MAD)");
            System.out.println("3. Max Pixel Difference");
            System.out.println("4. Entropy");
            System.out.println("5. Structural Similarity Index (SSIM)");
            System.out.print("Pilih antara 1 - 5: ");
            errorMethod = scanner.next();

            if (errorMethod.equals("1") || errorMethod.equals("2") || errorMethod.equals("3") || errorMethod.equals("4") || errorMethod.equals("5")) {
                break;
            } else {
                System.out.println("Masukan salah! Silakan pilih antara 1 sampai 5.");
            }
        }

        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            System.out.println("File gambar tidak ditemukan.");
            return;
        }

        System.out.print("Masukkan ambang batas error: ");
        double threshold = scanner.nextDouble();

        System.out.print("Masukkan ukuran blok minimum: ");
        int minBlockSize = scanner.nextInt();

        System.out.print("Masukkan nama file output (tanpa ekstensi .png): ");
        String outputFileName = scanner.next();
        String outputPath = "test/" + outputFileName + ".png";

        scanner.close();

        try {
            BufferedImage img = ImageIO.read(new File(inputPath));
            long originalSize = inputFile.length();

            long startTime = System.nanoTime();

            QuadTreeNode root = buildQuadTree(img, new Rectangle(0, 0, img.getWidth(), img.getHeight()), threshold, minBlockSize, errorMethod);

            BufferedImage compressedImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics g = compressedImg.getGraphics();
            CompressionUtils.drawCompressedImage(g, root);
            g.dispose();
            ImageIO.write(compressedImg, "png", new File(outputPath));

            long endTime = System.nanoTime();
            long executionTime = (endTime - startTime) / 1000000;

            long compressedSize = new File(outputPath).length();
            double compressionRatio = (1.0 - ((double) compressedSize / originalSize)) * 100;
            int depth = QuadTreeNode.countDepths(root);
            int nodeCount = QuadTreeNode.countNodes(root);

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
