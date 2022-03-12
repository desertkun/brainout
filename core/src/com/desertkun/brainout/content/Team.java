package com.desertkun.brainout.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.active.Player;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.Team")
public class Team extends Content
{
    private Color color;

    public Team()
    {
        this.color = Color.WHITE;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("color"))
        {
            color = Color.valueOf(jsonData.getString("color"));
        }
    }

    @Override
    public void write(Json json)
    {
        json.writeValue("title", getTitle().get());
        json.writeValue("color", color.toString());
    }

    public Color getColor()
    {
        return color;
    }

    @Override
    public boolean isEditorSelectable()
    {
        return true;
    }
}
