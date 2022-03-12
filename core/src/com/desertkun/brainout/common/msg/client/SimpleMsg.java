package com.desertkun.brainout.common.msg.client;

public class SimpleMsg
{
    public enum Code
    {
        mapInited,
        clientInited,
        start,
        invalidSpawn,
        updateSpawn,
        notAllowed
    }

    public Code code;

    public SimpleMsg() {}
    public SimpleMsg(Code code)
    {
        this.code = code;
    }
}
