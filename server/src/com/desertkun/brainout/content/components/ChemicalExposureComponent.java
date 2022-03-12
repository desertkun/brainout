package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.content.PlayerSkin;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ChemicalExposureComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ChemicalExposureComponent")
public class ChemicalExposureComponent extends ContentComponent
{
    private Array<PlayerSkin> playerSkins;
    private float period;
    private float damage;

    public ChemicalExposureComponent()
    {
        this.playerSkins = new Array();
    }

    @Override
    public ChemicalExposureComponentData getComponent(ComponentObject componentObject)
    {
        return new ChemicalExposureComponentData((ActiveData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        for (JsonValue skin : jsonData.get("skins"))
        {
            playerSkins.add(BrainOutServer.ContentMgr.get(skin.asString(), PlayerSkin.class));
        }
        this.period = jsonData.getFloat("period");
        this.damage = jsonData.getFloat("damage");
    }

    public Array<PlayerSkin> getPlayerSkins()
    {
        return playerSkins;
    }

    public float getPeriod()
    {
        return period;
    }

    public float getDamage()
    {
        return damage;
    }
}
