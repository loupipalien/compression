package com.ltchen.compression.deflate;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

/**
 * @author : ltchen
 * @date : 2017/12/21
 * @desc : 霍夫曼树
 */
public class HuffmanTree {

    /**
     * 码的总个数
     */
    private int codesCount;

    /**
     *
     */
    private Map<Integer,List<LeafNode>> depthMap;

    /**
     * 树的最大深度
     */
    private int maxDepth;

    /**
     * 树的根节点
     */
    private Node root;

    /**
     * 从给予频次数组 freqs 中构建一个深度小于 limit 的霍夫曼树
     * @param freqs 频次数组
     * @param limit 深度限制
     */
    public HuffmanTree(int[] freqs, int limit) {
        // 初始化
        codesCount = freqs.length;
        depthMap = new TreeMap<Integer,List<LeafNode>>();
        maxDepth = 0;

        // 初始化叶子节点
        PriorityQueue<Node> queue = new PriorityQueue<Node>();
        for (int i = 0; i < codesCount; i++) {
            if (freqs[i] > 0) {
                // i 作为值, freqs[i] 作为权重
                queue.add(new LeafNode(i, freqs[i]));
            }
        }

        // 确保树至少有两个叶子节点
        int index = 0；
        while (queue.size() < 2) {
            if (freqs[index] == 0) {
                queue.add(new LeafNode(index, 1));
            }
            index++;
        }


    }


    abstract class Node implements Comparable<Node> {
        /**
         * 此节点的父节点
         */
        public Node parent;

        /**
         * 是父节点的左孩子还是右孩子
         */
        public int side;

        /**
         * 此节点的权值
         */
        public int weight;

        /**
         * 根据权值比较节点
         * @param node 节点
         * @return 权值的差
         */
        @Override
        public int compareTo(Node node) {
            return this.weight -  node.weight;
        }
    }

    class InternalNode extends Node {

        /**
         * 左节点
         */
        public Node left;

        /**
         * 右节点
         */
        public Node right;

        /**
         * 构造内部节点
         * @param left 左孩子
         * @param right 右孩子
         */
        public InternalNode(Node left, Node right) {
            // 初始化左孩子
            left.parent = this;
            left.side = 0;
            this.left = left;
            // 初始右孩子
            left.parent = this;
            left.side = 1;
            this.right = right;
            // 此节点权重值为左孩子的权重值加右孩子权重值
            this.weight = left.weight + right.weight;
        }

        @Override
        public String toString() {
            return "InternalNode{" + "left=" + left + ", right=" + right + '}';
        }
    }

    class LeafNode extends Node {

        /**
         * 节点的值
         */
        public int value;

        /**
         * 构造叶子节点
         * @param value
         * @param weight
         */
        public LeafNode(int value, int weight) {
            this.value = value;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "LeafNode{" + "value=" + value + ", weight=" + weight + '}';
        }
    }
}
