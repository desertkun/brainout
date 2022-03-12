package com.desertkun.brainout.online;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.client.PlayerClient;
import org.json.JSONArray;
import org.json.JSONObject;

public class Promo
{
    private Array<ServerRewards> rewards = new Array<>();

    public Promo(JSONObject data)
    {
        JSONArray promos = data.optJSONArray("result");

        if (promos == null)
            return;

        for (int i = 0, t = promos.length(); i < t; i++)
        {
            JSONObject item = promos.optJSONObject(i);

            if (item == null)
                continue;

            int amount = item.optInt("amount", 1);
            JSONObject payload = item.optJSONObject("payload");

            if (payload == null)
                continue;

            this.rewards.add(new ServerRewards(payload, amount));
        }
    }

    public boolean apply(PlayerClient playerClient)
    {
        boolean weAreGood = false;

        for (ServerRewards reward : rewards)
        {
            weAreGood |= reward.apply(playerClient);
        }

        return weAreGood;
    }
}
