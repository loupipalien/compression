package com.ltchen.compression.deflate;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;
import java.util.zip.CRC32;

/**
 * @author : ltchen
 * @date : 2018/1/21
 * @desc :
 */
public class CRCTest {

    @Test
    public void testCRC() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < new Random().nextInt(10000); i++) {
            sb.append((char) random.nextInt(256));
        }
        String str = sb.toString();
        System.out.println("str:" + str);
        CRC crc = new CRC();
        crc.update(str.getBytes());
        System.out.println("crc:" + crc.getValue());
        CRC32 crc32 = new CRC32();
        crc32.update(str.getBytes());
        System.out.println("crc32:" + crc32.getValue());
        Assert.assertEquals(crc.getValue(), crc32.getValue());
    }

}
