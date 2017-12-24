package com.ltchen.compression.huffman;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author : ltchen
 * @date : 2017/12/02
 * @desc :
 */
public class ByteFreqCounter {

    /**
     * 字符统计: 因为每次读出一个字节, 8 个比特最多组成 256 个不同的字符
     */
    private static final int BYTE_NUMBER = 256;
    private static final int[] byteFreqs = new int[BYTE_NUMBER];
    private static final int BUFFER_SIZE = 1024;
    private static final byte[] buffer = new byte[BUFFER_SIZE];

    public ByteFreqCounter(InputStream is) throws IOException {
        // 记录读入 buffer 的字节个数
        int number;
        DataInputStream dis = new DataInputStream(is);
        while ((number = dis.read(buffer)) != -1) {
            for (int i = 0; i < number; i++) {
                // 统计字节出现的频次(byte 转换为无符号 byte)
                byteFreqs[buffer[i] & 0xFF] += 1;
            }
        }
    }

    /**
     * 返回字节统计
     * @return
     */
    public int[] getByteFreqs() {
        return byteFreqs;
    }
}
