package com.ltchen.compression.deflate;

import java.util.*;

/**
 * @author : ltchen
 * @date : 2017/12/21
 * @desc : 霍夫曼树
 */
public class HuffmanTree {

    /**
     * 码的总数
     */
    private int codeCount;

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
        codeCount = freqs.length;
        depthMap = new TreeMap<Integer,List<LeafNode>>();
        maxDepth = 0;

        // 初始化叶子节点
        PriorityQueue<Node> queue = new PriorityQueue<Node>();
        for (int i = 0; i < codeCount; i++) {
            if (freqs[i] > 0) {
                // i 作为值, freqs[i] 作为权重
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
            queue.add(new InternalNode(left, right));
        }
        root = queue.remove();

        // 构造 depthMap
        traverse(root);

        // 平衡霍夫曼树到限制深度
        while (maxDepth > limit) {
            // 在最深的一层获取一个叶子节点(最后一层必然有偶数个叶子节点)
            LeafNode leafA = depthMap.get(maxDepth).get(0);
            // 获取 leafA 的父节点
            InternalNode parentOne = (InternalNode) leafA.parent;
            LeafNode leafB;
            if (leafA.side == 0) {
                leafB = (LeafNode) parentOne.right;
            } else {
                leafB = (LeafNode) parentOne.left;
            }
            // 使 leafB 代替 parentOne 节点
            InternalNode parentTwo = (InternalNode) parentOne.parent;
            if (parentOne.side == 0) {
                parentTwo.left = leafB;
                parentTwo.left.parent = parentTwo;
                parentTwo.left.side = 0;
            } else {
                parentTwo.right = leafB;
                parentTwo.right.parent = parentTwo;
                parentTwo.side = 1;
            }
            // 平衡一次霍夫曼树
            boolean moved = false;
            for (int i = maxDepth - 2; i >= 1; i--) {
                List<LeafNode> leaves = depthMap.get(i);
                if (Objects.nonNull(leaves)) {
                    // 在倒数第二层获取一个叶子节点
                    LeafNode leafC = leaves.get(0);
                    // 将之前的 leafA 节点和 leafC 组成子树代替 leafC 的节点
                    InternalNode parentThree = (InternalNode) leafC.parent;
                    if (leafC.side == 0) {
                        parentThree.left = new InternalNode(leafA, leafC);
                        parentThree.left.parent = parentThree;
                        parentThree.left.side = 0;
                    } else {
                        parentThree.right = new InternalNode(leafA, leafC);
                        parentThree.right.parent = parentThree;
                        parentThree.right.side = 1;
                    }
                    moved = true;
                    break;
                }
            }
            // 检查是否有重新移动叶子节点
            if (!moved) {
                throw new AssertionError("霍夫曼树已不能被再平衡");
            }
            // 重新遍历
            traverse(root);
        }
    }

    /**
     * 从根节点遍历霍夫曼树
     * @param root 根节点
     */
    private void traverse(Node root) {
        depthMap.clear();
        maxDepth = 0;
        traverse(root, 0);
    }

    /**
     * 从当前节点遍历霍夫曼树
     * @param node 节点
     * @param depth 深度
     */
    private void traverse(Node node, int depth) {
        // 设置霍夫曼树的最大深度
        maxDepth = depth > maxDepth ? depth : maxDepth;
        // 递归遍历整个树, 并将叶子节点加入到 depthMap 中
        if (node instanceof InternalNode) {
            traverse(((InternalNode) node).left, depth + 1);
            traverse(((InternalNode) node).right, depth + 1);
        } else {
            if (Objects.isNull(depthMap.get(depth))) {
                depthMap.put(depth, new ArrayList<LeafNode>());
            }
            depthMap.get(depth).add((LeafNode) node);
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
