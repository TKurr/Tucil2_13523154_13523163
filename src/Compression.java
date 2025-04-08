import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.imageio.ImageIO;

class QuadTreeNode {
    Rectangle region;
    Color averageColor;
    boolean isLeaf;
    QuadTreeNode[] children;

    public QuadTreeNode(Rectangle region, Color color, boolean leaf) {
        this.region = region;
        this.averageColor = color;
        this.isLeaf = leaf;
        this.children = new QuadTreeNode[4]; // 4 children for quad tree
    }
}

public class Compression {
    
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

    public static QuadTreeNode buildQuadTree(BufferedImage img, Rectangle region, double threshold, int minBlockSize) {
        if (region.width <= minBlockSize || region.height <= minBlockSize) {
            return new QuadTreeNode(region, computeAverageColor(img, region), true);
        }
    
        Color avgColor = computeAverageColor(img, region);
        double error = computeVariance(img, region, avgColor); // Bisa diganti dengan metode lain
    
        // Jika error di bawah threshold, jangan bagi lagi (langsung buat simpul daun)
        if (error <= threshold) {
            return new QuadTreeNode(region, avgColor, true);
        }
    
        // Pastikan bisa dibagi lebih lanjut
        int halfWidth = region.width / 2;
        int halfHeight = region.height / 2;
        
        // Jika terlalu kecil untuk dibagi, buat simpul daun saja
        if (halfWidth < minBlockSize && halfHeight < minBlockSize) {
            return new QuadTreeNode(region, avgColor, true);
        }
    
        // Bagi menjadi empat blok, lalu cek apakah semuanya di atas threshold
        Rectangle[] subRegions = {
            new Rectangle(region.x, region.y, halfWidth, halfHeight),
            new Rectangle(region.x + halfWidth, region.y, region.width - halfWidth, halfHeight),
            new Rectangle(region.x, region.y + halfHeight, halfWidth, region.height - halfHeight),
            new Rectangle(region.x + halfWidth, region.y + halfHeight, region.width - halfWidth, region.height - halfHeight)
        };
    
        boolean shouldDivide = true;
        for (Rectangle subRegion : subRegions) {
            double subError = computeVariance(img, subRegion, computeAverageColor(img, subRegion));
            if (subError <= threshold) {
                shouldDivide = false;
                break;
            }
        }
    
        if (!shouldDivide) {
            return new QuadTreeNode(region, avgColor, true);
        }
    
        // Jika semua blok memiliki error di atas threshold, lakukan pembagian
        QuadTreeNode node = new QuadTreeNode(region, avgColor, false);
        for (int i = 0; i < 4; i++) {
            node.children[i] = buildQuadTree(img, subRegions[i], threshold, minBlockSize);
        }
    
        return node;
    }

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
            drawCompressedImage(g, root);
            g.dispose();
            ImageIO.write(compressedImg, "png", new File(outputPath));

            long endTime = System.nanoTime();
            long executionTime = (endTime - startTime) / 1_000_000; 

            long compressedSize = new File(outputPath).length();
            double compressionRatio = (1.0 - ((double) compressedSize / originalSize)) * 100;
            int depth = computeDepth(root);
            int nodeCount = countNodes(root);

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
