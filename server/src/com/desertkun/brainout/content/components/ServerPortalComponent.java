package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.PortalData;
import com.desertkun.brainout.data.components.ServerPortalComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerPortalComponent")
public class ServerPortalComponent extends ContentComponent
{
    private String activateEffect;
    private String lockedActivateEffect;

    @Override
    public ServerPortalComponentData getComponent(ComponentObject componentObject)
    {
        return new ServerPortalComponentData((PortalData)componentObject, this);
    }

    public ServerPortalComponent()
    {
        activateEffect = "";
        lockedActivateEffect = "";
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.activateEffect = jsonData.getString("activateEffect", "");
        this.lockedActivateEffect = jsonData.getString("lockedActivateEffect", "");
    }

    public String getActivateEffect()
    {
        return activateEffect;
    }

    public String getLockedActivateEffect()
    {
        return lockedActivateEffect;
    }
}
