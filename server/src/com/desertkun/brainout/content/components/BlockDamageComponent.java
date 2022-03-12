package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.BlockDamageComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.BlockDamageComponent")
public class BlockDamageComponent extends ContentComponent
{
    private float damage;
    private float period;

    @Override
    public BlockDamageComponentData getComponent(ComponentObject componentObject)
    {
        return new BlockDamageComponentData((BlockData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        damage = jsonData.getFloat("damage");
        period = jsonData.getFloat("period");
    }

    public float getDamage()
    {
        return damage;
    }

    public float getPeriod()
    {
        return period;
    }
}
