package com.ltchen.compression.deflate;

/**
 * @author : ltchen
 * @date : 2017/12/24
 * @desc : LZ77 算法中 distance/length 对
 */
public class LZ77Pair {

    /**
     * distance/length 符号范围
     * 见 RFC 1951, 3.2.5 章节 (https://www.ietf.org/rfc/rfc1951.txt)
     */
    public static final int[] DIST_LOWS = new int[30];
    public static final int[] DIST_HIGHS = new int[30];
    public static final int[] DIST_BITS = new int[30];
    public static final int[] LEN_LOWS = new int[29];
    public static final int[] LEN_HIGHS = new int[29];
    public static final int[] LEN_BITS = new int[29];
    static {
        // 生成 distance 符号范围
        for (int i = 0; i < 4; i++) {
            DIST_LOWS[i] = i + 1;
            DIST_HIGHS[i] = DIST_LOWS[i];
            DIST_BITS[i] = 0;
        }
        for (int i = 4; i < 30; i++) {
            int j = (i - 4) % 2;
            int k = (i - 4) / 2;
            DIST_LOWS[i] = ((j + 2) << (k + 1)) + 1;
            DIST_HIGHS[i] = DIST_LOWS[i] + (1 << (k + 1)) - 1;
            DIST_BITS[i] = k + 1;
        }
        // 生成 length 符号范围
        for (int i = 0; i < 8; i++) {
            LEN_LOWS[i] = i + 3;
            LEN_HIGHS[i] = LEN_LOWS[i];
            LEN_BITS[i] = 0;
        }
        for (int i = 8; i < 28; i++) {
            // 8 - 27 每 4 位增加一个 bits 数, 即区间增大两倍
            int j = (i - 8) % 4;
            int k = (i - 8) / 4;
            LEN_LOWS[i] = ((j + 4) << (k + 1)) + 3;
            LEN_HIGHS[i] = LEN_LOWS[i] + (1 << (k + 1)) - 1;
            LEN_BITS[i] = k + 1;
        }
        LEN_HIGHS[27]--;
        LEN_LOWS[28] = 258;
        LEN_HIGHS[28] = 258;
        LEN_BITS[28] = 0;
    }
    /**
     * 此距离的值
     */
    public int dist;

    /**
     * 此距离的码
     */
    public int distCode;

    /**
     * 此距离所在区间最小值到此距离的增量
     */
    public int distInc;

    /**
     * 此距离所在区间的所需的比特数
     */
    public int distBits;

    /**
     * 此长度的值
     */
    public int len;

    /**
     * 此长度的码
     */
    public int lenCode;

    /**
     * 此长度所在区间到此长度的增量
     */
    public int lenInc;

    /**
     * 此长度所在区间的所需的比特数
     */
    public int lenBits;

    /**
     * 构造 LZ77Pair
     * @param dist 距离的值
     * @param len 长度的值
     */
    public LZ77Pair(int dist, int len) {
        this.dist = dist;
        distCode = -1;
        for (int i = 0; i < 30; i++){
            if (dist <= DIST_HIGHS[i]) {
                distCode = i;
                distInc = dist - DIST_LOWS[i];
                distBits = DIST_BITS[i];
                break;
            }
        }
        this.len = len;
        lenCode = -1;
        for (int i = 0; i < 29; i++) {
            if (len <= LEN_HIGHS[i]) {
                lenCode = 257 + i;
                lenInc = len - LEN_LOWS[i];
                lenBits = LEN_BITS[i];
                break;
            }
        }
    }

    /**
     * 测试 distance/length 构造
     */
    public static void main(String[] args) {
        System.out.println("Code\tBits\tLen");
        for (int i = 0; i < 29; i++) {
            if (LEN_LOWS[i] == LEN_HIGHS[i]) {
                System.out.println(String.format("%d\t%d\t%d", 257 + i, LEN_BITS[i], LEN_LOWS[i]));
            } else {
                System.out.println(String.format("%d\t%d\t%d - %d", 257 + i, LEN_BITS[i], LEN_LOWS[i], LEN_HIGHS[i]));
            }
        }
        System.out.println();
        System.out.println("Code\tBits\tDist");
        for (int i = 0; i < 30; i++) {
            if (DIST_LOWS[i] == DIST_HIGHS[i]) {
                System.out.println(String.format("%d\t%d\t%d", i, DIST_BITS[i], DIST_LOWS[i]));
            } else {
                System.out.println(String.format("%d\t%d\t%d - %d", i, DIST_BITS[i], DIST_LOWS[i], DIST_HIGHS[i]));
            }
        }
    }
}
