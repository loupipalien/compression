package com.ltchen.compression.deflate;

import com.ltchen.compression.Compressor;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author : ltchen
 * @date : 2018/1/15
 * @desc :
 */
public class DeflateCompressor implements Compressor {


    @Override
    public void compress(InputStream in, OutputStream out) {

    }

    @Override
    public void decompress(InputStream in, OutputStream out) {

    }

    public static void main(String[] args) {
        System.out.println('P');
        System.out.println((char)0x1f);
    }
}
