package com.desertkun.brainout.mode;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.active.Flag;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.active.FlagData;
import com.desertkun.brainout.data.interfaces.Spawnable;

public class GameModeCTF<R extends GameModeRealization> extends GameMode<R>
{
    private int winPoints;
    private int chips;
    private ObjectMap<Team, Integer> points;
    private ObjectMap<Team, Integer> takingPoints;

    public GameModeCTF(Class<? extends GameModeRealization> realization)
    {
        super(realization, GameModeCTF.class);

        this.winPoints = 3;
        this.chips = 4;
        this.points = new ObjectMap<>();
        this.takingPoints = new ObjectMap<>();
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
        float max = 0;

        for (ObjectMap.Entry<Team, Integer> entry : points)
        {
            max = Math.max(max, entry.value + getTakingPoints(entry.key) * 0.5f);
        }

        return (float)max / (float)winPoints;
    }

    @Override
    public ID getID()
    {
        return ID.ctf;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData.has("winPoints"))
            this.winPoints = jsonData.getInt("winPoints");

        if (jsonData.has("chips"))
            this.chips = jsonData.getInt("chips");

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

        takingPoints.clear();
        if (jsonData.has("tp"))
        {
            JsonValue pointsValue = jsonData.get("tp");

            if (pointsValue.isObject())
            {
                for (JsonValue point: pointsValue)
                {
                    Team team = ((Team) BrainOut.ContentMgr.get(point.name()));
                    int p = point.asInt();

                    if (team != null)
                    {
                        takingPoints.put(team, p);
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
        json.writeValue("chips", getChips());

        json.writeObjectStart("points");
        for (ObjectMap.Entry<Team, Integer> point : points)
        {
            json.writeValue(point.key.getID(), point.value);
        }
        json.writeObjectEnd();

        json.writeObjectStart("tp");
        for (ObjectMap.Entry<Team, Integer> point : takingPoints)
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

    public ObjectMap<Team, Integer> getPoints()
    {
        return points;
    }

    public ObjectMap<Team, Integer> getTakingPoints()
    {
        return takingPoints;
    }

    public boolean setTakingPoints(Team team, int amount)
    {
        if (takingPoints.get(team, 0) != amount)
        {
            takingPoints.put(team, amount);
            return true;
        }

        return false;
    }

    public boolean setPoints(Team team, int amount)
    {
        if (points.get(team, 0) != amount)
        {
            points.put(team, amount);
            return true;
        }

        return false;
    }

    public int getTakingPoints(Team team)
    {
        return takingPoints.get(team, 0);
    }

    @Override
    public boolean validateActive(Active active)
    {
        if (active instanceof Flag)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean canSpawn(Spawnable spawnable, Team team)
    {
        if (spawnable instanceof FlagData)
        {
            return false;
        }

        return super.canSpawn(spawnable, team);
    }

    public int getChips()
    {
        return chips;
    }
}
