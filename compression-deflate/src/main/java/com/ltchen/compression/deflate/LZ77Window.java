package com.ltchen.compression.deflate;

/**
 * @author : ltchen
 * @date : 2017/12/20
 * @desc : LZ77 算法中的滑动窗口字典
 */
public class LZ77Window {

    /**
     * 最小匹配长度 (LZ77 算法认为小于 3 的字符匹配长度对压缩无增益)
     */
    public static final int MIN_MATCH = 3;

    /**
     * 最大匹配长度 (LZ77 压缩基于字节作为基本字符编码, 即最多不超过 256 个)
     */
    public static final int MAX_MATCH = 258;

    /**
     * 窗口最大大小
     */
    private int maxSize;

    /**
     * 窗口字典, 用于存储字节
     */
    private byte[] dict;

    /**
     * 窗口字典当前已存放的字节数
     */
    private int size;

    /**
     * 窗口字典中当前要放置字节位置
     */
    private int pos;

    /**
     * 窗口当前字节位置的掩码, 为了是 pos 到达 maxSize 是置零
     */
    private int mask;


    /**
     * 构造 LZ77Window
     * @param size 窗口大小
     */
    public LZ77Window(int size) {
        // 使用 int 值比特队列中 1 的个数判断
        if (Integer.bitCount(size) != 1) {
            throw new AssertionError("窗口大小必须为 2 次方");
        }
        maxSize = size;
        mask = maxSize - 1;
        dict = new byte[maxSize];
        pos = 0;
        this.size = size;
    }

    /**
     * 添加一个字节到滑动窗口
     * @param b 被添加的字节
     */
    public void add(byte b) {
        dict[pos] = b;
        pos = (pos + 1) & mask;
        if (size < maxSize) {
            size++;
        }
    }

    /**
     * 添加一个字节数组到滑动窗口
     * @param bytes 被添加的字节数组
     */
    public void add(byte[] bytes) {
        add(bytes, 0, bytes.length);
    }

    /**
     * 添加一个字节数组到滑动窗口
     * @param bytes 被添加的字节数组
     * @param off 偏移
     * @param len 长度
     */
    public void add (byte[] bytes, int off, int len) {
        for (int i = 0; i < len; i++) {
            add(bytes[off + i]);
        }
    }

}
