package com.desertkun.brainout.server.mapsource;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.server.ServerSettings;

@Reflect("source.LobbyMapSource")
public class LobbyMapSource extends MapSource implements MapSource.Settings
{
    private ServerSettings.GameModeConditions mode;

    public LobbyMapSource()
    {
        this.mode = new ServerSettings.GameModeConditions(GameMode.ID.lobby);
        this.mode.defines.put("minimapEnabled", "false");
    }

    @Override
    public boolean insert(String map, String mode)
    {
        return false;
    }

    @Override
    public Settings next()
    {
        return this;
    }

    @Override
    public Array<ServerMap> loadMaps(ServerSettings.MapConditions settings, boolean init)
    {
        return new Array<>(new ServerMap[]{new ServerMap("default", 0, 0)});
    }

    @Override
    public ServerSettings.MapConditions acquireMap()
    {
        return null;
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
