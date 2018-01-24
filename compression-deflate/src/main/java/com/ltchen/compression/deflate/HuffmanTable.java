package com.ltchen.compression.deflate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author : ltchen
 * @date : 2017/12/10
 * @desc : 霍夫曼树
 */
public class HuffmanTable {

    /**
     * 默认的霍夫曼码表
     * 见 RFC 1951, 3.2.6 章节 (https://www.ietf.org/rfc/rfc1951.txt)
     */
    public static final HuffmanTable LIT;
    public static final HuffmanTable DIST;
    static {
        // 生成固定的字母/长度码
        LIT = new HuffmanTable(286);
        int nextCode = 0;
        for (int i = 256; i <= 279; i++) {
            LIT.codes[i] = nextCode++;
            LIT.codeLens[i] = 7;
        }
        // 0010111 -> 00110000
        nextCode <<= 1;
        for (int i = 0; i <= 143; i++) {
            LIT.codes[i] = nextCode++;
            LIT.codeLens[i] = 8;
        }
        // 286,287 不会被用到
        for (int i = 280; i <= 285; i++) {
            LIT.codes[i] = nextCode++;
            LIT.codeLens[i] = 9;
        }
        // 11000101 -> 110010000
        nextCode = (nextCode + 2) << 1;
        for (int i = 144; i <= 255; i++) {
            LIT.codes[i] = nextCode++;
            LIT.codeLens[i] = 9;
        }

        // 生成固定的距离码
        DIST = new HuffmanTable(30);
        for (int i = 0; i <= 29 ; i++) {
            DIST.codes[i] = i;
            DIST.codeLens[i] = 5;
        }
    }

    /**
     * 码的数组
     */
    public int[] codes;

    /**
     * 码长度的数组
     */
    public int[] codeLens;

    /**
     * 构造霍夫曼表
     * @param codeCount 码的总数
     */
    public HuffmanTable(int codeCount) {
        codes = new int[codeCount];
        codeLens = new int[codeCount];
    }

    /**
     *
     * @param litCodeLens literal 生成的霍夫曼码表中所有码长度序列
     * @param distCodeLens distance 生成的霍夫曼码表中所有码长度序列
     * @return
     */
    public static List<Integer> packCodeLengths(int[] litCodeLens, int[] distCodeLens) {
        List<Integer> lengths = new ArrayList<>();
        // 压缩 literal 生成的霍夫曼码表中所有码长度序列
        pack(lengths, litCodeLens);
        // 压缩 distance 生成的霍夫曼码表中所有码长度序列
        pack(lengths, distCodeLens);
        return lengths;
    }

    /**
     * 压缩码长度的序列
     * 见 RFC 1951, 3.2.7 章节 (https://www.ietf.org/rfc/rfc1951.txt)
     * @param lengths  存放压缩后的码长度序列
     * @param codeLens 要压缩的码长度序列
     */
    private static void pack(List<Integer> lengths, int[] codeLens) {
        int n = codeLens.length;

        /*
         * 游程编码
         */
        // 游程重复的长度
        int runLen = 1;
        // 获取第一个码字的长度
        int last = codeLens[0];
        for (int i = 1; i <= n; i++) {
            if (i < n && last == codeLens[i]) {
                runLen++;
            } else {
                // 写入码的长度值
                lengths.add(last);
                // 减去 last 重复的 1 次
                runLen--;
                if (last == 0) {
                    // 对应的码没有被使用时
                    int j = 138;
                    // 0 重复 11 - 138 次时的编码
                    while (j >= 11) {
                        if ((runLen - j) >= 0) {
                            // 填入标记
                            lengths.add(18);
                            // 填入重复次数
                            lengths.add(j - 11);
                            runLen -= j;
                        } else {
                            j--;
                        }
                    }
                    // 0 重复 3 - 10 次时的编码
                    while (j >= 3) {
                        if ((runLen - j) >= 0) {
                            lengths.add(17);
                            lengths.add(j - 3);
                            runLen -= j;
                        } else {
                            j--;
                        }
                    }
                } else {
                    // 对应的码被使用时
                    int j = 6;
                    // 重复 3 - 6 次时, 少于 3 次编码无助于压缩 (不包括 last)
                    while (j >= 3) {
                        if((runLen - j) >= 0) {
                            lengths.add(16);
                            lengths.add(j - 3);
                            runLen -= j;
                        } else {
                            j--;
                        }
                    }
                }
                // 当码长重复次数少于 3 次 (不包括 last)
                while (runLen > 0) {
                    lengths.add(last);
                    runLen--;
                }
                // 获取下一个长度值, 继续向后
                if (i < n) {
                    last = codeLens[i];
                    runLen = 1;
                }
            }
        }
    }

    @Override
    public String toString() {
        return "HuffmanTable{" +
                "codes=" + Arrays.toString(codes) +
                ", codeLens=" + Arrays.toString(codeLens) +
                '}';
    }

    public static void main(String[] args) {
        System.out.println(LIT);
        System.out.println(DIST);
    }
}
