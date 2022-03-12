package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerAddStatItemComponent")
public class ServerAddStatItemComponent extends ServerStoreItemComponent
{
    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
    }

    @Override
    public boolean purchased(PlayerClient playerClient)
    {
        AddStatItemComponent cmp = getContent().getComponentFrom(AddStatItemComponent.class);

        if (cmp == null)
            return false;

        String stat = cmp.getStat();
        int amount = cmp.getAmount();

        playerClient.addStat(stat, amount);

        NotifyReason reason = null;

        switch (stat)
        {
            case Constants.User.TECH_SCORE:
                reason = NotifyReason.purchase; break;
            case Constants.User.SKILLPOINTS:
                reason = NotifyReason.skillPointsEarned; break;
            case Constants.User.GEARS:
                reason = NotifyReason.gearsEarned; break;
            case Constants.User.NUCLEAR_MATERIAL:
                reason = NotifyReason.nuclearMaterialReceived; break;
        }

        NotifyAward award = null;

        switch (stat)
        {
            case Constants.User.TECH_SCORE:
                award = NotifyAward.techScore; break;
            case Constants.User.SKILLPOINTS:
                award = NotifyAward.skillpoints; break;
            case Constants.User.GEARS:
                award = NotifyAward.gears; break;
            case Constants.User.NUCLEAR_MATERIAL:
                award = NotifyAward.nuclearMaterial; break;
        }

        if (award != null && reason != null)
            playerClient.notify(award, amount, reason, NotifyMethod.message, null);

        return true;
    }
}
