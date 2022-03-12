package com.desertkun.brainout.server.mapsource;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.server.ServerSettings;
import com.esotericsoftware.minlog.Log;

import java.io.InputStream;

@Reflect("source.StreamMapSource")
public class StreamMapSource extends MapSource implements MapSource.Settings
{
    private final GameMode.ID mode;
    private final String mapName;
    private PlayState.ID playState;
    private final InputStream map;
    private final ObjectMap<String, String> custom;

    public StreamMapSource(InputStream map, GameMode.ID mode, String mapName, ObjectMap<String, String> custom)
    {
        this.map = map;
        this.mapName = mapName;
        this.playState = PlayState.ID.game;
        this.mode = mode;
        this.custom = custom;
    }

    @Override
    public boolean insert(String map, String mode)
    {
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
        return new ServerSettings.MapConditions();
    }

    @Override
    public ServerSettings.GameModeConditions acquireMode()
    {
        return new ServerSettings.GameModeConditions(mode);
    }

    @Override
    public ServerSettings.BaseConditions getAdditionalSettings()
    {
        return null;
    }

    @Override
    public Array<ServerMap> loadMaps(ServerSettings.MapConditions settings, boolean init)
    {
        Array<ServerMap> maps = BrainOutServer.Controller.loadMaps(this.map, true);

        if (maps != null)
        {
            for (ServerMap map : maps)
            {
                map.setName(mapName);

                for (ObjectMap.Entry<String, String> entry : custom)
                {
                    map.setCustom(entry.key, entry.value);
                }
            }

            if (init)
            {
                for (ServerMap map : maps)
                {
                    map.init();
                }
            }
        }

        return maps;
    }

    @Override
    public PlayState init(PlayState.InitCallback callback)
    {
        return BrainOutServer.Controller.setPlayState(callback, playState);
    }
}
