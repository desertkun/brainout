package com.desertkun.brainout.data.active;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.SpawnTarget;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.Flag;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.Spawnable;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.inspection.*;
import com.desertkun.brainout.utils.LocalizedString;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.active.FlagData")
public class FlagData extends PointData implements Spawnable
{
    private LocalizedString spawnName;
    private float takeSpeed;

    @Override
    public float getSpawnX()
    {
        return getX();
    }

    @Override
    public float getSpawnY()
    {
        return getY();
    }

    @Override
    public boolean canSpawn(Team teamFor)
    {
        return (getTeam() == teamFor && (state == State.normal));
    }

    @Override
    public SpawnTarget getTarget()
    {
        return SpawnTarget.flag;
    }

    @InspectableGetter(name="name", kind= PropertyKind.string, value = PropertyValue.vString)
    public String getName()
    {
        return spawnName.getID();
    }

    @InspectableSetter(name="name")
    public void setName(String name)
    {
        spawnName.set(name);
    }

    public enum State
    {
        normal,
        taking,
        paused
    }

    private State state;
    private float time;
    private Team takingTeam;

    @InspectableProperty(name = "range", kind = PropertyKind.string, value = PropertyValue.vFloat)
    public float spawnRange;

    public FlagData(Flag flag, String dimension)
    {
        super(flag, dimension);

        time = 0;
        state = State.normal;
        takingTeam = null;
        spawnRange = flag.getSpawnRange();
        spawnName = new LocalizedString();

        if (flag.getSpawnName() != null)
        {
            spawnName.set(flag.getSpawnName());
        }
        else
        {
            spawnName.set(flag.getTitle().getID());
        }

        takeSpeed = 1;

        setzIndex(1);
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        if (takingTeam != null)
        {
            json.writeValue("takingteam", takingTeam.getID());
        }
        json.writeValue("state", state.toString());
        json.writeValue("time", time);
        json.writeValue("spawnRange", spawnRange);
        json.writeValue("sname", spawnName.getID());
        json.writeValue("speed", takeSpeed);
    }

    @Override
    public String toString()
    {
        return spawnName.get();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        spawnName.set(jsonData.getString("sname", getContent().getTitle().getID()));
        state = State.valueOf(jsonData.getString("state"));
        time = jsonData.getFloat("time");
        spawnRange = jsonData.getFloat("spawnRange", 0);
        takeSpeed = jsonData.getFloat("speed", 1.0f);

        if (jsonData.has("takingteam"))
        {
            Content cnt = BrainOut.ContentMgr.get(jsonData.getString("takingteam"));

            if (cnt instanceof Team)
            {
                takingTeam = ((Team)cnt);
            }
            else
            {
                takingTeam = null;
            }
        }
        else
        {
            takingTeam = null;
        }
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        switch (state)
        {
            case taking:
            {
                time -= dt * takeSpeed;

                if (time < 0)
                {
                    BrainOut.EventMgr.sendEvent(this, SimpleEvent.obtain(SimpleEvent.Action.flagTakeChanged));

                    setTeam(takingTeam);
                    setState(State.normal);

                    updated();
                }
                break;
            }
        }
    }

    @Override
    public float getSpawnRange()
    {
        return spawnRange;
    }

    public void setTime(float time)
    {
        this.time = time;
    }

    public float getTimeValue()
    {
        return 1.0f - time / ((Flag) getContent()).getTakeTime();
    }

    public State getState()
    {
        return state;
    }

    public void setState(State state)
    {
        this.state = state;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    public Team getTakingTeam()
    {
        return takingTeam;
    }

    public void setTakingTeam(Team takingTeam)
    {
        this.takingTeam = takingTeam;
    }

    @Override
    public int getZIndex()
    {
        return 15;
    }

    public float getTakeSpeed()
    {
        return takeSpeed;
    }

    public void setTakeSpeed(float takeSpeed)
    {
        this.takeSpeed = takeSpeed;
    }

    @Override
    public int getTags()
    {
        return super.getTags() | WithTag.TAG(Constants.ActiveTags.SPAWNABLE) | WithTag.TAG(Constants.ActiveTags.FLAG);
    }
}
