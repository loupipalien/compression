package com.ltchen.compression.deflate;

import java.util.ArrayList;
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
        for (int i = 144; i < 255; i++) {
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

    public static List<Integer> packCodeLens(int[] litCodeLens, int[] distCodeLens) {
        List<Integer> lengths = new ArrayList<Integer>();
        return lengths;
    }

    private static void pack(List<Integer> lengths, int[] codeLens) {

    }

}
