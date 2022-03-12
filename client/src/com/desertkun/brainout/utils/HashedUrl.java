package com.desertkun.brainout.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HashedUrl
{
    private static Pattern PATTERN = Pattern.compile("(brainout://)?([A-Za-z0-9+/=]+)*/?");

    private String location;
    private int tcp;
    private int udp;
    private int http;

    public boolean unhash(String hash)
    {
        try
        {
            Matcher matcher = PATTERN.matcher(hash);
            if (matcher.matches())
            {
                byte[] decodedBytes = Base64.decode(matcher.group(2).getBytes());
                String decoded = new String(decodedBytes);

                String[] split = decoded.split(";");

                this.location = split[0];
                this.tcp = Integer.valueOf(split[1]);
                this.udp = Integer.valueOf(split[2]);
                this.http = Integer.valueOf(split[3]);

                return true;
            }

            return false;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public String getLocation()
    {
        return location;
    }

    public int getTcp()
    {
        return tcp;
    }

    public int getUdp()
    {
        return udp;
    }

    public int getHttp()
    {
        return http;
    }
}
