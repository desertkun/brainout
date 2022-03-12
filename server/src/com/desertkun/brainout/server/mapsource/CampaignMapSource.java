package com.desertkun.brainout.server.mapsource;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.server.ServerSettings;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("source.CampaignMapSource")
public class CampaignMapSource extends MapSource implements MapSource.Settings
{
    private final ServerSettings.MapConditions map;
    private final ServerSettings.GameModeConditions mode;



    public CampaignMapSource()
    {
        this.map = new ServerSettings.MapConditions();
        this.mode = new ServerSettings.GameModeConditions();
    }

    @Override
    public Settings next()
    {
        return this;
    }

    @Override
    public ServerSettings.MapConditions acquireMap()
    {
        return map;
    }

    @Override
    public ServerSettings.GameModeConditions acquireMode()
    {
        return mode;
    }

    @Override
    public ServerSettings.BaseConditions getAdditionalSettings()
    {
        return null;
    }

    @Override
    public PlayState init(PlayState.InitCallback callback)
    {
        return null;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

    }
}
