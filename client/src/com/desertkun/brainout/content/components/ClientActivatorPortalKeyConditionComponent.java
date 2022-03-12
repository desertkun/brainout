package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.active.PortalData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.LocalizedString;

@Reflect("content.components.ClientActivatorPortalKeyConditionComponent")
public class ClientActivatorPortalKeyConditionComponent extends ClientActivatorConditionComponent
{
    private LocalizedString lockedText;

    public ClientActivatorPortalKeyConditionComponent()
    {
        this.lockedText = new LocalizedString();
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public boolean testCondition(PlayerData playerData, ComponentObject componentObject)
    {
        if (!(componentObject instanceof PortalData))
            return false;

        PortalData portalData = ((PortalData) componentObject);

        if (!portalData.isLocked())
        {
            return true;
        }

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc == null)
            return false;

        ConsumableContainer cnt = poc.getConsumableContainer();

        return cnt.hasConsumable(portalData.getKey());
    }

    @Override
    public String getFailedConditionLocalizedText()
    {
        return lockedText.get();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData.has("lockedText"))
        {
            this.lockedText.set(jsonData.getString("lockedText"));
        }
    }


}
