package com.desertkun.brainout.common.msg.server;

public class FreePlayRadioMsg
{
    public String message;
    public int repeat;

    public FreePlayRadioMsg() {}
    public FreePlayRadioMsg(String message, int repeat)
    {
        this.message = message;
        this.repeat = repeat;
    }
}
