package com.desertkun.brainout.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Compressor
{
    private Compressor() {}

    public static byte[] Compress(byte[] bytesToCompress)
    {
        Deflater deflater = new Deflater();
        deflater.setInput(bytesToCompress);
        deflater.finish();

        byte[] bytesCompressed = new byte[4096 * 64];

        int numberOfBytesAfterCompression = deflater.deflate(bytesCompressed);

        byte[] returnValues = new byte[numberOfBytesAfterCompression];

        System.arraycopy
        (
            bytesCompressed,
            0,
            returnValues,
            0,
            numberOfBytesAfterCompression
        );

        return returnValues;
    }

    public static byte[] Compress(String stringToCompress)
    {
        return Compress(stringToCompress.getBytes());
    }

    public static byte[] Decompress(byte[] bytesToDecompress)
    {
        return Decompress(bytesToDecompress, 0, bytesToDecompress.length);
    }

    public static byte[] Decompress(byte[] bytesToDecompress, int offset, int length)
    {
        byte[] returnValues;

        Inflater inflater = new Inflater();

        inflater.setInput(bytesToDecompress, offset, length);

        byte[] decompressBuffer = new byte[4096 * 64];

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try
        {
            while (!inflater.needsInput())
            {
                int decompressed = inflater.inflate(decompressBuffer);
                bos.write(decompressBuffer, 0, decompressed);
            }

            bos.close();

            returnValues = bos.toByteArray();
        }
        catch (DataFormatException | IOException dfe)
        {
            dfe.printStackTrace();
            return null;
        }

        inflater.end();

        return returnValues;
    }

    public static String DecompressToString(byte[] bytesToDecompress)
    {
        byte[] bytesDecompressed = Decompress(bytesToDecompress);

        if (bytesDecompressed == null)
            return null;

        String returnValue = null;

        try
        {
            returnValue = new String
            (
                bytesDecompressed,
                0,
                bytesDecompressed.length,
                "UTF-8"
            );
        }
        catch (UnsupportedEncodingException uee)
        {
            uee.printStackTrace();
            return null;
        }

        return returnValue;
    }
}
