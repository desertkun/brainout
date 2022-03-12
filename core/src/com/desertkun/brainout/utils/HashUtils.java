package com.desertkun.brainout.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HashUtils
{
    private static Mac sha256_HMAC;

    static
    {
        try
        {
            sha256_HMAC = Mac.getInstance("HmacSHA256");
        }
        catch (Exception e)
        {
            sha256_HMAC = null;
        }
    }

    public static byte[] Verify(String key, byte[] data)
    {
        if (sha256_HMAC == null)
            return null;

        try
        {
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            return sha256_HMAC.doFinal(data);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static String Verify(String key, String data)
    {
        if (sha256_HMAC == null)
            return "";

        try
        {
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            return Base64.encode(sha256_HMAC.doFinal(data.getBytes()));
        }
        catch (Exception e)
        {
            return "";
        }
    }
}
