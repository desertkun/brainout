package com.desertkun.brainout.playstate;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.mode.GameMode;

public class PlayStateEmpty extends PlayState
{
    private GameMode.ID nextMode;

    @Override
    public ID getID()
    {
        return ID.empty;
    }

    public void setNextMode(GameMode.ID nextMode)
    {
        this.nextMode = nextMode;
    }

    public GameMode.ID getNextMode()
    {
        return nextMode;
    }

    @Override
    public void write(Json json, Data.ComponentWriter componentWriter, int owner)
    {
        super.write(json, componentWriter, owner);

        json.writeValue("nm", nextMode);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        nextMode = GameMode.ID.valueOf(jsonData.getString("nm"));
    }
}
