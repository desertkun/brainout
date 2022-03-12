package com.desertkun.brainout.content.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PointData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.active.Active")
public class Active extends Content
{
    private Team team;
    private int zIndex;

    public ActiveData getData(String dimension)
    {
        return new PointData(this, dimension);
    }

    @Override
    public boolean isEditorSelectable()
    {
        return true;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        zIndex = jsonData.getInt("zIndex", 0);
        if (jsonData.has("team"))
        {
            team = (Team)BrainOut.ContentMgr.get(jsonData.getString("team"));
        }
    }

    public int getzIndex()
    {
        return zIndex;
    }

    public Team getTeam()
    {
        return team;
    }
}
