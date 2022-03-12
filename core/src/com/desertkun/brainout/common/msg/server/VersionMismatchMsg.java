package com.desertkun.brainout.common.msg.server;

public class VersionMismatchMsg
{
    public String serverVersion;

    public VersionMismatchMsg() {}

    public VersionMismatchMsg(String serverVersion)
    {
        this.serverVersion = serverVersion;
    }
}
