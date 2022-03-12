package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ActiveColliderComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ActiveColliderComponent")
public class ActiveColliderComponent extends ContentComponent
{
    private boolean needsValidateion;

    @Override
    public Component getComponent(ComponentObject playerData)
    {
        return new ActiveColliderComponentData((ActiveData)playerData, this);
    }

    @Override
    public void write(Json json)
    {
        //
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        needsValidateion = jsonData.getBoolean("needsValidation", true);
    }

    public boolean isNeedsValidateion()
    {
        return needsValidateion;
    }
}
