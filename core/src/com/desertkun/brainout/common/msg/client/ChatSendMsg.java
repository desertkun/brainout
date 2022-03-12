package com.desertkun.brainout.common.msg.client;

public class ChatSendMsg
{
    public String text;
    public Mode mode;

    public enum Mode
    {
        everyone,
        teamOnly,
        clan
    }

    public ChatSendMsg() {}
    public ChatSendMsg(String text, Mode mode)
    {
        this.text = text;
        this.mode = mode;
    }
}
