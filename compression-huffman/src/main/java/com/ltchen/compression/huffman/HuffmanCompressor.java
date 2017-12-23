package com.ltchen.compression.huffman;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author : ltchen
 * @date : 2017/12/23
 * @desc :
 */
public class HuffmanCompressor {

    public String compress(InputStream is, OutputStream os) throws IOException, ClassNotFoundException {
        // 缓冲包装
        BufferedInputStream bis = new BufferedInputStream(is);
        // 在流开始处标记, 用于复用流
        bis.mark(Integer.MAX_VALUE);
        // 计算输入流中的字节频次统计is
        ByteFreqCounter byteFreqCounter = new ByteFreqCounter(bis);
        // 将频次统计写出, 用于解压是构造霍夫曼树
        writeByteFreqs(new DataOutputStream(os), byteFreqCounter.getByteFreqs());
        // 构造霍夫曼树
        HuffmanTree huffmanTree = new HuffmanTree(byteFreqCounter.getByteFreqs());
        // 获取霍夫曼码 TODO 将霍夫曼码写出, 取代写出字节频次统计, 提高压缩率
        Map<String,Integer> huffmanCodes = huffmanTree.getHuffmanCodes();
        // 重置以复用流
        bis.reset();
        // 读取流中字节转换为霍夫曼编码写出
        String code = huffmanCodes.get(bis.read());
        System.out.println(huffmanCodes.size());
        System.out.println(huffmanCodes);
//        writeByteFreqs(os, new int[]{1,2,3});
        os.flush();
        os.close();
        return null;
    }

    public String decompress(InputStream is, OutputStream os) throws IOException {
        int[] byteFreqs = readByteFreqs(new DataInputStream(is));
        System.out.println(new HuffmanTree(byteFreqs).getHuffmanCodes());
        is.close();
        return null;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        HuffmanCompressor compressor = new HuffmanCompressor();
//        compressor.compress(new FileInputStream("pom.xml"), new FileOutputStream("pom.out"));
//        compressor.decompress(new FileInputStream("pom.out"), null);

        OutputStream os = new BufferedOutputStream(new FileOutputStream("pom.out"));
        os.write(1);
        os.write(2);
        os.write(3);
        os.flush();os.close();
        InputStream is = new BufferedInputStream(new FileInputStream("pom.out"));
        System.out.println(is.read());
        is.mark(Integer.MAX_VALUE);
        System.out.println(is.read());
        System.out.println(is.read());
        is.reset();
        System.out.println(is.read());
        System.out.println(is.read());
        System.out.println(is.read());
    }

    private void writeByteFreqs(DataOutputStream dos, int[] byteFreqs) throws IOException {
        int byteNumber = byteFreqs.length;
        // 写出字节个数
        dos.writeInt(byteNumber);
        // 写出字节统计
        for (int i = 0; i < byteNumber; i++) {
            dos.writeInt(byteFreqs[i]);
        }
    }

    private int[] readByteFreqs(DataInputStream dis) throws IOException {
        // 读出字节个数
        int byteNumber = dis.readInt();
        // 读出字节统计
        int[] byteFreqs = new int[byteNumber];
        for (int i = 0; i < byteNumber; i++) {
            byteFreqs[i] = dis.readInt();
        }
        return byteFreqs;
    }
}
