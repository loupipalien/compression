package com.ltchen.compression;

import com.ltchen.compression.deflate.DeflateCompressor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App {

    public static void main(String[] args) {
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
            DeflateCompressor deflateCompressor = new DeflateCompressor(inFile.getPath(), inFile.getName(), inFile.length(), showProgress);
            String info;

            // 压缩/解压缩
            long startTime = System.currentTimeMillis();
            if (compress) {
                deflateCompressor.compress(in, out);
                // 压缩统计
                long diff = inFile.length() - outFile.length();
                double ratio = ((double) outFile.length() / (double)inFile.length()) * 100;
                if (diff > 0) {
                    info = String.format("文件大小减小了 %s 字节, 压缩率为 %.1f%%", diff, ratio);
                } else {
                    info = String.format("文件大小增加了 %s 字节, 压缩率为 %.1f%%", -diff, ratio);
                }
            } else {
                deflateCompressor.decompress(in, out);
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
        System.out.println("使用方法:");
        System.out.println("\tjava HuffmanCompressor -vpcd [inFilePath] [outFilePath]");
        System.out.println("选项:");
        System.out.println("\t-v  显示详情");
        System.out.println("\t-p  显示进度");
        System.out.println("\t-c  压缩");
        System.out.println("\t-d  解压缩");
        System.exit(1);
    }
}
