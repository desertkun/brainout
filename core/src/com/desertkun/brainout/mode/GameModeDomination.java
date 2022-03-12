package com.desertkun.brainout.mode;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.data.Data;

public class GameModeDomination<R extends GameModeRealization> extends GameMode<R>
{
    private int winPoints;
    private int takeFlagPoints;
    private int holdFlagTime;
    private int holdFlagPoints;

    private ObjectMap<Team, Integer> points;

    public GameModeDomination(Class<? extends GameModeRealization> realization)
    {
        super(realization, GameModeDomination.class);

        this.points = new ObjectMap<Team, Integer>();
    }

    public int getPoints(Team team)
    {
        Integer points = this.points.get(team);

        if (points != null)
        {
            return points;
        }

        return 0;
    }

    @Override
    public float getGameProgress()
    {
        int max = 0;

        for (Integer value : points.values())
        {
            max = Math.max(max, value);
        }

        return (float)max / (float)winPoints;
    }

    @Override
    public ID getID()
    {
        return ID.domination;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData.has("winPoints"))
            this.winPoints = jsonData.getInt("winPoints");

        if (jsonData.has("takeFlagPoints"))
            this.takeFlagPoints = jsonData.getInt("takeFlagPoints");

        if (jsonData.has("holdFlagTime"))
            this.holdFlagTime = jsonData.getInt("holdFlagTime");

        if (jsonData.has("holdFlagPoints"))
            this.holdFlagPoints = jsonData.getInt("holdFlagPoints");

        points.clear();
        if (jsonData.has("points"))
        {
            JsonValue pointsValue = jsonData.get("points");

            if (pointsValue.isObject())
            {
                for (JsonValue point: pointsValue)
                {
                    Team team = ((Team) BrainOut.ContentMgr.get(point.name()));
                    int p = point.asInt();

                    if (team != null)
                    {
                        points.put(team, p);
                    }
                }
            }
        }

        super.read(json, jsonData);
    }

    @Override
    public void write(Json json, Data.ComponentWriter componentWriter, int owner)
    {
        json.writeValue("winPoints", getWinPoints());
        json.writeValue("takeFlagPoints", getTakeFlagPoints());
        json.writeValue("holdFlagTime", getHoldFlagTime());
        json.writeValue("holdFlagPoints", getHoldFlagPoints());

        json.writeObjectStart("points");
        for (ObjectMap.Entry<Team, Integer> point : points)
        {
            json.writeValue(point.key.getID(), point.value);
        }
        json.writeObjectEnd();

        super.write(json, componentWriter, owner);
    }

    public int getWinPoints()
    {
        return winPoints;
    }

    public int getTakeFlagPoints()
    {
        return takeFlagPoints;
    }

    public int getHoldFlagTime()
    {
        return holdFlagTime;
    }

    public int getHoldFlagPoints()
    {
        return holdFlagPoints;
    }

    public ObjectMap<Team, Integer> getPoints()
    {
        return points;
    }
}
