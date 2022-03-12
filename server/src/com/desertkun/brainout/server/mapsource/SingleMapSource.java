package com.desertkun.brainout.server.mapsource;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.server.ServerSettings;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("source.SingleMapSource")
public class SingleMapSource extends MapSource implements MapSource.Settings
{
    private ServerSettings.MapConditions map;
    private ServerSettings.GameModeConditions mode;
    private PlayState.ID playState;

    public SingleMapSource()
    {
        this.map = new ServerSettings.MapConditions();
        this.mode = new ServerSettings.GameModeConditions();
        this.playState = PlayState.ID.game;
    }

    public SingleMapSource(String map, GameMode.ID mode)
    {
        this.map = new ServerSettings.MapConditions(map);
        this.mode = new ServerSettings.GameModeConditions(mode);
        this.playState = PlayState.ID.game;
    }

    @Override
    public boolean insert(String map, String mode)
    {
        this.map.map = map;
        this.mode.mode = GameMode.ID.valueOf(mode);
        this.playState = PlayState.ID.game;

        return true;
    }

    @Override
    public void wrapPlayState(PlayState.ID playState)
    {
        this.playState = playState;
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
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        String map = jsonData.getString("map");
        String mode = jsonData.getString("mode");

        this.map.map = map;
        this.mode.mode = GameMode.ID.valueOf(mode);
    }

    @Override
    public PlayState init(PlayState.InitCallback callback)
    {
        return BrainOutServer.Controller.setPlayState(callback, playState);
    }
}
