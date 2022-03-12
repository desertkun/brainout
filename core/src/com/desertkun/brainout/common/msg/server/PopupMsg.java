package com.desertkun.brainout.common.msg.server;

public class PopupMsg
{
    public String title;
    public String data;

    public PopupMsg() {}
    public PopupMsg(String title, String data)
    {
        this.title = title;
        this.data = data;
    }
}
