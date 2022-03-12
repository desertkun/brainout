package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ActiveDamageComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ActiveDamageComponent")
public class ActiveDamageComponent extends ContentComponent
{
    private float damage;
    private float period;
    private float x;
    private float y;
    private float width;
    private float height;
    private String kind;

    public ActiveDamageComponent()
    {

    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new ActiveDamageComponentData((ActiveData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.damage = jsonData.getFloat("damage");
        this.period = jsonData.getFloat("period");
        this.x = jsonData.getFloat("x");
        this.y = jsonData.getFloat("y");
        this.width = jsonData.getFloat("width");
        this.height = jsonData.getFloat("height");
        this.kind = jsonData.getString("kind", Constants.Damage.DAMAGE_FRACTURE);
    }

    public float getPeriod()
    {
        return period;
    }

    public float getDamage()
    {
        return damage;
    }

    public float getHeight()
    {
        return height;
    }

    public float getWidth()
    {
        return width;
    }

    public float getX()
    {
        return x;
    }

    public float getY()
    {
        return y;
    }

    public String getKind()
    {
        return kind;
    }
}
