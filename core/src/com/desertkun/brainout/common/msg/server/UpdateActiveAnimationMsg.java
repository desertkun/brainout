package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.data.active.AnimationData;

public class UpdateActiveAnimationMsg
{
    public int object;
    public int d;

    public String animation[];
    public boolean loop;

    public UpdateActiveAnimationMsg() {}
    public UpdateActiveAnimationMsg(AnimationData animationData, String animation[], boolean loop)
    {
        this.object = animationData.getId();
        this.d = animationData.getDimensionId();
        this.animation = animation;
        this.loop = loop;
    }
}
