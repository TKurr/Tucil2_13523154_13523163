package com;

import java.awt.Color;
import java.awt.Rectangle;

public class QuadTreeNode {
    Rectangle region;
    Color averageColor;
    boolean isLeaf;
    QuadTreeNode[] children;

    public QuadTreeNode(Rectangle region, Color color, boolean leaf) {
        this.region = region;
        this.averageColor = color;
        this.isLeaf = leaf;
        this.children = new QuadTreeNode[4];
    }

    public static int countDepths(QuadTreeNode node) {
        if (node == null || node.isLeaf) return 0;
        int maxDepth = 0;
        for (QuadTreeNode child : node.children) {
            maxDepth = Math.max(maxDepth, countDepths(child));
        }
        return maxDepth + 1;
    }

    public static int countNodes(QuadTreeNode node) {
        if (node == null) return 0;
        int count = 1;
        for (QuadTreeNode child : node.children) {
            count += countNodes(child);
        }
        return count;
    }
}
