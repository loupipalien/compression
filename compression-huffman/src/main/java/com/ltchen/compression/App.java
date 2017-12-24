package com.ltchen.compression;

import com.ltchen.compression.huffman.HuffmanCompressUtil;

import java.io.*;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App {

    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        File file = new File("haha");
        System.out.println(file.getName());
    }

//    private long writeBytesToHuffmanCodes(InputStream is, OutputStream os, Map<Byte,String> huffmanCodeMap) throws IOException {
//        long readBytes = 0;
//        long writeBytes = 0;
//        DataInputStream dis = new DataInputStream(is);
//        byte[] buffer = new byte[1024];
//        // 记录读入 buffer 的字节个数
//        int number;
//        // 记录剩余的霍夫曼码串
//        String remainStr = "";
//        while ((number = dis.read(buffer)) != -1) {
//            HuffmanCompressUtil.ThreeTuple threeTuple = new HuffmanCompressUtil.ThreeTuple(buffer, number, remainStr);
//            HuffmanCompressUtil.TwoTuple twoTuple = HuffmanCompressUtil.bytesToHuffmanCodes(threeTuple, huffmanCodeMap);
//            byte[] bytes = (byte[]) twoTuple.first;
//            // 将转换后的字节写出
//            os.write(bytes);
//            // 剩余的霍夫曼码串
//            remainStr = (String) twoTuple.second;
//            // 更新处理进度
//            updateProgress(readBytes += number);
//            // 记录写出大小
//            writeBytes += bytes.length;
//        }
//        /* 尾部处理:
//         * 因为会存在最后的霍夫曼码串不足 8 位的情况, 在其后补 "0" 到 8 位
//         * 最后再写入一个字节标记补了多少个 "0", 用于解压时删除
//         */
//        int zeroNum = 0;
//        if (remainStr.length() > 0) {
//            int byteLen = 8;
//            zeroNum = byteLen - remainStr.length();
//            // 补 "0" 够一个字节长度后写出
//            while (remainStr.length() < byteLen) {
//                remainStr += '0';
//            }
//            os.write(Integer.parseInt(remainStr, 2));
//            writeBytes++;
//        }
//        // 写出补的 "0" 个数
//        os.write(zeroNum);
//        writeBytes++;
//        // 刷出
//        os.flush();
//        // 返回写出字节大小
//        return writeBytes;
//    }
//
//    private long writeHuffmanCodesToBytes(InputStream is, OutputStream os, Map<String,Byte> huffmanCodeMap) throws IOException {
//        long readBytes = 0;
//        long writeBytes = 0;
//        DataInputStream dis = new DataInputStream(is);
//        byte[] buffer = new byte[1024];
//        // 记录读入 buffer 的字节个数
//        int number;
//        // 记录剩余的霍夫曼码串
//        String remainStr = "";
//        while ((number = dis.read(buffer)) != -1) {
//            // 尾部处理: 见压缩时的处理
//            if (dis.available() < 1) {
//                // 尾部处理字节在最后一次单独获取到
//                if (number == 1) {
//                    int zeroNum = buffer[number--];
//                    remainStr = remainStr.substring(0, remainStr.length() - zeroNum);
//                } else {
//                    int zeroNum = buffer[number--];
//                    // TODO 处理不下去了...
//                }
//            }
//            HuffmanCompressUtil.ThreeTuple threeTuple = new HuffmanCompressUtil.ThreeTuple(buffer, number, remainStr);
//            HuffmanCompressUtil.TwoTuple twoTuple = HuffmanCompressUtil.huffmanCodesToBytes(threeTuple, huffmanCodeMap);
//        }
//        return 1L;
//    }
}
