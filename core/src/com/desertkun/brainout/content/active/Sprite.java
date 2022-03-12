package com.desertkun.brainout.content.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.active.SpriteData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.active.Sprite")
public class Sprite extends Active
{
    private String spriteName;
    private float scale;

    @Override
    public SpriteData getData(String dimension)
    {
        return new SpriteData(this, dimension);
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

        spriteName = jsonData.getString("sprite");
        scale = jsonData.getFloat("scale", 1);
    }

    public String getSpriteName()
    {
        return spriteName;
    }

    public float getScale()
    {
        return scale;
    }
}
