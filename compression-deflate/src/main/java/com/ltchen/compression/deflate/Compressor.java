package com.ltchen.compression.deflate;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author : ltchen
 * @date : 2017/12/19
 * @desc :
 */
public interface Compressor {

    /**
     * 从输入流中读取数据, 经过压缩后写入输出流
     * @param is 输入流
     * @param os 输出流
     * @return 压缩日志
     */
    public abstract String compress(InputStream is, OutputStream os);

    /**
     * 从输入流中读取数据, 经过解压缩后写入输出流
     * @param is 输入流
     * @param os 输出流
     * @return 解压缩日志
     */
    public abstract String decompress(InputStream is, OutputStream os);
}
