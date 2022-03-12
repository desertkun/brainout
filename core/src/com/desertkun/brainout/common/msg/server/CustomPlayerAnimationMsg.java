package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.data.active.PlayerData;

public class CustomPlayerAnimationMsg
{
    public int object;
    public int d;

    public String animationName;
    public String effect;

    public CustomPlayerAnimationMsg() {}
    public CustomPlayerAnimationMsg(PlayerData playerData, String animationName, String effect)
    {
        this.object = playerData.getId();
        this.d = playerData.getDimensionId();

        this.animationName = animationName;
        this.effect = effect;
    }
}
