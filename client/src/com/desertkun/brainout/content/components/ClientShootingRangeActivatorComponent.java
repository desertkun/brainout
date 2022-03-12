package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ClientActiveActivatorComponentData;
import com.desertkun.brainout.data.components.ClientShootingRangeActivatorComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.LocalizedString;

@Reflect("content.components.ClientShootingRangeActivatorComponent")
public class ClientShootingRangeActivatorComponent extends ClientActiveActivatorComponent
{
    public ClientShootingRangeActivatorComponent()
    {
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new ClientShootingRangeActivatorComponentData((ActiveData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }
}
