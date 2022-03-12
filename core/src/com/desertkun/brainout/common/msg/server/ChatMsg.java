package com.desertkun.brainout.common.msg.server;

import com.badlogic.gdx.graphics.Color;

public class ChatMsg
{
    public String author;
    public String text;
    public String key;
    public int color;
    public int senderID;

    public ChatMsg() {}
    public ChatMsg(String author, String text, String key, Color color, int sender)
    {
        this.author = author;
        this.text = text;
        this.color = Color.rgba8888(color);
        this.senderID = sender;
        this.key = key;
    }

    public boolean isTerminal()
    {
        return "terminal".equals(this.key);
    }
}
