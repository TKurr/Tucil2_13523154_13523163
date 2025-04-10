package com;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;

public class CompressionUtils {

    public static double computeVariance(BufferedImage img, Rectangle region, Color avgColor) {
        double sumVar = 0;
        int count = 0;
        for (int y = region.y; y < region.y + region.height; y++) {
            for (int x = region.x; x < region.x + region.width; x++) {
                Color c = new Color(img.getRGB(x, y));
                double diff = (c.getRed() - avgColor.getRed()) + 
                              (c.getGreen() - avgColor.getGreen()) + 
                              (c.getBlue() - avgColor.getBlue());
                sumVar += diff * diff;
                count++;
            }
        }
        return sumVar / count;
    }

    public static Color computeAverageColor(BufferedImage img, Rectangle region) {
        int sumR = 0, sumG = 0, sumB = 0, count = 0;
        for (int y = region.y; y < region.y + region.height; y++) {
            for (int x = region.x; x < region.x + region.width; x++) {
                Color c = new Color(img.getRGB(x, y));
                sumR += c.getRed();
                sumG += c.getGreen();
                sumB += c.getBlue();
                count++;
            }
        }
        return new Color(sumR / count, sumG / count, sumB / count);
    }

    public static void drawCompressedImage(Graphics g, QuadTreeNode node) {
        if (node == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF); 

        if (node.isLeaf) {
            g2d.setColor(node.averageColor);
            g2d.fillRect(node.region.x, node.region.y, node.region.width, node.region.height);
        } else {
            for (QuadTreeNode child : node.children) {
                drawCompressedImage(g2d, child);
            }
        }
    }

    public static double computeMAD(BufferedImage img, Rectangle region, Color avgColor) {
        double sumVar = 0;
        int count = 0;
        for (int y = region.y; y < region.y + region.height; y++) {
            for (int x = region.x; x < region.x + region.width; x++) {
                Color c = new Color(img.getRGB(x, y));
                double diff = (c.getRed() - avgColor.getRed()) + 
                              (c.getGreen() - avgColor.getGreen()) + 
                              (c.getBlue() - avgColor.getBlue());
                sumVar += Math.abs(diff);
                count++;
            }
        }
        return sumVar / count;
    }

    public static double computeMaxPixelDifference(BufferedImage img, Rectangle region, Color avgColor) {
        int maxR = Integer.MIN_VALUE, minR = Integer.MAX_VALUE;
        int maxG = Integer.MIN_VALUE, minG = Integer.MAX_VALUE;
        int maxB = Integer.MIN_VALUE, minB = Integer.MAX_VALUE;

        for (int y = region.y; y < region.y + region.height; y++) {
            for (int x = region.x; x < region.x + region.width; x++) {
                Color c = new Color(img.getRGB(x, y));

                maxR = Math.max(maxR, c.getRed());
                minR = Math.min(minR, c.getRed());

                maxG = Math.max(maxG, c.getGreen());
                minG = Math.min(minG, c.getGreen());

                maxB = Math.max(maxB, c.getBlue());
                minB = Math.min(minB, c.getBlue());
            }
        }

        double DR = maxR - minR;
        double DG = maxG - minG;
        double DB = maxB - minB;

        return (DR + DG + DB) / 3.0;
    }

    public static double computeEntropy(BufferedImage img, Rectangle region) {
        int[] histogramR = new int[256];
        int[] histogramG = new int[256];
        int[] histogramB = new int[256];
        int totalPixels = region.width * region.height;

        for (int y = region.y; y < region.y + region.height; y++) {
            for (int x = region.x; x < region.x + region.width; x++) {
                Color c = new Color(img.getRGB(x, y));
                histogramR[c.getRed()]++;
                histogramG[c.getGreen()]++;
                histogramB[c.getBlue()]++;
            }
        }

        double entropyR = computeChannelEntropy(histogramR, totalPixels);
        double entropyG = computeChannelEntropy(histogramG, totalPixels);
        double entropyB = computeChannelEntropy(histogramB, totalPixels);

        return (entropyR + entropyG + entropyB) / 3.0;
    }

    public static double computeChannelEntropy(int[] histogram, int totalPixels) {
        double entropy = 0.0;
        for (int i = 0; i < 256; i++) {
            if (histogram[i] > 0) {
                double probability = (double) histogram[i] / totalPixels;
                entropy -= probability * Math.log(probability) / Math.log(2); 
            }
        }
        return entropy;
    }
    public static double computeSSIM(BufferedImage img, Rectangle region) {
        int width = region.width;
        int height = region.height;

        double muX_R = 0.0, muY_R = 0.0, muX_G = 0.0, muY_G = 0.0, muX_B = 0.0, muY_B = 0.0;
        double sigmaX_R = 0.0, sigmaY_R = 0.0, sigmaX_G = 0.0, sigmaY_G = 0.0, sigmaX_B = 0.0, sigmaY_B = 0.0;
        double sigmaXY_R = 0.0, sigmaXY_G = 0.0, sigmaXY_B = 0.0;

        for (int y = region.y; y < region.y + height; y++) {
            for (int x = region.x; x < region.x + width; x++) {
                Color c = new Color(img.getRGB(x, y));
                muX_R += c.getRed();
                muX_G += c.getGreen();
                muX_B += c.getBlue();
            }
        }

        muX_R /= (width * height);
        muX_G /= (width * height);
        muX_B /= (width * height);

        for (int y = region.y; y < region.y + height; y++) {
            for (int x = region.x; x < region.x + width; x++) {
                Color c = new Color(img.getRGB(x, y));

                sigmaX_R += Math.pow(c.getRed() - muX_R, 2);
                sigmaX_G += Math.pow(c.getGreen() - muX_G, 2);
                sigmaX_B += Math.pow(c.getBlue() - muX_B, 2);

                sigmaXY_R += (c.getRed() - muX_R) * (c.getGreen() - muX_G);
                sigmaXY_G += (c.getGreen() - muX_G) * (c.getBlue() - muX_B); 
            }
        }

        sigmaX_R /= (width * height);
        sigmaX_G /= (width * height);
        sigmaX_B /= (width * height);

        sigmaXY_R /= (width * height);
        sigmaXY_G /= (width * height);
        sigmaXY_B /= (width * height);

        double C1 = Math.pow(0.01 * 255, 2);  
        double C2 = Math.pow(0.03 * 255, 2);  

        double SSIM_R = (2 * muX_R * muY_R + C1) * (2 * sigmaXY_R + C2) / ((Math.pow(muX_R, 2) + Math.pow(muY_R, 2) + C1) * (Math.pow(sigmaX_R, 2) + Math.pow(sigmaY_R, 2) + C2));
        double SSIM_G = (2 * muX_G * muY_G + C1) * (2 * sigmaXY_G + C2) / ((Math.pow(muX_G, 2) + Math.pow(muY_G, 2) + C1) * (Math.pow(sigmaX_G, 2) + Math.pow(sigmaY_G, 2) + C2));
        double SSIM_B = (2 * muX_B * muY_B + C1) * (2 * sigmaXY_B + C2) / ((Math.pow(muX_B, 2) + Math.pow(muY_B, 2) + C1) * (Math.pow(sigmaX_B, 2) + Math.pow(sigmaY_B, 2) + C2));

        double SSIM_RGB = (SSIM_R + SSIM_G + SSIM_B) / 3.0;

        return SSIM_RGB;
    }

}
