package com.desertkun.brainout.common.msg.server;

public class ServerPlayerMoveMsg extends ServerActiveMoveMsg
{
    public float aimX;
    public float aimY;
    public boolean enforce;

    public ServerPlayerMoveMsg(){}
    public ServerPlayerMoveMsg(int object, float x, float y,
                               float speedX, float speedY, float angle,
                               String dimension,
                               float aimX, float aimY, boolean enforce)
    {
        super(object, x, y, speedX, speedY, angle, dimension);

        this.aimX = aimX;
        this.aimY = aimY;
        this.enforce = enforce;
    }
}