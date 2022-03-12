package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.data.active.ActiveData;

public class ServerActiveVisibilityMsg extends ServerActiveMoveMsg
{
    public boolean v;

    public ServerActiveVisibilityMsg(int object, float x, float y,
                                     float speedX, float speedY, float angle,
                                     String dimension, boolean visibility)
    {
        super(object, x, y, speedX, speedY, angle, dimension);

        this.v = visibility;
    }

    public ServerActiveVisibilityMsg()
    {
    }
}
