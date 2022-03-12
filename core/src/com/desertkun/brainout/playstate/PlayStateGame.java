package com.desertkun.brainout.playstate;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.mode.GameMode;

public class PlayStateGame extends PlayState
{
    private GameMode mode;

    public PlayStateGame()
    {
    }

    @Override
    public ID getID()
    {
        return ID.game;
    }

    public GameMode getMode()
    {
        return mode;
    }

    public GameMode setMode(GameMode.ID mode)
    {
        if (this.mode != null)
        {
            this.mode.release();
        }

        this.mode = BrainOut.getInstance().newMode(mode);

        return this.mode;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (mode != null)
        {
            mode.update(dt);
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("mode"))
        {
            GameMode.ID mode = GameMode.ID.valueOf(jsonData.getString("mode"));
            setMode(mode);
            getMode().read(json, jsonData.get("modeData"));
        }
    }

    @Override
    public void write(Json json, Data.ComponentWriter componentWriter, int owner)
    {
        super.write(json, componentWriter, owner);

        if (mode != null)
        {
            json.writeValue("mode", mode.getID());
            json.writeObjectStart("modeData");
            mode.write(json, componentWriter, owner);
            json.writeObjectEnd();
        }
    }

    public GameMode.Phase getPhase()
    {
        if (mode == null)
            return GameMode.Phase.none;

        return mode.getPhase();
    }

    public boolean isGameActive()
    {
        return getMode().isGameActive();
    }

    public boolean isGameFinished()
    {
        return mode != null && mode.isGameFinished();
    }

    @Override
    public boolean onEvent(Event event)
    {
        return mode.onEvent(event);
    }

    public void initMode(InitCallback callback)
    {
        mode.init(callback);
    }
}
