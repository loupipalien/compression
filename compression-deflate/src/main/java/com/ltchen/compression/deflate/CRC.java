package com.ltchen.compression.deflate;

/**
 * @author : ltchen
 * @date : 2018/1/16
 * @desc : 见 RFC 1952, 8 章节 (https://www.ietf.org/rfc/rfc1952.txt)
 */
public class CRC {

    /**
     * 所有 8 比特信息的 CRC 码.
     */
    private final static int[] CRC_TABLE = new int[256];
    /**
     * CRC 校验值
     */
    private int value;

    static {
        for (int n = 0; n < 256; n++) {
            int c = n;
            for (int k = 0; k < 8; k++) {
                if ((c & 1) == 1) {
                    c = (c >>> 1) ^ 0xedb88320;
                } else {
                    c >>>= 1;
                }
            }
            CRC_TABLE[n] = c;
        }
    }

    public CRC() {
        // 等同于 0 ^ 0xffffffff
        value = 0xffffffff;
    }

    /**
     * 返回 CRC 的校验值
     * @return
     */
    public int getValue() {
        // 等同于 value ^ 0xffffffff
        return ~value;
    }

    /**
     * 更新校验值
     * @param b 字节
     */
    public void update(byte b) {
        value = CRC_TABLE[(value ^ b) & 0xff] ^ (value >>> 8);
    }

    /**
     * 更新校验值
     * @param bytes 字节数组
     */
    public void update(byte[] bytes) {
        update(bytes, 0, bytes.length);
    }

    /**
     * 更新校验值
     * @param bytes 字节数组
     * @param off 偏移
     * @param len 长度
     */
    public void update(byte[] bytes, int off, int len) {
        for (int i = 0; i < len; i++) {
            update(bytes[off + i]);
        }
    }

    public static void main(String[] args) {
        String str = "Hello World!";
        CRC crc = new CRC();
        crc.update(str.getBytes());
        int value = crc.getValue();
        System.out.println("crc value:" + value);
    }
}
