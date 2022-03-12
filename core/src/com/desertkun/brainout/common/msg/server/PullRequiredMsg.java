package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.UdpMessage;

public class PullRequiredMsg implements UdpMessage
{
    public int weaponId;
    public String slot;

    public PullRequiredMsg() {}
    public PullRequiredMsg(int weaponId, String slot)
    {
        this.weaponId = weaponId;
        this.slot = slot;
    }
}
