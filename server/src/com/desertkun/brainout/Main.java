package com.desertkun.brainout;

public class Main
{
    public static void main(String[] args)
    {
        BrainOutServer.initServerInstance(args, new ServerEnvironment());

        BrainOutServer.getInstance().startMain();
    }
}