package com.desertkun.brainout.common.msg.client.editor2;

import com.desertkun.brainout.common.msg.ModeMessage;

public class RemoveUserImageMsg implements ModeMessage
{
    public String name;

    public RemoveUserImageMsg() {}
    public RemoveUserImageMsg(String name)
    {
        this.name = name;
    }
}
