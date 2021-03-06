package com.ltchen.compression.deflate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @author : ltchen
 * @date : 2018/1/14
 * @desc :
 */
public class Deflater {

    /**
     * 见 RFC 1951, 3.2.7 章节 (https://www.ietf.org/rfc/rfc1951.txt)
     * +-BFINAL-+-BTYPE-+-HLIT-+-HDIST-+-HCLEN-+-/CLL (HCLEN + 4)/-+-/CL1 (HLIT + 257)/-+-/CL2 (HDIST + 1)/-+-/LIT 编码流 + DIST 编码流/-+
     * |    *   |  **   |***** | ***** | ****  |***|    ...    |***|     ***...***      |     ***...***     |        ***...***         |
     * +--------+-------+------+-------+-------+-------------------+--------------------+-------------------+--------------------------+
     * 注: * 表示一个比特
     */

    /**
     * 是否开启 debug
     */
    private static boolean DEBUG = false;

    /**
     * 数据集最后块的标记符
     */
    private final static int BFINAL = 1;
    /**
     * 数据集非最后块的标记符
     */
    private final static int NON_BFINAL = 0;

    /**
     * 压缩模式: 00-无压缩, 01-固定霍夫曼码, 10-动态霍夫曼码, 11-保留 (错误)
     */
    private static int BTYPE = 2;
    /**
     * 是否开启 LZ77 压缩
     */
    private static boolean ENABLE_LZ77 = true;

    /**
     * 缓冲区大小
     */
    private static int BUFFER_SIZE = 32768;
    /**
     * 窗口大小
     */
    private static int WINDOW_SIZE = 32768;

    /**
     * 终止标记字符
     */
    private static final int END_OF_BLOCK = 256;

    /**
     * 码长度的频次排序 (会再使用霍夫曼编码压缩, PK 认为树最多有 7 层, 所以使用 3 比特表示即可)
     * 见 RFC 1951, 3.2.7 章节 (https://www.ietf.org/rfc/rfc1951.txt)
     */
    private static final int[] CODE_LENGTH_ORDER = {16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15};

    private static final int LITERAL_COUNT = 286;
    private static final int DISTANCE_COUNT = 30;
    private static final int CODE_LENGTH_COUNT = 19;

    /**
     * 比特输入流
     */
    private BitInputStream in;
    /**
     * 比特输出流
     */
    private BitOutputStream out;
    /**
     *  循环冗余校验
     */
    private CRC crc;
    /**
     * 处理块超过字节边界的比特数
     */
    private int remainBits;

    private DeflateCompressor dc;

    public Deflater(DeflateCompressor dc, BitInputStream in, BitOutputStream out) {
        this.dc = dc;
        this.in = in;
        this.out = out;
        crc = new CRC();
        remainBits = 0;
    }

    public long process() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitOutputStream bos = new BitOutputStream(baos);

        // 创建缓冲区和滑动窗口
        byte[] buffer = new byte[BUFFER_SIZE];
        LZ77Window window = new LZ77Window(WINDOW_SIZE);

