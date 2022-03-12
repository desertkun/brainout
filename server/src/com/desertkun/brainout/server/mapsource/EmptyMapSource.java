package com.desertkun.brainout.server.mapsource;


import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.PlayStateEmpty;
import com.desertkun.brainout.playstate.ServerPSEmpty;
import com.desertkun.brainout.server.ServerSettings;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("source.EmptyMapSource")
public class EmptyMapSource extends MapSource implements MapSource.Settings
{
    private final GameMode.ID mode;

    public EmptyMapSource(GameMode.ID mode)
    {
        this.mode = mode;
    }

    @Override
    public Settings next()
    {
        return this;
    }

    @Override
    public ServerSettings.MapConditions acquireMap()
    {
        return null;
    }

    @Override
    public ServerSettings.GameModeConditions acquireMode()
    {
        return null;
    }

    @Override
    public ServerSettings.BaseConditions getAdditionalSettings()
    {
        return null;
    }

    @Override
    public PlayState init(PlayState.InitCallback callback)
    {
        PlayStateEmpty ps = (PlayStateEmpty)BrainOutServer.Controller.setPlayState(callback, PlayState.ID.empty);
        ps.setNextMode(mode);
        return ps;
    }
}
