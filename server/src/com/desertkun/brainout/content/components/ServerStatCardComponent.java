package com.desertkun.brainout.content.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.data.gamecase.gen.CardData;
import com.desertkun.brainout.data.gamecase.gen.ContentCardData;
import com.desertkun.brainout.data.gamecase.gen.StatCardData;
import com.desertkun.brainout.online.UserProfile;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerStatCardComponent")
public class ServerStatCardComponent extends ServerCardComponent
{
    private String stat;
    private int amountFrom, amountTo;

    @Override
    public void generate(CardData cardData)
    {
        if (!(cardData instanceof StatCardData))
            return;

        StatCardData statCardData = ((StatCardData) cardData);

        statCardData.setStat(stat);
        statCardData.setAmount(MathUtils.random(amountFrom, amountTo));
    }

    @Override
    public void apply(PlayerClient client, UserProfile profile, CardData cardData)
    {
        if (!(cardData instanceof StatCardData))
            return;

        StatCardData statCardData = ((StatCardData) cardData);

        if (profile != null)
        {
            profile.addStat(statCardData.getStat(), statCardData.getAmount(), true);

            client.resourceEvent(1,
                "card",
                cardData.getCaseData().getCase().getID(),
                ((StatCardData) cardData).getCard().getID());

            client.resourceEvent(statCardData.getAmount(),
                statCardData.getStat(),
                "case-card",
                statCardData.getStat());
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.stat = jsonData.getString("stat");
        if (jsonData.has("amount"))
        {
            JsonValue amount = jsonData.get("amount");

            amountFrom = amount.getInt("from", 1);
            amountTo = amount.getInt("to", 1);
        }
        else
        {
            amountFrom = 1;
            amountTo = 1;
        }
    }
}
