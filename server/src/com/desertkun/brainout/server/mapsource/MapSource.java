package com.desertkun.brainout.server.mapsource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.server.ServerConstants;
import com.desertkun.brainout.server.ServerSettings;

public abstract class MapSource implements Json.Serializable
{
    public interface Settings
    {
        ServerSettings.MapConditions acquireMap();
        ServerSettings.GameModeConditions acquireMode();
        ServerSettings.BaseConditions getAdditionalSettings();

        PlayState init(PlayState.InitCallback callback);
    }

    public boolean insert(String map, String mode) { return false; }
    public void setForceMode(String mode) {}
    public void wrapPlayState(PlayState.ID playState) {}

    public abstract Settings next();

    public void next(PlayState.InitCallback callback, PlayState.ID playState)
    {
        BrainOutServer.Controller.setPlayState((success) -> {
            if (success)
            {
                next();
            }

            callback.done(success);
        }, playState);
    }

    private void _processConditions(ServerSettings.BaseConditions conditions)
    {
        if (conditions == null) return;

        conditions.getDefines(BrainOutServer.PackageMgr.getDefines());
    }

    private void _registerPackages(ServerSettings.BaseConditions conditions)
    {
        if (conditions == null) return;

        for (String s: conditions.packages)
        {
            BrainOutServer.PackageMgr.registerPackage(s);
        }
    }

    public void processConditions(Settings settings)
    {
        ServerSettings.GameModeConditions modeConditions = settings.acquireMode();
        ServerSettings.MapConditions mapConditions = settings.acquireMap();

        _processConditions(BrainOutServer.Settings.getBaseConditions());
        _processConditions(modeConditions);
        _processConditions(mapConditions);
        _processConditions(settings.getAdditionalSettings());

        _registerPackages(BrainOutServer.Settings.getBaseConditions());
        _registerPackages(modeConditions);
        _registerPackages(mapConditions);
        _registerPackages(settings.getAdditionalSettings());
    }

    public void loadMode(GameMode gameMode, JsonValue settings)
    {
        if (settings != null)
        {
            gameMode.read(BrainOut.R.JSON, settings);
        }
    }

    public Array<ServerMap> loadMaps(ServerSettings.MapConditions settings, boolean init)
    {
        if (settings == null)
        {
            throw new RuntimeException("No map passed");
        }

        if (settings.map == null)
        {
            return null;
        }

        Array<ServerMap> maps;

        FileHandle mapHandle = Gdx.files.absolute(settings.map);

        if (mapHandle.exists())
        {
            maps = BrainOutServer.Controller.loadMaps(settings.map, ServerConstants.Maps.MAP_KEY);

            for (ServerMap map : maps)
            {
                map.setName(mapHandle.nameWithoutExtension());
            }
        }
        else
        {
            throw new RuntimeException("Map '" + settings.map + "' was not found.");
        }

        if (init)
        {
            for (ServerMap map : maps)
            {
                map.init();
            }
        }

        return maps;
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {

    }
}
