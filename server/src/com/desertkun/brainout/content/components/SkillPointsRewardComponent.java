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

@Reflect("content.components.SkillPointsRewardComponent")
public class SkillPointsRewardComponent extends ServerOwnableComponent
{
    private int amount;

    @Override
    public void owned(PlayerClient client, OwnableContent content)
    {
        client.getProfile().addStat(Constants.User.SKILLPOINTS, getAmount(), true);
        client.notify(NotifyAward.skillpoints, getAmount(), NotifyReason.skillPointsEarned,
                NotifyMethod.message, null);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.amount = jsonData.getInt("amount", 1);
    }

    public int getAmount()
    {
        return amount;
    }
}
