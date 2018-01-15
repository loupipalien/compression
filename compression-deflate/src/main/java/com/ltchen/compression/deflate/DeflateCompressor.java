package com.ltchen.compression.deflate;

import com.ltchen.compression.Compressor;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author : ltchen
 * @date : 2018/1/15
 * @desc :
 */
public class DeflateCompressor implements Compressor {

    /**
     * 见 RFC 1952, 2.2 章节 (https://www.ietf.org/rfc/rfc1952.txt)
     * +---+---+---+---+---+---+---+---+---+---+========//========+===========//==========+---+---+---+---+---+---+---+---+
     * |ID1|ID2| CM|FLG|     MTIME     |XFL| OS|   额外的头字段   |       压缩的数据      |     CRC     |     ISIZE     |
     * +---+---+---+---+---+---+---+---+---+---+========//========+===========//==========+---+---+---+---+---+---+---+---+
     */

    /**
     * 第一个魔法值: 0x1f
     */
    private final static int FIRST_MAGIC_NUMBER = 31;
    /**
     * 第二个魔法值: 0x8b
     */
    private final static int SECOND_MAGIC_NUMBER = 139;

    /**
     * 压缩方法标识, 8 既是 deflate 压缩
     */
    private final static int COMPRESSION_METHOD = 8;

    /**
     * 头标识标志
     * bit 0   FTEXT
     * bit 1   FHCRC
     * bit 2   FEXTRA
     * bit 3   FNAME
     * bit 4   FCOMMENT
     * bit 5   reserved
     * bit 6   reserved
     * bit 7   reserved
     */
    private static int FLG;
    /**
     * 头标识
     */
    public static int FTEXT;
    public static int FHCRC;
    public static int FEXTRA;
    public static int FNAME;
    public static int FCOMMENT;
    /**
     * 修改时间
     */
    private static int MTIME;
    /**
     * 扩展标识
     */
    private static int XFL;
    /**
     * 系统
     */
    private static int OS;
    /**
     * 32 位循环冗余校验值
     */
    private static int CRC32;
    /**
     * 原始文件大小 (字节数)
     */
    private static int ISIZE;


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


    @Override
    public void compress(InputStream in, OutputStream out) {
        BitInputStream bis = new BitInputStream(in);
        BitOutputStream bos = new BitOutputStream(out);

        try {
            // 写出文件头
            bos.writeByte(FIRST_MAGIC_NUMBER);
            bos.writeByte(SECOND_MAGIC_NUMBER);
            bos.writeByte(COMPRESSION_METHOD);
            // TODO 后续处理文件头格式
            for (int i = 0; i < 6; i++) {
                bos.writeByte(0);
            }
            bos.write(fileName.getBytes());
            bos.writeByte(0);

            // 压缩数据并写出
            Deflater deflater = new Deflater(bis, bos);
            deflater.process();

            // 写出文件尾
            bos.writeInt(deflater.getCRCValue());
            bos.writeUnsignedInt(fileSize);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void decompress(InputStream in, OutputStream out) {

    }

    public DeflateCompressor(String filePath, String fileName, long fileSize, boolean showProgress) {
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
                System.out.println(String.format("%s: 处理进度 %d%%", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), percent));
                lastPercent = percent;
            }
        }
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
