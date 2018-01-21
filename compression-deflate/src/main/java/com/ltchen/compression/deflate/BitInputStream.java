package com.ltchen.compression.deflate;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author : ltchen
 * @date : 2017/12/18
 * @desc : 读入比特流的工具类
 */
public class BitInputStream {

    /**
     * 基础输入流
     */
    private DataInputStream dis;

    /**
     * 已读入的字节数
     */
    private long count;

    /**
     * 构建一个比特输入流
     * @param is 输入流
     */
    public BitInputStream(InputStream is) {
        dis = new DataInputStream(is);
        count = 0;
    }

    /**
     * 获取已读取的字节数
     * @return 已读取的字节数
     */
    public long getCount() {
        return count;
    }

    /**
     * 将输入流中的数据读入到数组中
     * @param bytes 字节数组
     * @return 写入数组的字节数
     * @throws IOException
     */
    public int read(byte[] bytes) throws IOException {
        return read(bytes, 0 ,bytes.length);
    }

    /**
     * 将输入流中的数据读入到数组中
     * @param bytes 字节数组
     * @param off 写入数组的起始偏移
     * @param len 写入数组的个数
     * @return 写入数组的字节数
     * @throws IOException
     */
    public int read(byte[] bytes, int off, int len) throws IOException {
        int size = dis.read(bytes, off, len);
        if (size > 0) {
            count += size;
        }
        return size;
    }

    /**
     * 读取输入流中单个字节
     * @return 字节的值
     * @throws IOException
     */
    public int readByte() throws IOException {
        count++;
        return dis.readUnsignedByte();
    }

    /**
     * 读取输入流中两个字节
     * @return 两个字节的值
     * @throws IOException
     */
    public int readShort() throws IOException {
        return (readByte() | readByte() << 8);
    }

    /**
     * 读取输入流中四个字节
     * @return 四个字节的值
     * @throws IOException
     */
    public int readInt() throws IOException {
        return (readByte() | readByte() << 8 | readByte() << 16 | readByte() << 24);
    }

    /**
     * 读取输入流中四个字节
     * @return 四个字节的无符号值
     * @throws IOException
     */
    public long readUnsignedInt() throws IOException {
        return (readInt() & 0xffffffffL);
    }

    /**
     * 跳过输入流中接下来的 n 个字节
     * @param n 被跳过的字节数
     * @return 实际跳过的字节数
     * @throws IOException
     */
    public int skipBytes(int n) throws IOException {
        count += n;
        return dis.skipBytes(n);
    }

    /**
     * 一个比特队列
     */
    public int bitVal = 0;

    /**
     * 队列当前的位置
     */
    public int bitPos = 0;

    /**
     * 读取比特队列
     * @param n 读取的比特个数
     * @return 比特队列中 n 个比特的值
     * @throws IOException
     */
    public int readBits(int n) throws IOException {
        int val = 0;
        for (int m = 0; m < n; m++) {
            // 每 8 位读一个字节
            if (bitPos == 0) {
                bitVal = readByte();
            }
            // 将每个 bit 位拼接到正确的位置
            val |= ((bitVal >>> bitPos) & 1) << m;
            // 每 8 位将 bitPos 置 0
            bitPos = (bitPos + 1) & 7;
        }
        return val;
    }

    /**
     * 清空比特队列
     */
    public void clearBits() {
        bitVal = 0;
        bitPos = 0;
    }
}
