package com.desertkun.brainout.common.msg.client;

import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class RemoveBlockMsg extends PlayerMoveMsg
{
    public int placeX;
    public int placeY;
    public int layer;
    public int recordId;

    public RemoveBlockMsg()
    {
        super();
    }

    public RemoveBlockMsg(float x, float y, Vector2 move, float aimX, float aimY,
                          int placeX, int placeY, int layer, ConsumableRecord record)
    {
        super(x, y,  move, aimX, aimY);

        this.placeX = placeX;
        this.placeY = placeY;
        this.layer = layer;
        this.recordId = record.getId();
    }
}
