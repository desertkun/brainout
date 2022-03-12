package com.desertkun.brainout.common.msg.client;

public class CreateClanMsg
{
    public String name;
    public String avatar;

    public CreateClanMsg() {}
    public CreateClanMsg(String name, String avatar)
    {
        this.name = name;
        this.avatar = avatar;
    }
}
