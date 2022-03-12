package com.desertkun.brainout.content.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.active.ThrowableActiveData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.active.ThrowableActive")
public class ThrowableActive extends Active
{
    private boolean showOnMinimap;

    @Override
    public ThrowableActiveData getData(String dimension)
    {
        return new ThrowableActiveData(this, dimension);
    }

    @Override
    public boolean isEditorSelectable()
    {
        return false;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        showOnMinimap = jsonData.getBoolean("showOnMinimap", true);
    }

    public boolean isShowOnMinimap()
    {
        return showOnMinimap;
    }
}
