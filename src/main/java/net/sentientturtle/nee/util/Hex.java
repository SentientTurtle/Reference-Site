package net.sentientturtle.nee.util;

/**
 * Utility class for quickly converting bytearrays to hexadecimal strings and back.
 */
public class Hex {
    private final static char[] charArray = "0123456789ABCDEF".toCharArray();
    private static final byte[] byteArray = new byte['F' - '0' + 1];
    static {
        for (int i = 0; i < charArray.length; i++) {
            byteArray[charArray[i] - '0'] = (byte) i;
        }
    }

    public static String toHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = charArray[v >>> 4];
            hexChars[j * 2 + 1] = charArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] fromHex(String string) {
        char[] chars = string.toCharArray();
        assert chars.length % 2 == 0;
        byte[] bytes = new byte[chars.length / 2];
        for (int i = bytes.length - 1; i >= 0; i--) {
            bytes[i] = (byte) (byteArray[chars[i * 2] - '0']  << 4 | byteArray[chars[i * 2 + 1] - '0']);
        }
        return bytes;
    }
}


