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
        this.children = new QuadTreeNode[4]; // 4 children for quad tree
    }
}
