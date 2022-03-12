package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.FreeplayExitDoorData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.ClientFreeplayExitDoorComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.mode.ClientFreeRealization;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.GameModeFree;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientFreeplayExitDoorComponent")
public class ClientFreeplayExitDoorComponent extends ClientActivatorConditionComponent
{
    private String iconName;

    private TextureAtlas.AtlasRegion icon;

    @Override
    public ClientFreeplayExitDoorComponentData getComponent(ComponentObject componentObject)
    {
        return new ClientFreeplayExitDoorComponentData((FreeplayExitDoorData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    private boolean isAllowedToLeave(PlayerData playerData)
    {
        return true;
    }

    @Override
    public boolean testCondition(PlayerData playerData, ComponentObject componentObject)
    {
        return isAllowedToLeave(playerData);
    }

    @Override
    public String getFailedConditionLocalizedText()
    {
        return L.get("MENU_NEED_DOGTAG");
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        icon = BrainOutClient.getRegion(iconName);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        iconName = jsonData.getString("icon");
    }

    public TextureAtlas.AtlasRegion getIcon()
    {
        return icon;
    }
}
