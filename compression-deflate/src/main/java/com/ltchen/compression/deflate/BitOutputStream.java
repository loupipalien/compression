package com.ltchen.compression.deflate;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author : ltchen
 * @date : 2017/12/18
 * @desc : 写出比特流的工具类
 */
public class BitOutputStream {

    /**
     * 基础输出流
     */
    private DataOutputStream dos;

    /**
     * 已写出的字节数
     */
    private long count;

    /**
     * 构建一个比特输出流
     * @param os 输出流
     */
    public BitOutputStream(OutputStream os) {
        dos = new DataOutputStream(os);
        count = 0;
    }

    /**
     * 返回已写出的字节数
     * @return
     */
    public long getCount() {
        return count;
    }

    /**
     * 写出一个字节数组的数据
     * @param bytes 字节数组
     * @throws IOException
     */
    public void write(byte[] bytes) throws IOException {
        write(bytes, 0, bytes.length);
    }

    /**
     * 写出字节数组从下标 off 后 len 个字节的数据
     * @param bytes 字节数组
     * @param off 下标偏移
     * @param len 长度
     * @throws IOException
     */
    public void write(byte[] bytes, int off, int len) throws IOException {
        dos.write(bytes, off, len);
        count += len;
    }

    /**
     * 写出 byte, 一个字节
     * @param val 值
     * @throws IOException
     */
    public void writeByte(int val) throws IOException {
        dos.writeByte(val);
        count++;
    }

    /**
     * 写出 short, 两个字节
     * @param val 值
     * @throws IOException
     */
    public void writeShort(int val) throws IOException {
        writeByte(val);
        writeByte(val >> 8);
    }

    /**
     * 写出 int, 四个字节
     * @param val 值
     * @throws IOException
     */
    public void writeInt(int val) throws IOException {
        writeByte(val);
        writeByte(val >> 8);
        writeByte(val >> 16);
        writeByte(val >> 24);
    }

    /**
     * 写出无符号 int, 四个字节
     * @param val
     * @throws IOException
     */
    public void writeUnsignedInt(long val) throws IOException {
        writeByte((byte) (val));
        writeByte((byte) (val >> 8));
        writeByte((byte) (val >> 16));
        writeByte((byte) (val >> 24));
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
     * 写出比特队列
     * @param val 值
     * @param n 写出的比特数
     * @throws IOException
     */
    public void writeBits(int val, int n) throws IOException {
        for (int m = 0; m < n; m++) {
            // 将每个 bit 位拼接到正确的位置
            bitVal |= ((val >>> m) & 1) << bitPos;
            bitPos++;
            if (bitPos > 7) {
                // 每 8 位写一个字节
                writeByte(bitVal);
                bitVal = 0;
                bitPos = 0;
            }
        }
    }

    /**
     * 反向写出比特队列
     * @param val 值
     * @param n 写出的比特数
     * @throws IOException
     */
    public void writeBitsR(int val, int n) throws IOException {
        for (int m = n; m > 0; m--) {
            // 将每个 bit 位拼接到正确的位置
            bitVal |= ((val >>> m) & 1) << bitPos;
            bitPos++;
            if (bitPos > 7) {
                // 每 8 位写一个字节
                writeByte(bitVal);
                bitVal = 0;
                bitPos = 0;
            }
        }
    }

    /**
     * 刷出比特队列 (高位补 1)
     * @throws IOException
     */
    public void flushBits() throws IOException {
        if (bitPos > 0) {
            writeBits(0xff, 8 - bitPos);
        }
    }
}
