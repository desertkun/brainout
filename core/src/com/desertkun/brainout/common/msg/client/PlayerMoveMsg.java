package com.desertkun.brainout.common.msg.client;

import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.common.msg.UdpMessage;

public class PlayerMoveMsg implements UdpMessage
{
    public float x;
    public float y;
    public float moveX;
    public float moveY;
    public float aimX;
    public float aimY;

    public PlayerMoveMsg(){}
    public PlayerMoveMsg(float x, float y, Vector2 move,
                         float aimX, float aimY)
    {
        this.x = x;
        this.y = y;
        this.moveX = move.x;
        this.moveY = move.y;
        this.aimX = aimX;
        this.aimY = aimY;
    }
}