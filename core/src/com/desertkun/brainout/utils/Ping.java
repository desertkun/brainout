package com.desertkun.brainout.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Ping
{
    private static ExecutorService pool = Executors.newFixedThreadPool(4);

    public interface PingCallback
    {
        void result(boolean success, int time);
    }

    public static void GetLatency(String host, PingCallback callback)
    {
        pool.submit(() ->
        {
            try
            {
                long now = System.currentTimeMillis();
                boolean result = InetAddress.getByName(host).isReachable(1000);
                long spent = System.currentTimeMillis() - now;
                callback.result(result, result ? (int)spent : 0);
            }
            catch (IOException e)
            {
                callback.result(false, 0);
            }
        });
    }
}
