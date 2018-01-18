package com.ltchen.compression.deflate;

import java.io.IOException;
import java.util.*;

/**
 * @author : ltchen
 * @date : 2018/1/17
 * @desc :
 */
public class Inflater {

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
    private static int WINDOW_SIZE = 256;

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

    private LZ77Window window;

    private List<Integer> litCodes;
    private Map<Integer, List<Integer>> litCodeMap;

    private List<Integer> distCodes;
    private Map<Integer, List<Integer>> distCodeMap;

    private List<Integer> clenCodes;
    private Map<Integer, List<Integer>> clenCodeMap;

    private int HLIT;
    private int HDIST;
    private int HCLEN;

    public long process() throws IOException {
        // 读取文件头标记
        int bFinal = in.readBits(1);
        int bType = in.readBits(2);
        // 处理压缩块数据块
        do {
            if (bType == 0) {
                // TODO
            } else if (bType == 1) {
                // TODO
            } else if (bType == 2) {
                // TODO
            } else {
                throw new AssertionError("无效的数据块类型!");
            }
        } while (bFinal == 0);
        // 返回处理字节数
        return out.getCount();
    }


    /**
     * 处理非压缩数据块
     * 见 RFC 1951, 3.2.4 章节 (https://www.ietf.org/rfc/rfc1951.txt)
     * @throws IOException
     */
    private void processUnCompressedBlock() throws IOException {
        // 读取压缩数据块头标记
        int len = in.readShort();
        int nLen = in.readShort();
        if (len != (nLen ^ 0xffff)) {
            throw new AssertionError("无效的非压缩数据块类型!");
        }
        // 读取数据
        byte[] bytes = new byte[len];
        in.read(bytes);
        // 更新循环冗余校验值
        crc.update(bytes);
        // TODO ???
        window.add(bytes);
        // 写出解压缩数据
        out.write(bytes);
    }

    private void processHuffmanCompressedBlock() throws IOException {
        while (true) {
            int litCode = readCode(litCodes, litCodeMap);

            // 转换码后写出
            if (litCode < END_OF_BLOCK) {
                // 直接将字符码转化为字符
                byte b = (byte) litCode;
                crc.update(b);
                // TODO ???
                window.add(b);
                out.writeByte(b);
            } else if (litCode == END_OF_BLOCK) {
                // 数据块结束标记
                break;
            } else {
                // 计算长度码并转换为长度值
                int lenCode = litCode - 257;
                int len = LZ77Pair.LEN_LOWS[lenCode] + in.readBits(LZ77Pair.LEN_EXTRA_BITS[lenCode]);
                // 因为是长度, 后必是一个距离; 计算距离码并转换为距离值
                int distCode = readCode(distCodes, distCodeMap);
                int dist = LZ77Pair.DIST_LOWS[distCode] + in.readBits(LZ77Pair.DIST_EXTRA_BITS[distCode]);
                // 从窗口中获取
                byte[] bytes = window.getBytes(dist, len);
                crc.update(bytes);
                // TODO ???
                window.add(bytes);
                out.write(bytes);
            }
        }
    }

