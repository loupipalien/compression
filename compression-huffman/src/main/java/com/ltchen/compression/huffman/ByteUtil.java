package com.ltchen.compression.huffman;

import java.util.List;

/**
 * @author : ltchen
 * @date : 2017/12/04
 * @desc : 字节工具类
 */
public class ByteUtil {

    private  ByteUtil(){}

    /**
     * 将字节转换位比特字符串
     * @param b 字节
     * @return String
     */
    public static String byteToBits(byte b) {
        String str = Integer.toBinaryString(((int)b | 256));
        int len = str.length();
        return str.substring(len - 8, len);
    }

    /**
     * 将比特字符串转换为字节
     * @param bits 比特字符串
     * @return byte
     */
    public static byte bitsToByte(String bits) {
        return (byte) Integer.parseInt(bits,2);
    }

    /**
     * 将 List<Byte> 转换为 byte[]
     * @param list Byte列表
     * @return byte[]
     */
    public static byte[] toArray(List<Byte> list) {
        byte[] bytes = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            bytes[i] = list.get(i);
        }
        return bytes;
    }

    public static void main(String[] args) {
        System.out.println(Integer.toBinaryString(Integer.MAX_VALUE));
        System.out.println(Integer.toBinaryString(Integer.MIN_VALUE));
    }
}
