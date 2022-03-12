package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.online.ClientProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerUnlockStoreItemComponent")
public class ServerUnlockStoreItemComponent extends ServerStoreItemComponent
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
        UnlockStoreItemComponent cmp = getContent().getComponentFrom(UnlockStoreItemComponent.class);

        if (cmp != null && playerClient != null)
        {
            playerClient.gotOwnable(cmp.getContent(), "purchase", ClientProfile.OnwAction.owned, cmp.getAmount());
        }

        return true;
    }
}
