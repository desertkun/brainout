package com.desertkun.brainout.common.msg;

public class SetFriendlyStatusMsg implements ModeMessage
{
    public boolean friendly;

    public SetFriendlyStatusMsg() {}
    public SetFriendlyStatusMsg(boolean friendly)
    {
        this.friendly = friendly;
    }
}
