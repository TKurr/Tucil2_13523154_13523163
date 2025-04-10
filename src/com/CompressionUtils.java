package com;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;

public class CompressionUtils {
        // Compute variance
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
    
        // Compute max depth of tree
        public static int computeDepth(QuadTreeNode node) {
            if (node == null || node.isLeaf) return 0;
            int maxDepth = 0;
            for (QuadTreeNode child : node.children) {
                maxDepth = Math.max(maxDepth, computeDepth(child));
            }
            return maxDepth + 1;
        }
    
        // Count total nodes in tree
        public static int countNodes(QuadTreeNode node) {
            if (node == null) return 0;
            int count = 1;
            for (QuadTreeNode child : node.children) {
                count += countNodes(child);
            }
            return count;
        }
    
        // Compute average color
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
    
        // Draw compressed image
        public static void drawCompressedImage(Graphics g, QuadTreeNode node) {
            if (node == null) return;
    
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF); // Matikan anti-aliasing
    
            if (node.isLeaf) {
                g2d.setColor(node.averageColor);
                g2d.fillRect(node.region.x, node.region.y, node.region.width, node.region.height);
            } else {
                for (QuadTreeNode child : node.children) {
                    drawCompressedImage(g2d, child);
                }
            }
        }
}
