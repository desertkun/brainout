package com.desertkun.brainout.utils;

import java.io.*;

public class FileCopy
{
    public static void copyFile(File source, File dest) throws IOException
    {
        copy(new FileInputStream(source), new FileOutputStream(dest));
    }

    public static void copyFile(InputStream source, File dest) throws IOException
    {
        copy(source, new FileOutputStream(dest));
    }

    public static void copy(InputStream is, OutputStream os) throws IOException
    {
        try
        {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
        finally
        {
            is.close();
            os.close();
        }
    }
}
