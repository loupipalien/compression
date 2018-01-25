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
     * |ID1|ID2| CM|FLG|     MTIME     |XFL| OS|    额外的头字段    |        压缩的数据       |      CRC      |     ISIZE     |
     * +---+---+---+---+---+---+---+---+---+---+========//========+===========//==========+---+---+---+---+---+---+---+---+
     * 注: 一格为一比特
     */

    /**
     * 第一个魔法值: 0x1f
     */
    private final static int ID1 = 31;
    /**
     * 第二个魔法值: 0x8b
     */
    private final static int ID2 = 139;

    /**
     * 压缩方法标识, 8 既是 deflate 压缩
     */
    private final static int CM = 8;

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
    private static int FLG = 0;
    /**
     * 头标识
     */
    public final static int FTEXT = 1;
    public final static int FHCRC = 2;
    public final static int FEXTRA = 4;
    public final static int FNAME = 8;
    public final static int FCOMMENT = 16;
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
            bos.writeByte(ID1);
            bos.writeByte(ID2);
            bos.writeByte(CM);
            //bos.writeByte(FLG);
            bos.writeByte(8);
            // TODO 后续处理文件头格式
            for (int i = 0; i < 6; i++) {
                bos.writeByte(0);
            }

            // 写出文件名
            bos.write(fileName.getBytes());
            bos.writeByte(0);

            // 压缩数据并写出
            Deflater deflater = new Deflater(this, bis, bos);
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
        BitInputStream bis = new BitInputStream(in);
        BitOutputStream bos = new BitOutputStream(out);

        try {
            // 读取文件头中的魔法值并校验
            int id1 = bis.readByte();
            int id2 = bis.readByte();
            if (id1 != ID1 || id2 != ID2) {
                throw new AssertionError("非法的魔法值!");
            }
            // 读取文件头中的压缩方法标记并校验
            int cm = bis.readByte();
            if (cm != CM) {
                throw new AssertionError("不支持的压缩方法!");
            }
            // 读取文件头中的文件标记
            int flg = bis.readByte();
            if ((flg & (FTEXT | FHCRC | FEXTRA | FNAME | FCOMMENT)) != 0) {
                //throw new AssertionError("不支持的扩展标记");
            }
            // 跳过文件头后续 6 个字节
            bis.skipBytes(6);

            if ((flg & FNAME) != 0) {
                int b;
                do {
                    b = bis.readByte();
                } while (b != 0);
            }

            // 解压数据并写出
            Inflater inflater = new Inflater(this, bis, bos);
            inflater.process();

            // 读取文件尾
            int crc = bis.readInt();
            if (crc != inflater.getCRCValue()) {
                throw new AssertionError(String.format("循环冗余校验失配, 期望值 = %08X, 实际值 = %08X", inflater.getCRCValue(), crc));
            }
            bis.readUnsignedInt();
            // 显示处理进度
            updateProgress(bis.getCount());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DeflateCompressor(String filePath, String fileName, long fileSize, boolean showProgress) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.showProgress = showProgress;
        lastPercent = -1;
    }

    public void updateProgress(long readBytes){
        if (showProgress) {
            long percent = readBytes * 100 / fileSize;
            if (percent != lastPercent) {
                System.out.println(String.format("%s: 处理进度 %d%%", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), percent));
                lastPercent = percent;
            }
        }
    }

}
