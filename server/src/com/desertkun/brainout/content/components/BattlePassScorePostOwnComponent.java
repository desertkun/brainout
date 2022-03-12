package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.online.ServerBattlePassEvent;
import com.desertkun.brainout.online.ServerEvent;
import com.desertkun.brainout.reflection.Reflect;

@Reflect("content.components.BattlePassScorePostOwnComponent")
public class BattlePassScorePostOwnComponent extends PostOwnComponent
{
    private int score;

    @Override
    public void owned(PlayerClient playerClient, OwnableContent content)
    {
        playerClient.updateEvents(() ->
        {
            ServerBattlePassEvent e = null;

            for (ObjectMap.Entry<Integer, ServerEvent> entry : playerClient.getOnlineEvents())
            {
                if (entry.value instanceof ServerBattlePassEvent)
                {
                    e = ((ServerBattlePassEvent) entry.value);
                    break;
                }
            }

            if (e == null)
            {
                playerClient.log("Cannot provide battle points: no battle event");
                return;
            }

            e.addScore(getScore());
            playerClient.updateEvents();
        });
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        score = jsonData.getInt("score");
    }

    public int getScore()
    {
        return score;
    }
}
