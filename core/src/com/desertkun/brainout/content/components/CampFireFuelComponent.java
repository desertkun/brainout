package com.desertkun.brainout.content.components;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.CampFireFuelComponent")
public class CampFireFuelComponent extends ContentComponent
{
    private float duration;
    private int need;

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        duration = jsonData.getFloat("duration");
        need = jsonData.getInt("need", 1);
    }

    public int getNeed()
    {
        return need;
    }

    public float getDuration(int quality)
    {
        return Interpolation.sineOut.apply((float)quality / 100.0f) * duration;
    }
}
