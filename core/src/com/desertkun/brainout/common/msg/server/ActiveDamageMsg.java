package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.interfaces.LaunchData;

public class ActiveDamageMsg implements UdpMessage
{
    public int activeData;
    public int d;
    public float newHealth;

    public float x;
    public float y;
    public float angle;
    public String content;
    public String kind;

    public ActiveDamageMsg() {}
    public ActiveDamageMsg(ActiveData activeData,
                           float newHealth, float x, float y, float angle, Content content,
                           String damageKind)
    {
        this.activeData = activeData.getId();
        this.d = activeData.getDimensionId();
        this.newHealth = newHealth;

        this.x = x;
        this.y = y;
        this.angle = angle;

        this.content = content != null ? content.getID() : null;
        this.kind = damageKind;
    }
}
