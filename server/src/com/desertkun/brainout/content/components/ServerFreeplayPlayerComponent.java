package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.ServerFreeplayPlayerComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerFreeplayPlayerComponent")
public class ServerFreeplayPlayerComponent extends ContentComponent
{
    private float bonesSpeed;
    private float bonesDamage;
    private String bonesEffect;
    private String swampEffect;

    private float godMode;
    private int tries;
    private float woundHealth;
    private float restoredHealth;
    private float reviveTime;

    private float bonesBleedingDuration;
    private float bonesBleedingIntensity;

    @Override
    public ServerFreeplayPlayerComponentData getComponent(ComponentObject componentObject)
    {
        return new ServerFreeplayPlayerComponentData((PlayerData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }


    public float getGodMode()
    {
        return godMode;
    }

    public int getTries()
    {
        return tries;
    }

    public float getWoundHealth()
    {
        return woundHealth;
    }

    public float getRestoredHealth()
    {
        return restoredHealth;
    }

    public float getReviveTime()
    {
        return reviveTime;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        godMode = jsonData.getFloat("god-time", 1.0f);
        tries = jsonData.getInt("total", 2);
        woundHealth = jsonData.getFloat("set-health", 400);
        restoredHealth = jsonData.getFloat("restore-health", 50);
        reviveTime = jsonData.getFloat("revive-time", 5);

        bonesSpeed = jsonData.getFloat("bones-speed", 10);
        bonesDamage = jsonData.getFloat("bones-damage", 10);
        bonesEffect = jsonData.getString("bones-effect");
        swampEffect = jsonData.getString("swamp-effect");
        bonesBleedingDuration = jsonData.getFloat("bones-bleeding-duration");
        bonesBleedingIntensity = jsonData.getFloat("bones-bleeding-intensity");
    }

    public String getSwampEffect()
    {
        return swampEffect;
    }

    public float getBonesDamage()
    {
        return bonesDamage;
    }

    public String getBonesEffect()
    {
        return bonesEffect;
    }

    public float getBonesSpeed()
    {
        return bonesSpeed;
    }

    public float getBonesBleedingDuration()
    {
        return bonesBleedingDuration;
    }

    public float getBonesBleedingIntensity()
    {
        return bonesBleedingIntensity;
    }
}