        // 最大为 BUFFER_SIZE, 即 2^16
        int len;
        while ((len = in.read(buffer, 0, BUFFER_SIZE)) > 0) {
            // 显示进度
            dc.updateProgress(in.getCount());

            // 将上一次处理的块写出
            if (bos.getCount() > 0) {
                // 写出数据块
                writeBlock(NON_BFINAL, baos.toByteArray());
                // 重置
                baos.reset();
            }

            // 更新冗余循环校验
            crc.update(buffer, 0, len);

            /*
             * 无压缩写出
             * 见 RFC 1951, 3.2.4 章节 (https://www.ietf.org/rfc/rfc1951.txt)
             */
            if (BTYPE == 0) {
                bos.writeShort(len);
                bos.writeShort(len ^ 0xffff);
                bos.write(buffer, 0, len);
                // TODO 不懂这里窗口是用于做什么 ???
                window.add(buffer, 0 , len);
                remainBits = 0;
                continue;
            }

            // 初始化
            LZ77Pair[] pairs = new LZ77Pair[len];
            int[] litFreq = new int[LITERAL_COUNT];
            int[] distFreq = new int[DISTANCE_COUNT];
            int[] clenFreq = new int[CODE_LENGTH_COUNT];

            // 查询匹配串, 并统计 literal, distance 的频次
            for (int i = 0; i < len; i++) {
                LZ77Pair pair = null;
                if (ENABLE_LZ77) {
                    // 在滑动窗口中匹配
                    pair = window.find(buffer, i, len);
                }
                if (pair != null) {
                    // 在 buffer 的第 i 个字节匹配到的
                    pairs[i] = pair;
                    // 将匹配到的字节添加到滑动窗口中
                    window.add(buffer, i, pair.len);
                    // 向后移动已匹配的字节数
                    i += (pair.len - 1);
                    // 更新距离码的频次
                    distFreq[pair.distCode]++;
                    // 更新长度的频次 (literal 和 length 使用一个码表)
                    litFreq[pair.lenCode]++;
                } else {
                    // 将字节加入窗口
                    window.add(buffer[i]);
                    // 更新字符频次
                    litFreq[buffer[i] & 0xff]++;
                }
            }
            // 添加块结束标记符
            litFreq[END_OF_BLOCK]++;

            // 生成霍夫曼码
            int[] litCodes, litCodeLens, distCodes, distCodeLens, clenCodes, clenCodeLens;
            List<Integer> clens;
            // 树限制深度为 15, 不懂 PK 为何这样设计 (但大神总有大神的理由...膜拜中)
            int treeLimitDepth = 15;
            if (BTYPE == 2) {
                // 生成 literal 码
                HuffmanTree litTree = new HuffmanTree(litFreq, treeLimitDepth);
                HuffmanTable litTable = litTree.getTable();
                litCodes = litTable.codes;
                litCodeLens = litTable.codeLens;

                // 生成 distance 码
                HuffmanTree distTree = new HuffmanTree(distFreq, treeLimitDepth);
                HuffmanTable distTable = distTree.getTable();
                distCodes = distTable.codes;
                distCodeLens = distTable.codeLens;

                // 将码长度打包
                clens = HuffmanTable.packCodeLengths(litCodeLens, distCodeLens);

                // 统计码长度的频次
                Iterator<Integer> iterator = clens.iterator();
                while (iterator.hasNext()) {
                    int clen = iterator.next();
                    clenFreq[clen]++;
                    // 跳过游程编码标识
                    if (clen == 16 || clen == 17| clen == 18) {
                        iterator.next();
                    }
                }

                // 生成 codeLength 码
                HuffmanTree clenTree = new HuffmanTree(clenFreq, 7);
                HuffmanTable clenTable = clenTree.getTable();
                clenCodes = clenTable.codes;
                clenCodeLens = clenTable.codeLens;
            } else {
                litCodes = HuffmanTable.LIT.codes;
                litCodeLens = HuffmanTable.LIT.codeLens;

                distCodes = HuffmanTable.DIST.codes;
                distCodeLens = HuffmanTable.DIST.codeLens;

                clens = null;
                clenCodes = null;
                clenCodeLens = null;
            }

            // debug 时打印 litCodes, distCodes, clenCodes
            if (DEBUG) {
                System.out.println("literal codes");
                printCodes(LITERAL_COUNT, litCodes, litCodeLens);
                System.out.println("distance codes");
                printCodes(DISTANCE_COUNT, distCodes, distCodeLens);
                System.out.println("code length codes");
                printCodes(CODE_LENGTH_COUNT, clenCodes, clenCodeLens);
            }

            // 压缩数据
            if (BTYPE == 2) {
                /*
                 * 见 RFC 1951, 3.2.7 章节 (https://www.ietf.org/rfc/rfc1951.txt)
                 */
                // 写出 litCodes 的可变个数
                bos.writeBits(LITERAL_COUNT - 257, 5);
                // 写出 distCodes 的可变个数
                bos.writeBits(DISTANCE_COUNT - 1, 5);
                // 写出 clenCodes 的可变个数
                bos.writeBits(CODE_LENGTH_COUNT - 4, 4);
                // 写出 clenCodeLens
                for (int i = 0; i < CODE_LENGTH_COUNT; i++) {
                    bos.writeBits(clenCodeLens[CODE_LENGTH_ORDER[i]], 3);
                }
                // 写出 litCodeLens 和 distCodeLens 经过游程编码后的序列
                Iterator<Integer> iterator = clens.iterator();
                while (iterator.hasNext()) {
                    int clen = iterator.next();
                    bos.writeBitsR(clenCodes[clen], clenCodeLens[clen]);
                    if (clen == 16) {
                        bos.writeBits(iterator.next(), 2);
                    }
                    if (clen == 17) {
                        bos.writeBits(iterator.next(), 3);
                    }
                    if (clen == 18) {
                        bos.writeBits(iterator.next(), 7);
                    }
                }
            }

            // 写出压缩数据 (literal codes 和 distance codes)
            for (int i = 0; i < len; i++) {
                LZ77Pair pair = pairs[i];
                if (pair != null) {
                    // 写出长度
                    int lenCode = pair.lenCode;
                    bos.writeBitsR(litCodes[lenCode], litCodeLens[lenCode]);
                    bos.writeBits(pair.lenExtra, pair.lenExtraBits);
                    // 写出距离
                    int distCode = pair.distCode;
                    bos.writeBitsR(distCodes[distCode], distCodeLens[distCode]);
                    bos.writeBits(pair.distExtra, pair.distExtraBits);
                    // 向后滑动
                    i += (pair.len - 1);
                } else {
                    // 为匹配的字节, 无符号写出
                    int litCode = buffer[i] & 0xff;
                    bos.writeBitsR(litCodes[litCode], litCodeLens[litCode]);
                }
            }

            // 写出块结束标记符
            bos.writeBitsR(litCodes[END_OF_BLOCK], litCodeLens[END_OF_BLOCK]);
            remainBits = bos.bitPos;
            bos.flushBits();
        }

