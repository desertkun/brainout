package com.desertkun.brainout.mode;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.active.Flag;
import com.desertkun.brainout.data.Data;

public class GameModeLobby<R extends GameModeRealization> extends GameMode<R>
{
    public GameModeLobby(Class<? extends GameModeRealization> realization)
    {
        super(realization, GameModeLobby.class);
    }

    @Override
    public float getGameProgress()
    {
        return 0;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);
    }

    @Override
    public void write(Json json, Data.ComponentWriter componentWriter, int owner)
    {
        super.write(json, componentWriter, owner);
    }

    @Override
    public boolean validateActive(Active active)
    {
        return !(active instanceof Flag);
    }

    @Override
    public ID getID()
    {
        return ID.lobby;
    }

    @Override
    public boolean isAutoKickEnabled()
    {
        return false;
    }

}
