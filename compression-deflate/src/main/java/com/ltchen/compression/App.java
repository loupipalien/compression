package com.ltchen.compression;

/**
 * Hello world!
 *
 */
public class App {

    public static void main( String[] args ) {
        int[] CRC_TABLE = new int[256];
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

        System.out.println( "Hello World!" );
        System.out.println(Integer.toHexString(1));
        String str = "00000000000000000000000000000011";
        System.out.println(Integer.valueOf(str, 2).toString());
    }
}
