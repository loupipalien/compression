package com.ltchen.compression.deflate;

import java.io.DataOutputStream;
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


}
