package com.ltchen.compression;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author : ltchen
 * @date : 2017/12/01
 * @desc : 压缩接口类: 包括压缩方法和解压缩方法
 */

public interface Compressor {

    /**
     * 从输入流中读取字节数, 通过压缩算法写出到输出流
     * @param in 输入流
     * @param out 输出流
     */
    void compress(InputStream in, OutputStream out);

    /**
     * 从输入流中读去字节数, 通过解压缩算法写出到输出流
     * @param in 输入流
     * @param out 输出流
     */
    void decompress(InputStream in, OutputStream out);
}