    private void readCodes() throws IOException {
        // 读入 litCodes 的可变个数, 并计算 HLIT
        HLIT = 257 + in.readBits(5);
        // 读入 distCodes 的可变个数, 并计算 HDIST
        HDIST = 1 + in.readBits(5);
        // 读入 clenCodes 的可变个数, 并计算 HCLEN
        HCLEN = 4 + in.readBits(4);

        // 读入 clenCodeLens
        int[] clenCodeLens = new int[CODE_LENGTH_COUNT];
        for (int i = 0; i < HCLEN; i++) {
            clenCodeLens[CODE_LENGTH_ORDER[i]] = in.readBits(3);
        }
        // 构建 clenCodes
        clenCodes = buildCodes(clenCodeLens);
        clenCodeMap = buildCodeMap(clenCodes, clenCodeLens);

        // 解压 literal/distance 的码的长度序列
        int[] lengths = new int[HLIT + HDIST];
        for (int i = 0; i < lengths.length; i++) {
            int code = readCode(clenCodes, clenCodeMap);
            if (code < 16) {
                lengths[i] = code;
            } else {
                int n = 0;
                if (code == 16) {
                    // 16 标识前一个 code 有重复 3 - 6 次
                    n = 3 + in.readBits(2);
                }
                if (code == 17) {
                    // 17 标识 code = 0 重复 3 - 10 次
                    n = 3 + in.readBits(3);
                }
                if (code == 18)  {
                    // 18 标识 code = 0 重复 11 - 138 次
                    n = 11 + in.readBits(7);
                }
                for (int j = 0; j < n; j++) {
                    lengths[i + j] = lengths[i - 1];
                }
                // 向后滑动
                i += (n - 1);
            }
        }

        // 获取 litCodeLens
        int[] litCodeLens = new int[LITERAL_COUNT];
        System.arraycopy(lengths, 0, litCodeLens, 0, HLIT);
        // 构建 litCodes
        litCodes = buildCodes(litCodeLens);
        litCodeMap = buildCodeMap(litCodes, litCodeLens);

        // 获取 distCodeLens
        int[] distCodeLens = new int[DISTANCE_COUNT];
        System.arraycopy(lengths, HLIT, distCodeLens, 0, HDIST);
        // 构建 distCodes
        distCodes = buildCodes(distCodeLens);
        distCodeMap = buildCodeMap(distCodes, distCodeLens);
    }

    /**
     * 利用码长度构建码
     * @param codeLens 码长度的序列
     * @return
     */
    private List<Integer> buildCodes(int[] codeLens) {
        int n = codeLens.length;
        Integer[] codes = new Integer[n];
        // 找出被使用的码的长度
        Set<Integer> lengths = new TreeSet<>();
        for (int i = 0; i < n; i++) {
            if (codeLens[i] > 0) {
                lengths.add(codeLens[i]);
            }
        }
        // 构建码
        int nextCode = 0;
        int lastShift = 0;
        for (Integer length : lengths) {
            nextCode <<= (length - lastShift);
            lastShift = length;
            // 转换为码
            for (int i = 0; i < n; i++) {
                if (codeLens[i] == length) {
                    codes[i] = nextCode++;
                }
            }
        }
        return Arrays.asList(codes);
    }

    /**
     * 利用码和码的长度构建码长度与码序列的映射
     * @param codes 码序列
     * @param codeLens 码长度
     * @return
     */
    private Map<Integer,List<Integer>> buildCodeMap(List<Integer> codes, int[] codeLens) {
        int n = codeLens.length;
        Map<Integer,List<Integer>> codeMap = new TreeMap<>();
        // 构建 codeMap
        for (int i = 0; i < n; i++) {
            int length = codeLens[i];
            if (length > 0) {
                List<Integer> codeList = codeMap.get(length);
                if (codeList == null) {
                    codeList = new ArrayList<>();
                    codeMap.put(codeLens[i], codeList);
                }
                codeList.add(codes.get(i));
            }
        }
        return codeMap;
    }

    private int readCode(List<Integer> lenCodes, Map<Integer, List<Integer>> lenCodeMap) throws IOException {
        int code = 0;
        int codeLen = 0;
        int index = -1;

        do {
            // 因为树最大高度限制为 15, 所以 codeLen 的最大值为 14
            if (codeLen >= 15) {
                throw new AssertionError("找不到对应的码");
            }
            // 读取比特转化为
            code <<= 1;
            code |= in.readBits(1);
            codeLen++;
            // 检查是否匹配到码
            List<Integer> codeList = lenCodeMap.get(codeLen);
            if (codeList != null) {
                index = codeList.indexOf(code);
            }
        } while (index != -1);
        // 此码的下标即为经过游程编码后的值
        return lenCodes.indexOf(code);
    }


    public static void main(String[] args) {
        int i = 10;
        System.out.println(i);
        System.out.println(Integer.toBinaryString(i));
        System.out.println(~i);
        System.out.println(Integer.toBinaryString(~i));
        System.out.println(i ^ 0xffff);
        System.out.println(Integer.toBinaryString(i ^ 0xffff));
        System.out.println(i + ~i);
        System.out.println(Integer.toBinaryString(i + ~i));
    }
}
