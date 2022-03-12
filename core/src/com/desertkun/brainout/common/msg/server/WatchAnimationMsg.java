package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.data.active.ActiveData;

public class WatchAnimationMsg
{
    public String d;
    public int o;
    public String b;
    public String eff;

    public WatchAnimationMsg() {}
    public WatchAnimationMsg(String dimension, ActiveData object, String bone, String eff)
    {
        this.d = dimension;
        this.o = object.getId();
        this.b = bone;
        this.eff = eff;
    }
}
