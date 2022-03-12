package com.desertkun.brainout.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import com.badlogic.gdx.files.FileHandle;

public class md5 {
	
	public static String getFromFile(FileHandle handle) {
	    InputStream inputStream = null;
	    try {
	        MessageDigest digest = MessageDigest.getInstance("MD5");
	        
	        inputStream = new DigestInputStream(handle.read(), digest);
	        byte[] buffer = new byte[1024];
	        int numRead = 0;
	        while (numRead != -1) {
	            numRead = inputStream.read(buffer);
	        }
	        
	        byte [] md5Bytes = digest.digest();
	        return convertHashToString(md5Bytes);
	    } catch (Exception e) {
	        return null;
	    } finally {
	        if (inputStream != null) {
	            try {
	                inputStream.close();
	            } catch (Exception e) { }
	        }
	    }
	}

    public static String getFromString(String str) {
        InputStream inputStream = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            inputStream = new DigestInputStream(new ByteArrayInputStream(str.getBytes(), 0, str.length()),
                    digest);
            byte[] buffer = new byte[1024];
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
            }

            byte [] md5Bytes = digest.digest();
            return convertHashToString(md5Bytes);
        } catch (Exception e) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) { }
            }
        }
    }

	private static String convertHashToString(byte[] md5Bytes) {
	    String returnVal = "";
	    for (int i = 0; i < md5Bytes.length; i++) {
	        returnVal += Integer.toString(( md5Bytes[i] & 0xff ) + 0x100, 16).substring(1);
	    }
	    return returnVal.toLowerCase();
	}
}
