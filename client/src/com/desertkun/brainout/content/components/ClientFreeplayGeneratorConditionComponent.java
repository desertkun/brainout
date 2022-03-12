package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.FreeplayGeneratorData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.active.PortalData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.LocalizedString;

@Reflect("content.components.ClientFreeplayGeneratorConditionComponent")
public class ClientFreeplayGeneratorConditionComponent extends ClientActivatorConditionComponent
{
    private LocalizedString noPowerText;
    private String generatorName;

    public ClientFreeplayGeneratorConditionComponent()
    {
        this.noPowerText = new LocalizedString();
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public boolean testCondition(PlayerData playerData, ComponentObject componentObject)
    {
        Map map = playerData.getMap();

        if (map == null)
            return false;

        ActiveData activeData = map.getActiveNameIndex().get(generatorName);

        if (!(activeData instanceof FreeplayGeneratorData))
            return false;

        FreeplayGeneratorData generator = ((FreeplayGeneratorData) activeData);

        return generator.isWorking();
    }

    @Override
    public String getFailedConditionLocalizedText()
    {
        return noPowerText.get();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        generatorName = jsonData.getString("generator");

        if (jsonData.has("noPowerText"))
        {
            this.noPowerText.set(jsonData.getString("noPowerText"));
        }
    }
}
