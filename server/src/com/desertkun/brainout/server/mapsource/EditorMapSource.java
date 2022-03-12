package com.desertkun.brainout.server.mapsource;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.server.ServerController;
import com.desertkun.brainout.server.ServerSettings;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("source.EditorMapSource")
public class EditorMapSource extends MapSource implements MapSource.Settings
{
    private final int width;
    private final int height;
    private final String mapName;

    private ServerSettings.MapConditions map;
    private ServerSettings.GameModeConditions mode;

    public EditorMapSource(String mapName, int width, int height)
    {
        this.map = new ServerSettings.MapConditions(mapName);
        this.mode = new ServerSettings.GameModeConditions(GameMode.ID.editor);

        this.mapName = mapName;
        this.width = width;
        this.height = height;
    }

    @Override
    public Array<ServerMap> loadMaps(ServerSettings.MapConditions settings, boolean init)
    {
        ServerController C = BrainOutServer.Controller;

        if (C.mapExists(mapName)) return null;

        Array<ServerMap> maps = new Array<>();

        ServerMap map = C.createMap(width, height, "default");
        maps.add(map);
        map.setName(new FileHandle(mapName).nameWithoutExtension());

        if (init)
        {
            map.init();
        }

        return maps;
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
        return BrainOutServer.Controller.setPlayState(callback, PlayState.ID.game);
    }
}
