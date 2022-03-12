package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.content.Content;

public class ContentActionMsg
{
    public enum Action
    {
        purchase,
        repair,
        open
    }

    public String what;
    public Action action;

    public ContentActionMsg() {}
    public ContentActionMsg(Content what, Action action)
    {
        this.what = what.getID();
        this.action = action;
    }
}
