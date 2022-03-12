package com.desertkun.brainout.online;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.client.PlayerClient;
import org.json.JSONArray;
import org.json.JSONObject;

public class ServerRewards
{
    private Array<ServerReward> rewards;

    public ServerRewards(JSONObject payload, int amount)
    {
        rewards = new Array<>();

        JSONArray actions = payload.optJSONArray("actions");

        if (actions != null)
        {
            for (int j = 0, y = actions.length(); j < y; j++)
            {
                JSONObject action = actions.getJSONObject(j);

                ServerReward reward = new ServerReward();
                reward.read(action, amount);

                rewards.add(reward);
            }
        }
    }

    public boolean apply(PlayerClient playerClient)
    {
        boolean weAreGood = false;

        for (ServerReward reward : rewards)
        {
            weAreGood |= reward.apply(playerClient);
        }

        return weAreGood;
    }
}
