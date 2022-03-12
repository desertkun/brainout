package com.desertkun.brainout.content.consumable.impl;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.PlayerSkin;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.consumable.impl.PlayerSkinConsumableItem")
public class PlayerSkinConsumableItem extends ConsumableItem
{
    private PlayerSkin playerSkin;

    public PlayerSkinConsumableItem(PlayerSkin playerSkin)
    {
        this.playerSkin = playerSkin;
    }

    public PlayerSkinConsumableItem()
    {
        this.playerSkin = null;
    }

    @Override
    public PlayerSkin getContent()
    {
        return playerSkin;
    }

    @Override
    public boolean stacks(ConsumableItem item)
    {
        return false;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        playerSkin = BrainOut.ContentMgr.get(jsonData.getString("skin"), PlayerSkin.class);
    }

    public void setPlayerSkin(PlayerSkin playerSkin)
    {
        this.playerSkin = playerSkin;
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("skin", playerSkin.getID());
    }
}
