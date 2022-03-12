package com.desertkun.brainout.common.msg.client;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.content.shop.ShopCart;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.interfaces.Spawnable;

public class SpawnMsg extends UpdateSelectionsMsg
{
    public SpawnMsg() {}
    public SpawnMsg(Spawnable spawnPointData, ShopCart shopCart)
    {
        super(spawnPointData, shopCart);
    }
}
