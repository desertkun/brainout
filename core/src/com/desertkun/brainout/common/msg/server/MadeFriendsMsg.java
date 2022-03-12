package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.ModeMessage;

public class MadeFriendsMsg implements ModeMessage
{
    public int with;
    public boolean friend;

    public MadeFriendsMsg() {}
    public MadeFriendsMsg(int with, boolean friend)
    {
        this.with = with;
        this.friend = friend;
    }
}
