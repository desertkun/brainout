package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.AutoRemoveComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.AutoRemoveComponent")
public class AutoRemoveComponent extends ContentComponent
{
    private Kind kind;

    public enum Kind
    {
        XLessThanZero
    }

    @Override
    public AutoRemoveComponentData getComponent(ComponentObject componentObject)
    {
        return new AutoRemoveComponentData((ActiveData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    public Kind getKind()
    {
        return kind;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.kind = Kind.valueOf(jsonData.getString("kind"));
    }
}
