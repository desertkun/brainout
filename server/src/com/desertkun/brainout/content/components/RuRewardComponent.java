package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.content.OwnableContent;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.RuRewardComponent")
public class RuRewardComponent extends ServerOwnableComponent
{
    private int amount;

    @Override
    public void owned(PlayerClient client, OwnableContent content)
    {
        client.getProfile().addStat("ru", getAmount(), true);
        client.notify(NotifyAward.ru, getAmount(), NotifyReason.ruEarned, NotifyMethod.message, null);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.amount = jsonData.getInt("amount", 1000);
    }

    public int getAmount()
    {
        return amount;
    }
}