        // 写出最后一个块
        writeBlock(BFINAL, baos.toByteArray());
        // 刷出
        out.flushBits();
        // 返回一共写出多少字节
        return out.getCount();
    }


    /**
     * 将压缩数据块写出
     * @param block 压缩数据块
     * @throws IOException
     */
    private void writeBlock(int bFinal, byte[] block) throws IOException {
        // 写出最后块的标记符
        out.writeBits(bFinal, 1);
        // 写出块编码类型
        out.writeBits(BTYPE, 2);
        // TODO ???
        if (BTYPE == 0) {
            out.flushBits();
        }
        // 检查字节边界是否对齐, 即 BitOutPutStream 中 bitPos 为 0 且 remainBits 为 0
        if (out.bitPos == 0 && remainBits == 0) {
            out.write(block);
        } else {
            for (int i = 0; i < block.length; i++) {
                if (i == block.length - 1 && remainBits > 0) {
                    // 只写出最后一个字节的低 remainBits 个字符, 因为前 8 - remainBits 是补的 1
                    out.writeBits(block[i], remainBits);
                } else {
                    // 每次写出一个字节
                    out.writeBits(block[i], 8);
                }
            }
        }
    }

    /**
     * 打印码
     * @param count 打印总数
     * @param codes 码序列
     * @param codeLens 码长度序列
     */
    private void printCodes(int count, int[] codes, int[]codeLens) {
        for (int i = 0; i < count; i++) {
            if (codeLens[i] > 0) {
                String code = String.format("%" + codeLens[i] + "s", Integer.toBinaryString(codes[i]));
                code = code.replace(' ', '0');
                System.out.println(i + "\t" + code);
            }
        }
    }

    /**
     * 获取 CRC 校验值
     * @return
     */
    public int getCRCValue() {
        return crc.getValue();
    }

    public static void main(String[] args) {
        System.out.println(Integer.toBinaryString(5));
        System.out.println(String.format("%10s", Integer.toBinaryString(127)));
    }
}
