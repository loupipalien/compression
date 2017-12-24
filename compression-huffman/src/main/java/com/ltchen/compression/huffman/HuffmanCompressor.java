package com.ltchen.compression.huffman;

import com.ltchen.compression.Compressor;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author : ltchen
 * @date : 2017/12/15
 * @desc : 霍夫曼压缩
 */
public class HuffmanCompressor implements Compressor{

    /**
     * 输入文件的路径
     */
    private String filePath;

    /**
     * 输入文件的名称
     */
    private String fileName;

    /**
     * 输入文件的大小
     */
    private long fileSize;

    /**
     * 是否显示进度
     */
    private boolean showProgress;

    /**
     * 最新计算的百分比
     */
    private long lastPercent;

    public HuffmanCompressor(String filePath, String fileName, long fileSize, boolean showProgress) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.showProgress = showProgress;
        lastPercent = -1;
    }

    private void updateProgress(long readBytes){
        if (showProgress) {
            long percent = readBytes * 100 / fileSize;
            if (percent != lastPercent) {
                System.out.println(String.format("%s: 处理进度 %d%%", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),percent));
                lastPercent = percent;
            }
        }
    }

    @Override
    public void compress(InputStream is, OutputStream os){
        try {
            // 缓冲包装
            BufferedInputStream bis = new BufferedInputStream(is);
            // 在流开始处标记, 用于复用流 (大文件时 BufferedInputStream 缓冲数据到内存容易导致 java.lang.OutOfMemoryError: java heap space)
            // bis.mark(Integer.MAX_VALUE);
            // 计算输入流中的字节频次统计
            ByteFreqCounter byteFreqCounter = new ByteFreqCounter(this.filePath);
            // 将频次统计写出, 用于解压是构造霍夫曼树
            writeByteFreqs(os, byteFreqCounter.getByteFreqs());
            // 构造霍夫曼树
            HuffmanTree huffmanTree = new HuffmanTree(byteFreqCounter.getByteFreqs());
            // 获取霍夫曼码 TODO 将霍夫曼码写出, 取代写出字节频次统计, 提高压缩率
            Map<Byte,String> huffmanCodeMap = huffmanTree.getHuffmanCodeMap(huffmanTree.getHuffmanCodeMap());
            System.out.println("compress:"+huffmanTree.getHuffmanCodeMap());
            // 重置以复用流
            // bis.reset();
            // 读取流中字节转换为霍夫曼编码写出
            writeByteAsHuffmanCode(bis, os, huffmanCodeMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将字节转换为霍夫曼码写出
     * @param is 输入流
     * @param os 输出流
     * @param huffmanCodeMap 霍夫曼码
     * @throws IOException
     */
    private void writeByteAsHuffmanCode(InputStream is, OutputStream os, Map<Byte,String> huffmanCodeMap) throws IOException {
        long readBytes = 0;
        DataInputStream dis = new DataInputStream(is);
        byte[] buffer = new byte[8196];
        // 记录读入 buffer 的字节个数
        int number;
        // 记录剩余的霍夫曼码串
        String remainStr = "";
        while ((number = dis.read(buffer)) != -1) {
            StringBuilder sb = new StringBuilder(remainStr);
            for (int i = 0; i < number; i++) {
                // 将字节转换为霍夫曼码
                sb.append(huffmanCodeMap.get(buffer[i]));
            }
            String str = sb.toString();
            // 霍夫曼码字符串转换为压缩字节写出
            remainStr = writeHuffmanCodesStrAsByte(os, str);
            // 更新处理进度
            updateProgress(readBytes += number);
        }
        /* 尾部处理:
         * 因为会存在最后的霍夫曼码串不足 8 位的情况, 在其后补 "0" 到 8 位
         * 最后再写入一个字节标记补了多少个 "0", 用于解压时删除
         */
        int zeroNum = 0;
        if (remainStr.length() > 0) {
            int byteLen = 8;
            zeroNum = byteLen - remainStr.length();
            // 补 "0" 够一个字节长度后写出
            while (remainStr.length() < byteLen) {
                remainStr += '0';
            }
            os.write(Integer.parseInt(remainStr, 2));
        }
        // 写出补的 "0" 个数
        os.write(zeroNum);
        // 刷出
        os.flush();
    }

    /**
     *
     * @param os 输出流
     * @param str 霍夫曼码串
     * @return 不足一个字节长度的霍夫曼码串
     * @throws IOException
     */
    private String writeHuffmanCodesStrAsByte(OutputStream os, String str) throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        // 一个字节长度
        int byteLen = 8;
        // 当霍夫曼码串长度小于一个字节的长度时直接返回
        if (str.length() < byteLen) {
            return str;
        }
        // 将 huffmanCodes 每一个字节长度截取出转换为字节写出
        while (str.length() > byteLen) {
            String bitsStr = str.substring(0, byteLen);
            dos.writeByte(Integer.parseInt(bitsStr, 2));
            str = str.substring(byteLen);
        }
        // 刷出
        dos.flush();
        // 返回剩余不足一个字节长度的霍夫曼码字符串
        return str;
    }

    @Override
    public void decompress(InputStream is, OutputStream os){
        try {
            // 读出字节频次统计
            int[] byteFreqs = readByteFreqs(is);
            // 构造霍夫曼树
            HuffmanTree huffmanTree = new HuffmanTree(byteFreqs);
            // 获取霍夫曼码
            Map<String, Byte> huffmanCodeMap = huffmanTree.getHuffmanCodeMap();
            System.out.println("decompress:"+huffmanCodeMap);
            // 读取流中霍夫曼码字符串转换为对应字节写出
            writeHuffmanCodeAsByte(is, os, huffmanCodeMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeHuffmanCodeAsByte(InputStream is, OutputStream os, Map<String, Byte> huffmanCodeMap) throws IOException {
        // 记录以处理的字节数
        long readBytes = 0;
        DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
        byte[] buffer = new byte[8196];
        // 记录读入 buffer 的字节个数
        int number;
        // 记录剩余的霍夫曼码串
        String remainStr = "";
        while ((number = dis.read(buffer)) != -1) {
            StringBuilder sb = new StringBuilder(remainStr);
            for (int i = 0; i < number; i++) {
                // 将字节转换为霍夫曼码
                sb.append(ByteUtil.byteToBits(buffer[i]));
            }
            String str = sb.toString();
            // 尾部处理: 见压缩时的处理
            if (dis.available() < 1) {
                int byteLen = 8;
                int zeroNum = Integer.parseInt(str.substring(str.length() - byteLen), 2);
                str = str.substring(0, str.length() - zeroNum - byteLen);
            }
            // 将霍夫曼码字符串转换为解压缩字节写出
            remainStr = writeHuffmanCodesStrAsByte(os, huffmanCodeMap, str);
            // 更新处理进度
            updateProgress(readBytes += number);
        }
    }

    private String writeHuffmanCodesStrAsByte(OutputStream os, Map<String, Byte> huffmanCodeMap, String str) throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        // 字符串中是否还有霍夫曼码
        boolean hasHuffmanCode = true;
        while (hasHuffmanCode) {
            hasHuffmanCode = false;
            // 将字符串中的霍夫曼码转换为字节写出
            for (String huffmanCode : huffmanCodeMap.keySet()) {
                if(str.startsWith(huffmanCode)) {
                    os.write(huffmanCodeMap.get(huffmanCode));
                    str = str.substring(huffmanCode.length());
                    hasHuffmanCode = true;
                    break;
                }
            }
        }
        // 刷出
        dos.flush();
        // 返回不包含任何霍夫曼码的字符串
        return str;
    }

    /**
     * 写出字节频次统计
     * @param os 输出流
     * @param byteFreqs 字节频次统计
     * @throws IOException
     */
    private void writeByteFreqs(OutputStream os, int[] byteFreqs) throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        // 写出字节个数
        int byteNumber = byteFreqs.length;
        dos.writeInt(byteNumber);
        // 写出字节统计
        for (int i = 0; i < byteNumber; i++) {
            dos.writeInt(byteFreqs[i]);
        }
    }

    /**
     * 读入字节频次统计
     * @param is 输入流
     * @return int[]
     * @throws IOException
     */
    private int[] readByteFreqs(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        // 读出字节个数
        int byteNumber = dis.readInt();
        // 读出字节统计
        int[] byteFreqs = new int[byteNumber];
        for (int i = 0; i < byteNumber; i++) {
            byteFreqs[i] = dis.readInt();
        }
        return byteFreqs;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // 检查参数
        if (args.length != 3) {
            usage();
        }

        // Parse flags
        boolean verbose = args[0].contains("v");
        boolean showProgress = args[0].contains("p");
        boolean compress = args[0].contains("c");
        boolean decompress = args[0].contains("d");
        // 压缩参数和解压缩参数不可同时出现
        if (!(compress ^ decompress)) {
            usage();
        }

        try {
            // 输入输出文件
            File inFile = new File(args[1]);
            FileInputStream in = new FileInputStream(inFile);
            File outFile = new File(args[2]);
            FileOutputStream out = new FileOutputStream(outFile);

            // 霍夫曼压缩器
            HuffmanCompressor huffmanCompressor = new HuffmanCompressor(inFile.getPath(), inFile.getName(), inFile.length(), showProgress);
            String info;

            // 压缩/解压缩
            long startTime = System.currentTimeMillis();
            if (compress) {
                huffmanCompressor.compress(in, out);
                // 压缩统计
                long diff = inFile.length() - outFile.length();
                double ratio = ((double) outFile.length() / (double)inFile.length()) * 100;
                if (diff > 0) {
                    info = String.format("文件大小减小了 %s 字节, 压缩率为 %.1f%%", diff, ratio);
                } else {
                    info = String.format("文件大小增加了 %s 字节, 压缩率为 %.1f%%", -diff, ratio);
                }
            } else {
                huffmanCompressor.decompress(in, out);
                // 解压缩统计
                long diff = inFile.length() - outFile.length();
                if (diff > 0) {
                    info = String.format("文件大小减小了 %s 字节", diff);
                } else {
                    info = String.format("文件大小增加了 %s 字节", -diff);
                }
            }
            long endTime = System.currentTimeMillis();

            // 打印统计信息
            if (verbose) {
                // 压缩/解压缩信息
                System.out.println(info);
                // 耗时
                System.out.println(String.format("耗时 %.3f 秒", (endTime - startTime) / 1000.0));
                System.out.println();
            }

            // 关闭流
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打印使用方法并退出
     */
    private static void usage() {
        System.out.println("用法:");
        System.out.println("\tjava HuffmanCompressor -vpcd [inFilePath] [outFilePath]");
        System.out.println("选项:");
        System.out.println("\t-v  显示详情");
        System.out.println("\t-p  显示进度");
        System.out.println("\t-c  压缩");
        System.out.println("\t-d  解压缩");
        System.exit(1);
    }

}
