package com.ltchen.compression.huffman;

import java.util.*;

/**
 * @author : ltchen
 * @date : 2017/12/13
 * @desc : 霍夫曼树
 */
public class HuffmanTree {

    /**
     * 树的根节点
     */
    private Node root;

    /**
     * 从给予的字符频次数组构造霍夫曼树 (约定左子树的权值小于右子树)
     * @param freqs 频次数组(index 代表字符值, freqs[index] 代表出现的频次)
     */
    public HuffmanTree(int[] freqs) {
        // 初始化叶子节点
        PriorityQueue<Node> queue = new PriorityQueue<Node>();
        for (int i = 0; i < freqs.length; i++) {
            // 过滤未出现的字符
            if (freqs[i] > 0) {
                // index 作为叶子节点的 value, 频次作为叶子节点的权重
                queue.add(new LeafNode(i, freqs[i]));
            }
        }
        // 确保树至少有两个叶子节点
        for (int i = 0; queue.size() < 2; i++) {
            if (freqs[i] == 0) {
                queue.add(new LeafNode(i, 1));
            }
        }
        // 初始化树
        while (queue.size() > 1) {
            Node left = queue.remove();
            Node right = queue.remove();
            // 将新的子树加入队列继续构造霍夫曼树
            queue.add(new InternalNode(left, right));
        }
        // 树的根节点赋值
        root = queue.remove();
    }

    @Override
    public String toString() {
        return root.toString();
    }

    public static void main(String[] args) {
        int[] freqs = new int[]{1,2,3,4,5,6,7,8,9};
        HuffmanTree tree = new HuffmanTree(freqs);
        System.out.println(tree);
        System.out.println(tree.getHuffmanCodes());
    }

    /**
     * 获取此霍夫曼树的霍夫曼码: Map<霍夫曼码,字符值>
     * @return Map<String,Integer>
     */
    public Map<String,Integer> getHuffmanCodes() {
        Map<String,Integer> codes = new TreeMap<String,Integer>();
        generateHuffmanCode(root, "", codes);
        return codes;
    }

    /**
     * 生成当前节点的霍夫曼码
     * @param node 当前节点
     * @param preCode 当前节点的前缀码
     * @param huffmanCodes 树的霍夫曼码
     */
    private void generateHuffmanCode(Node node, String preCode, Map<String,Integer> huffmanCodes) {
        if (Objects.nonNull(node)) {
            // 如果不是根节点, 当前节点编码为前缀码加当前节点的 side
            String code = node.equals(root) ? preCode : preCode + node.side;
            if (node instanceof LeafNode) {
                huffmanCodes.put(code, ((LeafNode) node).value);
            } else {
                // 内部节点则递归
                generateHuffmanCode(((InternalNode)node).left, code, huffmanCodes);
                generateHuffmanCode(((InternalNode)node).right, code, huffmanCodes);
            }
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
