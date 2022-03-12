package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.active.PortalData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.LocalizedString;

@Reflect("content.components.ClientCampfireActivatorComponent")
public class ClientCampfireActivatorComponent extends ClientActivatorConditionComponent
{
    private LocalizedString lockedText;
    private LocalizedString outsideText;
    private LocalizedString fail;

    public ClientCampfireActivatorComponent()
    {
        this.lockedText = new LocalizedString();
        this.outsideText = new LocalizedString();
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public boolean testCondition(PlayerData playerData, ComponentObject componentObject)
    {
        ItemData itemData = ((ItemData) componentObject);
        Map map = itemData.getMap();

        if (!map.getDimension().equals("default") &&
                !map.getDimension().equals("swamp2") &&
                !map.getDimension().equals("forest"))
        {
            fail = outsideText;

            return false;
        }

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc == null)
            return false;

        ConsumableContainer cnt = poc.getConsumableContainer();

        for (ConsumableRecord record : cnt.getData().values())
        {
            Content content = record.getItem().getContent();

            if (content.hasComponent(CampFireStarterComponent.class))
                return true;
        }

        fail = lockedText;

        return false;
    }

    @Override
    public String getFailedConditionLocalizedText()
    {
        return fail.get();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData.has("lockedText"))
        {
            this.lockedText.set(jsonData.getString("lockedText"));
        }

        if (jsonData.has("outsideText"))
        {
            this.outsideText.set(jsonData.getString("outsideText"));
        }
    }


}
