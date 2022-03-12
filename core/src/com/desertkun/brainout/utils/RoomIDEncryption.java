package com.desertkun.brainout.utils;

public class RoomIDEncryption
{
    private static int MuhXor = 0xDEADBEEF;

    public static byte[] IntToByteArray(int value)
    {
        return new byte[] {
            (byte)(value >>> 24),
            (byte)(value >>> 16),
            (byte)(value >>> 8),
            (byte)value};
    }

    public static int ByteArrayToInt(byte[] b)
    {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public static String EncryptHumanReadable(String data)
    {
        try
        {
            int converted = Integer.parseInt(data) ^ MuhXor;
            byte[] input = IntToByteArray(converted);
            String encoded = Base32.encode(input);
            return encoded.replaceAll("(.{3})", "$1-");
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static String DecryptHumanReadable(String data)
    {
        try
        {
            String decoded = data.replaceAll("-", "");
            byte[] encrypted;

            try
            {
                encrypted = Base32.decode(decoded);
            }
            catch (IllegalArgumentException e)
            {
                return null;
            }

            int roomId = ByteArrayToInt(encrypted);

            roomId = roomId ^ MuhXor;

            return String.valueOf(roomId);
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
