package com.desertkun.brainout.mode;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.SpectatorTeam;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.data.Data;

public class GameModeNormal<R extends GameModeRealization> extends GameMode<R>
{
    private ObjectMap<Team, Float> points;
    private float initialPoints;

    public GameModeNormal(Class<? extends GameModeRealization> realization)
    {
        super(realization, GameModeNormal.class);

        this.points = new ObjectMap<>();
    }

    public int getPoints(Team team)
    {
        return (int)Math.ceil(this.points.get(team, 0.0f));
    }

    @Override
    public float getGameProgress()
    {
        float min = initialPoints;

        for (ObjectMap.Entry<Team, Float> point : points)
        {
            if (point.key instanceof SpectatorTeam)
                continue;

            min = Math.min(min, point.value);
        }

        return 1.0f - min / initialPoints;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        points.clear();
        if (jsonData.has("points"))
        {
            JsonValue pointsValue = jsonData.get("points");

            if (pointsValue.isObject())
            {
                for (JsonValue point: pointsValue)
                {
                    Team team = ((Team) BrainOut.ContentMgr.get(point.name()));
                    float p = point.asFloat();

                    if (team != null)
                    {
                        points.put(team, p);
                    }
                }
            }
        }

        initialPoints = jsonData.getFloat("inpoints", 0);

        super.read(json, jsonData);
    }

    @Override
    public void write(Json json, Data.ComponentWriter componentWriter, int owner)
    {
        json.writeObjectStart("points");
        for (ObjectMap.Entry<Team, Float> point : points)
        {
            json.writeValue(point.key.getID(), point.value);
        }
        json.writeObjectEnd();

        json.writeValue("inpoints", initialPoints);

        super.write(json, componentWriter, owner);
    }

    @Override
    public ID getID()
    {
        return ID.normal;
    }

    public ObjectMap<Team, Float> getPoints()
    {
        return points;
    }

    public float getInitialPoints()
    {
        return initialPoints;
    }

    public void setInitialPoints(float initialPoints)
    {
        this.initialPoints = initialPoints;
    }
}
