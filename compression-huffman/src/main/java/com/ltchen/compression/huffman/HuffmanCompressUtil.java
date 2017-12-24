package com.ltchen.compression.huffman;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author : ltchen
 * @date : 2017/12/04
 * @desc : 霍夫曼压缩工具类
 */
public class HuffmanCompressUtil {

    private HuffmanCompressUtil() {}

    /**
     * 将字节压缩为霍夫曼码
     * @param threeTuple ThreeTuple<需转换的字节数组,字节数组有效长度,上一次剩余的霍夫曼码字符串>
     * @param huffmanCodeMap 霍夫曼码和字节的映射
     * @return ThreeTuple<byte[],String>
     */
    public static TwoTuple<byte[],String> bytesToHuffmanCodes(ThreeTuple<byte[], Integer, String> threeTuple, Map<Byte, String> huffmanCodeMap) {
        // 一个字节长度
        int byteLen = 8;
        // 上一次剩余的霍夫曼码字符串
        StringBuilder sb = new StringBuilder(threeTuple.third);
        // 将字节转为霍夫曼码字符串
        for (int i = 0; i < threeTuple.second; i++) {
            sb.append(huffmanCodeMap.get(threeTuple.first[i]));
        }
        String huffmanCodes = sb.toString();
        // 将霍夫曼码字符串每 8 位转换为一个字节组成字节数组
        List<Byte> list = new ArrayList<Byte>();
        while (huffmanCodes.length() > byteLen) {
            String bits = huffmanCodes.substring(0, byteLen);
            list.add(ByteUtil.bitsToByte(bits));
            huffmanCodes = huffmanCodes.substring(byteLen);
        }
        // 返回字节数组和剩余的霍夫曼码字符串
        return new TwoTuple(ByteUtil.toArray(list), huffmanCodes);
    }

    /**
     * 将霍夫曼码解压为字节
     * @param threeTuple ThreeTuple<需转换的字节数组,字节数组有效长度,上一次剩余的霍夫曼码字符串>
     * @param huffmanCodeMap 霍夫曼码和字节的映射
     * @return TwoTuple<byte[],String>
     */
    public static TwoTuple<byte[],String> huffmanCodesToBytes(ThreeTuple<byte[],Integer,String> threeTuple, Map<String,Byte> huffmanCodeMap) {
        // 上一次剩余的霍夫曼码字符串
        StringBuilder sb = new StringBuilder(threeTuple.third);
        // 将字节转为霍夫曼码字符串
        for (int i = 0; i < threeTuple.second; i++) {
            sb.append(ByteUtil.byteToBits(threeTuple.first[i]));
        }
        String huffmanCodes = sb.toString();
        // 将霍夫曼码字符串转换为对应字节的字节数组
        List<Byte> list = new ArrayList<Byte>();
        boolean hasHuffmanCode = true;
        while (hasHuffmanCode) {
            hasHuffmanCode = false;
            // 将霍夫曼码字符串转换为字节
            for (String huffmanCode : huffmanCodeMap.keySet()) {
                if(huffmanCodes.startsWith(huffmanCode)) {
                    list.add(huffmanCodeMap.get(huffmanCode));
                    huffmanCodes = huffmanCodes.substring(huffmanCode.length());
                    hasHuffmanCode = true;
                    break;
                }
            }
        }
        // 返回字节数组和剩余的霍夫曼码字符串
        return new TwoTuple(ByteUtil.toArray(list), huffmanCodes);
    }

    /**
     * 二元组
     * @param <First> 第一个参数泛型
     * @param <Second> 第二个参数泛型
     */
    static class TwoTuple<First,Second> {
        public final First first;
        public final Second second;

        public TwoTuple(First first, Second second) {
            this.first = first;
            this.second = second;
        }

        public First getFirst() {
            return first;
        }

        public Second getSecond() {
            return second;
        }
    }

    /**
     * 三元组
     * @param <First> 第一个参数泛型
     * @param <Second> 第二个参数泛型
     * @param <Third> 第三个参数泛型
     */
    static class ThreeTuple<First,Second,Third> {
        public final First first;
        public final Second second;
        public final Third third;

        public ThreeTuple(First first, Second second, Third third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }
    }
}
