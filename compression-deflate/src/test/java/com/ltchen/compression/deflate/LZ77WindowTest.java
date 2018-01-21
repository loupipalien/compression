package com.ltchen.compression.deflate;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author : ltchen
 * @date : 2018/1/21
 * @desc :
 */
public class LZ77WindowTest {

    @Test
    public void testFindLZ77Pair() {
        testLZ77("abcdefghijAabcdefBCDdefEFG", "abcdefghijA<11,6>BCD<6,3>EFG");
        testLZ77("abcde bcde bcde bcde bcde 123", "abcde <5,20>123");
        testLZ77("abcdebcdef", "abcde<4,4>f");
        testLZ77("Blah blah blah blah blah!", "Blah b<5,18>!");
        testLZ77("This is a string with multiple strings within it", "This <3,3>a string with multiple<21,7>s<22,5>in it");
        testLZ77("This is a string of text, whereherehereherehe parts of the string have text that is in other parts of the string", "This <3,3>a string of text, where<4,14> parts<35,5><13,3><49,7>have<51,5><21,3>at<76,4>in o<33,3>r<47,20>");
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < 25; i++) {
            buffer.append("0123456789");
        }
        testLZ77("abcdefghij" + buffer.toString() + "0123abcdefg", "abcdefghij0123456789<10,244><264,7>"
        );
        testLZ77("These blah is blah blah blah!", "These blah is<8,6><5,9>!");
    }

    private void testLZ77(String input, String expected) {
        byte[] buffer = input.getBytes();
        StringBuffer output = new StringBuffer();
        LZ77Window window = new LZ77Window(32768);

        for (int i = 0; i < buffer.length; i++) {
            LZ77Pair pair = window.find(buffer, i, buffer.length);
            if (pair != null) {
                window.add(buffer, i, pair.len);
                i += (pair.len - 1);
                output.append(String.format("<%d,%d>", pair.dist, pair.len));
            } else {
                window.add(buffer[i]);
                output.append((char) buffer[i]);
            }
        }
        assertEquals(expected, output.toString());
    }
}
