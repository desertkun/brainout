package com.desertkun.brainout.content;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.Achievement")
public class Achievement extends OwnableContent
{
    private boolean hide;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        hide = jsonData.getBoolean("hide", false);
    }

    public boolean isHide()
    {
        return hide;
    }
}
