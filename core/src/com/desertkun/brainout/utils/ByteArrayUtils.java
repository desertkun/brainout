package com.desertkun.brainout.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteArrayUtils
{
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static byte[] toByteArray(InputStream stream)
    {
        try
        {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead; int nTotal = 0;
            byte[] data = new byte[16384];

            while ((nRead = stream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
                nTotal += nRead;
            }

            buffer.flush();
            return buffer.toByteArray();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static String toHex(ByteBuffer bytes)
    {
        char[] hexChars = new char[bytes.limit() * 2];

        for ( int j = 0; j < bytes.limit(); j++ ) {
            int v = bytes.get() & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String toHex(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] fromHex(String s)
    {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
