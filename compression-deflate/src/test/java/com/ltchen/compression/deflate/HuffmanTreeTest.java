package com.ltchen.compression.deflate;

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * @author : ltchen
 * @date : 2018/1/21
 * @desc :
 */
public class HuffmanTreeTest {

    @Test
    public void testBalanceHuffmanTree() {
        // 斐波那契数列生成霍夫曼树, 如果进行重平衡会产生深度最深的霍夫曼树
        int n = 21;
        int[] fib = new int[n];
        fib[0] = 1;
        fib[1] = 1;
        for (int i = 2; i < fib.length; i++) {
            fib[i] = fib[i-1] + fib[i-2];
        }

        try {
            HuffmanTree tree = new HuffmanTree(fib, 15);

            // 获取霍夫曼表
            HuffmanTable table = tree.getTable();
            int[] litCode = table.codes;
            int[] litCodeLen = table.codeLens;

            // 打印
            for (int i = 0; i < n; i++) {
                if (litCodeLen[i] > 0) {
                    String code = String.format("%s", Integer.toBinaryString(litCode[i]));
                    System.out.println(i + "\t" + code);
                }
            }
        } catch (AssertionError e) {
            // 平衡失败
            fail();
        }
    }
}
