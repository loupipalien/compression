package com.ltchen.compression.lz77;

import com.ltchen.compression.Compressor;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author : ltchen
 * @date : 2017/12/15
 * @desc :
 */
public class LZ77Compressor implements Compressor{

    /**
     *
     */
    private static final int MAX_WINDOW_SIZE = (1 << 12) - 1;

    /**
     * 前向缓冲区大小
     */
    private static final int LOOK_AHEAD_BUFFER_SIZE = (1 << 4) - 1;

    /**
     * 滑动窗口大小
     */
    private int slideWindowSize = MAX_WINDOW_SIZE;

    public LZ77Compressor(int slideWindowSize) {
        this.slideWindowSize = slideWindowSize;
    }

    public static void main(String[] args) {
        System.out.println(MAX_WINDOW_SIZE);
    }
    @Override
    public void compress(InputStream in, OutputStream out) {


    }

    @Override
    public void decompress(InputStream in, OutputStream out) {

    }
}
