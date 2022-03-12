package com.desertkun.brainout.playstate.special;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.RandomJumpComponentData;
import com.desertkun.brainout.data.components.ServerPickupCallbackComponentData;
import com.desertkun.brainout.mode.GameMode;

public class CandiesGame extends SpecialGame
{
    private static Array<String> candies = new Array<>(new String[]{
        "active-candy-1",
        "active-candy-2",
        "active-candy-3"
    });

    @Override
    public void init()
    {
    }

    @Override
    public void onClientDeath(Client client, PlayerData playerData)
    {
        GameMode mode = BrainOutServer.Controller.getGameMode();

        if (mode == null)
            return;


        switch (mode.getID())
        {
            case editor:
            case editor2:
            case free:
            case lobby:
            {
                return;
            }
        }

        Map map = playerData.getMap();

        if (map == null)
            return;;

        int amount = MathUtils.random(3, 5);

        for (int i = 0; i < amount; i++)
        {
            Active candy = BrainOutServer.ContentMgr.get(candies.random(), Active.class);
            ActiveData data = candy.getData(map.getDimension());

            ServerPickupCallbackComponentData callback = data.getComponent(ServerPickupCallbackComponentData.class);

            if (callback != null)
            {
                callback.setCallback(picker -> picker.addStat("candies", 1));
            }

            data.setPosition(playerData.getX(), playerData.getY());

            RandomJumpComponentData j = data.getComponent(RandomJumpComponentData.class);
            j.jump(true);

            map.addActive(map.generateServerId(), data, true);
        }
    }

    @Override
    public void release()
    {
    }
}
