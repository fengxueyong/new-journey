/**
 * @ClassName:     ${ByteConverter}
 * @Description:   ${}
 * @author         ${wangzongsheng}
 * @version        V1.0
 * @Date           ${2019-01-14}
 */

package com.naixue.util;

public class ByteConverter {

    /**
     * @param buf
     * @return
     */
    public static short bytesToShort(byte[] buf) {
        return (short) (buf[0] & 0xff | ((buf[1] << 8) & 0xff00));
    }

    /**
     * @param buf
     * @return
     */
    public static int bytesToIntBigEndian(byte[] buf) {
        return buf[0] & 0xff | ((buf[1] << 8) & 0xff00)
                | ((buf[2] << 16) & 0xff0000) | ((buf[3] << 24) & 0xff000000);
    }

    /**
     * byte array to int
     *
     * @param buf
     * @return
     */
    public static long bytesToLong(byte[] buf) {
        return (long) buf[0] & 0xffl
                | (((long) buf[1] << 8) & 0xff00l)
                | (((long) buf[2] << 16) & 0xff0000l)
                | (((long) buf[3] << 24) & 0xff000000l)
                | (((long) buf[4] << 32) & 0xff00000000l)
                | (((long) buf[5] << 40) & 0xff0000000000l)
                | (((long) buf[6] << 48) & 0xff000000000000l)
                | (((long) buf[7] << 56) & 0xff00000000000000l);
    }


    public static byte[] shortToBytes(short n) {
        byte[] buf = new byte[2];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (byte) (n >> (8 * i));
        }
        return buf;
    }

    /**
     * int to byte array
     *
     * @param n
     * @return
     */
    public static byte[] intToBytes(int n) {
        byte[] buf = new byte[4];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (byte) (n >> (8 * i));
        }
        return buf;
    }

    public static byte[] longToBytes(long n) {
        byte[] buf = new byte[8];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (byte) (n >> (8 * i));
        }
        return buf;
    }


    public static short bytesToShort(byte[] buf, int offset) {
        return (short) (buf[offset] & 0xff | ((buf[offset + 1] << 8) & 0xff00));
    }

    public static int bytesToInt(byte[] buf, int offset) {
        return buf[offset] & 0xff
                | ((buf[offset + 1] << 8) & 0xff00)
                | ((buf[offset + 2] << 16) & 0xff0000)
                | ((buf[offset + 3] << 24) & 0xff000000);
    }

    public static long bytesToLong(byte[] buf, int offset) {
        return (long) buf[offset] & 0xffl
                | (((long) buf[offset + 1] << 8) & 0xff00l)
                | (((long) buf[offset + 2] << 16) & 0xff0000l)
                | (((long) buf[offset + 3] << 24) & 0xff000000l)
                | (((long) buf[offset + 4] << 32) & 0xff00000000l)
                | (((long) buf[offset + 5] << 40) & 0xff0000000000l)
                | (((long) buf[offset + 6] << 48) & 0xff000000000000l)
                | (((long) buf[offset + 7] << 56) & 0xff00000000000000l);
    }
}